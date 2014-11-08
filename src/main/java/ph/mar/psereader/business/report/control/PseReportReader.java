package ph.mar.psereader.business.report.control;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.slf4j.Logger;

import ph.mar.psereader.business.index.entity.PseIndex;
import ph.mar.psereader.business.index.entity.PseIndexQuote;
import ph.mar.psereader.business.market.entity.MarketSummary;
import ph.mar.psereader.business.report.entity.PseReport;
import ph.mar.psereader.business.report.entity.PseReportRow;
import ph.mar.psereader.business.stock.entity.SectorType;
import ph.mar.psereader.business.stock.entity.SubSectorType;

@Dependent
public class PseReportReader {

	private static final String PAGE_HEADER = "(?s)        The Philippine Stock Exchange, Inc\n       Daily Quotations Report\n       [a-zA-Z]{3,9} \\d{2} , \\d{4}\nName Symbol Bid Ask Open High Low Close Volume Value, Php\nNet Foreign\nBuying/\\(Selling\\),\nPhp\n";
	private static final Pattern MARKET_SUMMARY = Pattern.compile("(?s)NO. OF ADVANCES(.*)");
	private static final Pattern REPORT_DATE = Pattern.compile("([a-zA-Z]{3,9} \\d{2} , \\d{4})");
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("MMMM dd , yyyy");
	private static final Pattern NEGATIVE_VALUE = Pattern.compile("^\\((\\d+(\\.\\d+)?)\\)$");
	private static final Pattern POSITIVE_VALUE = Pattern.compile("\\d+(\\.\\d+)?");
	private static final Pattern ADVANCES_COUNT = Pattern.compile("NO. OF ADVANCES: (\\d+)\n");
	private static final Pattern DECLINES_COUNT = Pattern.compile("NO. OF DECLINES: (\\d+)\n");
	private static final Pattern UNCHANGED_COUNT = Pattern.compile("NO. OF UNCHANGED: (\\d+)\n");
	private static final Pattern TRADES_COUNT = Pattern.compile("NO. OF TRADES: (\\d+)\n");
	private static final Pattern TOTAL_FOREIGN_BUY = Pattern.compile("FOREIGN BUYING: Php (\\d+(\\.\\d+)?)\n");
	private static final Pattern TOTAL_FOREIGN_SELL = Pattern.compile("FOREIGN SELLING: Php (\\d+(\\.\\d+)?)\n");
	private static final Pattern SUSPENDED_STOCKS = Pattern
			.compile("(?s)Securities Under Suspension by the Exchange as of [a-zA-Z]{3,9} \\d{2} \\d{4}\n(.*)");

	@Inject
	Logger log;

	public PseReport read(byte[] bytes) {
		try (PDDocument document = PDDocument.load(new ByteArrayInputStream(bytes))) {
			return parseReport(extractContent(document));
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public PseReport read(String filename) {
		try (PDDocument document = PDDocument.load(new File(filename))) {
			return parseReport(extractContent(document));
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private String extractContent(PDDocument document) throws IOException {
		PDFTextStripper stripper = new PDFTextStripper();
		return stripper.getText(document);
	}

	private PseReport parseReport(String content) throws ParseException {
		Date reportDate = parseReportDate(content);
		PseReport report = new PseReport(reportDate);
		String headerlessContent = content.replaceAll(PAGE_HEADER, "");
		parseStocks(headerlessContent, report);
		parseMarketSummary(headerlessContent, report);
		return report;
	}

	private Date parseReportDate(String content) throws ParseException {
		Matcher matcher = REPORT_DATE.matcher(content);

		if (matcher.find()) {
			String date = matcher.group();
			return DATE_FORMAT.parse(date);
		}

		return null;
	}

	private void parseStocks(String headerlessContent, PseReport report) {
		String stocksData = headerlessContent.replaceAll(MARKET_SUMMARY.pattern(), "");
		String[] rows = stocksData.split("\n");
		SectorType currentSector = null;
		SubSectorType currentSubSector = null;

		for (String row : rows) {
			SectorType sector = SectorType.get(row);

			if (sector != null) {
				currentSector = sector;
				currentSubSector = currentSector.getFirstSubSector();
				continue;
			}

			SubSectorType subSector = SubSectorType.get(row);

			if (subSector != null) {
				currentSubSector = subSector;
				continue;
			}

			if (row.contains("TOTAL")) {
				continue;
			}

			String trimmedRow = row.replace(",", "").replaceAll("\\h{2,}", " ");
			String[] columns = trimmedRow.split(" ");

			if (columns.length >= 11) {
				ArrayUtils.reverse(columns);
				PseReportRow reportRow = new PseReportRow(columns[9], currentSector, currentSubSector);
				reportRow.setOpen(parseBigDecimal(columns[6]));
				reportRow.setHigh(parseBigDecimal(columns[5]));
				reportRow.setLow(parseBigDecimal(columns[4]));
				reportRow.setClose(parseBigDecimal(columns[3]));
				reportRow.setVolume(parseLong(columns[2]));

				if (reportRow.isValid()) {
					reportRow.setBid(parseBigDecimal(columns[8]));
					reportRow.setAsk(parseBigDecimal(columns[7]));
					reportRow.setValue(parseBigDecimal(columns[1]));
					reportRow.setForeignBuySell(parseValue(columns[0]));
					reportRow.setCompanyName(parseCompanyName(columns));
					reportRow.setReport(report);
					report.add(reportRow);
				}

				continue;
			}
		}
	}

	private void parseMarketSummary(String headerlessContent, PseReport report) {
		Matcher marketSummaryMatcher = MARKET_SUMMARY.matcher(headerlessContent);

		if (!marketSummaryMatcher.find()) {
			return;
		}

		String marketSummaryData = marketSummaryMatcher.group();
		marketSummaryData = marketSummaryData.replace(",", "").replaceAll("\\h{2,}", " ");

		parseMarketSummaryStats(report, marketSummaryData);
		parsePseIndeces(report, marketSummaryData);
		parseSuspendedStocks(report, marketSummaryData);
	}

	private void parseMarketSummaryStats(PseReport report, String marketSummaryData) {
		String advancesCount = extractSummaryData(ADVANCES_COUNT, marketSummaryData);
		String declinesCount = extractSummaryData(DECLINES_COUNT, marketSummaryData);
		String unchangedCount = extractSummaryData(UNCHANGED_COUNT, marketSummaryData);
		String tradesCount = extractSummaryData(TRADES_COUNT, marketSummaryData);
		String totalForeignBuy = extractSummaryData(TOTAL_FOREIGN_BUY, marketSummaryData);
		String totalForeignSell = extractSummaryData(TOTAL_FOREIGN_SELL, marketSummaryData);

		MarketSummary marketSummary = new MarketSummary(report.getDate());
		marketSummary.setAdvancesCount(Integer.parseInt(advancesCount));
		marketSummary.setDeclinesCount(Integer.parseInt(declinesCount));
		marketSummary.setUnchangedCount(Integer.parseInt(unchangedCount));
		marketSummary.setTradesCount(Integer.parseInt(tradesCount));
		marketSummary.setTotalForeignBuy(new BigDecimal(totalForeignBuy));
		marketSummary.setTotalForeignSell(new BigDecimal(totalForeignSell));
		report.setMarketSummary(marketSummary);
	}

	private void parsePseIndeces(PseReport report, String marketSummaryData) {
		List<PseIndex> indeces = new ArrayList<>();

		for (PseIndex.Type type : PseIndex.Type.values()) {
			String indexData = extractSummaryData(type.getRegex(), marketSummaryData);

			if (indexData == null) {
				continue;
			}

			String[] columns = indexData.split(" ");
			ArrayUtils.reverse(columns);
			PseIndex index = new PseIndex(type);
			boolean smeOrEtf = columns.length == 2;
			boolean pseiOrAll = columns.length == 6;
			PseIndexQuote indexQuote = new PseIndexQuote(report.getDate());

			if (smeOrEtf) {
				indexQuote.setVolume(Long.parseLong(columns[1]));
				indexQuote.setValue(new BigDecimal(columns[0]));
			} else if (pseiOrAll) {
				indexQuote.setOpen(new BigDecimal(columns[5]));
				indexQuote.setHigh(new BigDecimal(columns[4]));
				indexQuote.setLow(new BigDecimal(columns[3]));
				indexQuote.setClose(new BigDecimal(columns[2]));
				indexQuote.setPercentChange(parseValue(columns[1]));
				indexQuote.setPointChange(parseValue(columns[0]));
			} else {
				indexQuote.setOpen(new BigDecimal(columns[7]));
				indexQuote.setHigh(new BigDecimal(columns[6]));
				indexQuote.setLow(new BigDecimal(columns[5]));
				indexQuote.setClose(new BigDecimal(columns[4]));
				indexQuote.setPercentChange(parseValue(columns[3]));
				indexQuote.setPointChange(parseValue(columns[2]));
				indexQuote.setVolume(Long.parseLong(columns[1]));
				indexQuote.setValue(new BigDecimal(columns[0]));
			}

			indexQuote.setIndex(index);
			index.add(indexQuote);
			indeces.add(index);
		}

		report.setIndeces(indeces);
	}

	private void parseSuspendedStocks(PseReport report, String marketSummaryData) {
		String suspendedStocksData = extractSummaryData(SUSPENDED_STOCKS, marketSummaryData);
		String[] suspendedStocksRow = suspendedStocksData.split("\n");
		List<String> suspendedStocks = new ArrayList<>();

		for (String row : suspendedStocksRow) {
			String trimmedRow = row.trim();
			String[] columns = trimmedRow.split(" ");
			suspendedStocks.add(columns[columns.length - 1]);
		}

		report.setSuspendedStocks(suspendedStocks);
	}

	private String extractSummaryData(Pattern pattern, String value) {
		Matcher matcher = pattern.matcher(value);

		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	private BigDecimal parseBigDecimal(String value) {
		return "-".equals(value) ? null : new BigDecimal(value);
	}

	private Long parseLong(String value) {
		return "-".equals(value) ? null : Long.parseLong(value);
	}

	private BigDecimal parseValue(String value) {
		Matcher negativeValueMatcher = NEGATIVE_VALUE.matcher(value);

		if (negativeValueMatcher.find()) {
			String negativeValue = negativeValueMatcher.group(1);
			return new BigDecimal("-" + negativeValue);
		}

		Matcher positiveValueMatcher = POSITIVE_VALUE.matcher(value);

		if (positiveValueMatcher.find()) {
			String positiveValue = positiveValueMatcher.group();
			return new BigDecimal(positiveValue);
		}

		return null;
	}

	private String parseCompanyName(String[] columns) {
		String[] names = Arrays.copyOfRange(columns, 10, columns.length, String[].class);
		StringBuilder name = new StringBuilder();

		for (int i = 0; i < names.length; i++) {
			name.append(names[i]);

			if (i < names.length - 1) {
				name.append(" ");
			}
		}

		return WordUtils.capitalizeFully(name.toString());
	}

}
