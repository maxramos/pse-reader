package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.FstoResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RiskType;
import ph.mar.psereader.business.stock.entity.Quote;

public class FSTO implements Callable<FstoResult> {

	private static final int PERIOD = 21;
	private static final int SMOOTHING = 3;
	private static final BigDecimal _100 = new BigDecimal("100");

	private static final BigDecimal OVERSOLD_LEVEL = new BigDecimal("30");
	private static final BigDecimal OVERBOUGHT_LEVEL = new BigDecimal("70");;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	private RiskType risk;

	public FSTO(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public FstoResult call() throws Exception {
		return _results.size() < 2 ? initialFsto() : succeedingFsto();
	}

	public RiskType getRisk() {
		return risk;
	}

	private FstoResult initialFsto() {
		int size = PERIOD + SMOOTHING - 1;
		List<Quote> period = _quotes.subList(0, size);
		List<BigDecimal> fastKs = fastKs(period);

		BigDecimal fastK = fastKs.get(0);
		BigDecimal fastD = IndicatorUtil.avg(fastKs, 2);
		FstoResult result = new FstoResult(fastK, fastD);

		determineRisk(fastK);

		return result;
	}

	private FstoResult succeedingFsto() {
		List<Quote> period = _quotes.subList(0, PERIOD);
		List<IndicatorResult> results = _results.subList(0, 2);
		List<BigDecimal> fastKs = fastKs(period, results);

		BigDecimal fastK = fastKs.get(0);
		BigDecimal fastD = IndicatorUtil.avg(fastKs, 2);
		FstoResult result = new FstoResult(fastK, fastD);

		determineRisk(fastK);

		return result;
	}

	private List<BigDecimal> fastKs(List<Quote> quotes) {
		List<BigDecimal> fastKs = new ArrayList<>(SMOOTHING);

		for (int i = 0, start = 0, end = PERIOD; i < SMOOTHING; i++, start++, end++) {
			List<Quote> period = quotes.subList(start, end);
			BigDecimal close = period.get(0).getClose();
			BigDecimal lowestLow = lowestLow(period);
			BigDecimal highestHigh = highestHigh(period);
			BigDecimal fastK = fastK(close, lowestLow, highestHigh);
			fastKs.add(fastK);
		}

		return fastKs;
	}

	private List<BigDecimal> fastKs(List<Quote> quotes, List<IndicatorResult> results) {
		BigDecimal close = quotes.get(0).getClose();
		BigDecimal fastK = fastK(close, lowestLow(quotes), highestHigh(quotes));
		BigDecimal previousFastK1 = results.get(0).getFstoResult().getFastK();
		BigDecimal previousFastK2 = results.get(1).getFstoResult().getFastK();
		return Arrays.asList(new BigDecimal[] { fastK, previousFastK1, previousFastK2 });
	}

	private BigDecimal fastK(BigDecimal close, BigDecimal lowestLow, BigDecimal highestHigh) {
		if (highestHigh.compareTo(lowestLow) == 0) {
			return BigDecimal.ZERO;
		}

		// (CLOSE - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
		return close.subtract(lowestLow).divide(highestHigh.subtract(lowestLow), 4, RoundingMode.HALF_UP).multiply(_100);
	}

	private BigDecimal lowestLow(List<Quote> quotes) {
		BigDecimal lowestLow = quotes.get(0).getLow();

		for (int i = 1; i < quotes.size(); i++) {
			if (quotes.get(i).getLow().compareTo(lowestLow) < 0) {
				lowestLow = quotes.get(i).getLow();
			}
		}

		return lowestLow;
	}

	private BigDecimal highestHigh(List<Quote> quotes) {
		BigDecimal highestHigh = quotes.get(0).getHigh();

		for (int i = 1; i < quotes.size(); i++) {
			if (quotes.get(i).getHigh().compareTo(highestHigh) > 0) {
				highestHigh = quotes.get(i).getHigh();
			}
		}

		return highestHigh;
	}

	private void determineRisk(BigDecimal fastK) {
		if (fastK.compareTo(OVERBOUGHT_LEVEL) > 0) {
			risk = RiskType.HIGH;
		} else if (fastK.compareTo(OVERSOLD_LEVEL) > 0) {
			risk = RiskType.MODERATE;
		} else {
			risk = RiskType.LOW;
		}
	}

}
