package ph.mar.psereader.business.report.boundary;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.operation.entity.Settings;
import ph.mar.psereader.business.report.control.PseReportReader;
import ph.mar.psereader.business.report.entity.PseIndex;
import ph.mar.psereader.business.report.entity.PseReport;
import ph.mar.psereader.business.report.entity.PseReportRow;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Startup
@Singleton
public class PseReportManager {

	@Inject
	Logger log;

	@Inject
	Repository repository;

	@Inject
	PseReportReader pseReportReader;

	private transient Map<String, Stock> tempStockMap;
	private transient Map<PseIndex.Type, PseIndex> tempPseIndexMap;

	public List<Date> findAllDates() {
		return repository.find(PseReport.ALL_DATES, Date.class);
	}

	public List<PseReportRow> findAllRowsByDate(Date date) {
		return repository.find(PseReportRow.BY_DATE, with("date", date).asParameters(), PseReportRow.class);
	}

	public Map<String, Integer> addAll(List<byte[]> files) {
		List<PseReport> addedReports = new ArrayList<>();
		tempStockMap = new TreeMap<>();
		tempPseIndexMap = new TreeMap<>();

		for (byte[] file : files) {
			PseReport report = pseReportReader.read(file);

			if (add(report)) {
				addedReports.add(report);
			}
		}

		List<Stock> stocks = new ArrayList<>(tempStockMap.values());
		int addedStocksCount = 0;

		for (Stock stock : stocks) {
			addedStocksCount += add(stock) ? 1 : 0;
		}

		List<PseIndex> pseIndeces = new ArrayList<>(tempPseIndexMap.values());
		int addedPseIndeces = 0;

		for (PseIndex index : pseIndeces) {
			addedPseIndeces += add(index) ? 1 : 0;
		}

		int newlySuspendedCount = updateSuspendedStocks(addedReports);

		Map<String, Integer> results = new HashMap<>();
		results.put("reports", addedReports.size());
		results.put("stocks", addedStocksCount);
		results.put("suspended", newlySuspendedCount);
		results.put("indeces", addedPseIndeces);
		return results;
	}

	public PseReport findLatestReport() {
		List<PseReport> reports = repository.find(PseReport.ALL, PseReport.class, 1);

		if (reports.isEmpty()) {
			return null;
		}

		return reports.get(0);
	}

	private boolean add(PseReport report) {
		if (!isExisting(report)) {
			repository.add(report);
			log.info("Report {} processed.", Quote.DATE_FORMAT.format(report.getDate()));
			processStocks(report);
			processPseIndeces(report.getIndeces());
			return true;
		}

		return false;
	}

	private boolean add(Stock stock) {
		if (stock.getId() == null) {
			repository.add(stock);
			log.info("Stock {} processed.", stock.getSymbol());
			return true;
		}

		repository.update(stock);
		return false;
	}

	private boolean add(PseIndex index) {
		if (index.getId() == null) {
			repository.add(index);
			log.info("Index {} processed.", index.getType().getName());
			return true;
		}

		repository.update(index);
		return false;
	}

	private int updateSuspendedStocks(List<PseReport> reports) {
		if (reports.isEmpty()) {
			return 0;
		}

		PseReport lastReport = reports.get(reports.size() - 1);
		List<Settings> settingsList = repository.find(Settings.ALL, Settings.class);
		Settings settings = settingsList.get(0);
		int newlySuspendedCount = 0;

		if (settings.getSuspensionDate().compareTo(lastReport.getDate()) <= 0) {
			List<Stock> stocks = repository.find(Stock.ALL, Stock.class);
			List<String> suspendedStocks = lastReport.getSuspendedStocks();
			settings.setSuspensionDate(lastReport.getDate());
			settings.setSuspendedStocks(suspendedStocks);

			for (Stock stock : stocks) {
				if (suspendedStocks.contains(stock.getSymbol())) {
					stock.setSuspended(true);
					newlySuspendedCount++;
				} else {
					stock.setSuspended(false);
				}
			}

			repository.flush();
		}

		return newlySuspendedCount;
	}

	private boolean isExisting(PseReport report) {
		List<PseReport> reports = repository.find(PseReport.BY_DATE, with("date", report.getDate()).asParameters(), PseReport.class);
		return !reports.isEmpty();
	}

	private void processStocks(PseReport report) {
		for (PseReportRow row : report.getRows()) {
			String symbol = row.getSymbol();
			Stock stock = tempStockMap.get(symbol);
			Quote quote = Quote.convert(row);

			if (stock == null) {
				List<Stock> stocks = repository.find(Stock.ALL_WITH_QUOTES_BY_SYMBOL, with("symbol", symbol).asParameters(), Stock.class);

				if (stocks.isEmpty()) {
					stock = Stock.convert(row);
				} else {
					stock = stocks.get(0);
					repository.detach(stock);
				}

				stock.add(quote);
				tempStockMap.put(stock.getSymbol(), stock);
			} else {
				stock.add(quote);
			}
		}
	}

	private void processPseIndeces(List<PseIndex> indeces) {
		for (PseIndex index : indeces) {
			PseIndex.Type type = index.getType();
			PseIndex tempIndex = tempPseIndexMap.get(type);

			if (tempIndex == null) {
				List<PseIndex> managedIndeces = repository.find(PseIndex.ALL_WITH_QUOTES_BY_TYPE, with("type", type).asParameters(), PseIndex.class);

				if (managedIndeces.isEmpty()) {
					tempIndex = index;
				} else {
					tempIndex = managedIndeces.get(0);
					repository.detach(tempIndex);
					tempIndex.addAll(index.getQuotes());
				}

				tempPseIndexMap.put(type, tempIndex);
			} else {
				tempIndex.addAll(index.getQuotes());
			}
		}
	}

}
