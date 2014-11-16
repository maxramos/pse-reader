package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class SSTO implements Callable<SstoResult> {

	private static final int PERIOD = 5;
	private static final int SMOOTHING = 3;
	private static final BigDecimal _100 = new BigDecimal("100");

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public SSTO(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public SstoResult call() throws Exception {
		return _results.size() < 2 ? initialSsto() : succeedingSsto();
	}

	private SstoResult initialSsto() {
		int size = PERIOD + SMOOTHING * 2 - 1 - 1;
		List<Quote> period = _quotes.subList(0, size);
		List<BigDecimal> fastKs = fastKs(period);
		List<BigDecimal> slowKs = sma(fastKs, SMOOTHING, 2);

		BigDecimal slowK = slowKs.get(0);
		BigDecimal slowD = IndicatorUtil.avg(slowKs, 2);
		BigDecimal fastK = fastKs.get(0);
		return new SstoResult(slowK, slowD, fastK);
	}

	private SstoResult succeedingSsto() {
		List<Quote> period = _quotes.subList(0, PERIOD);
		List<IndicatorResult> results = _results.subList(0, 2);
		List<BigDecimal> fastKs = fastKs(period, results);
		List<BigDecimal> slowKs = slowKs(fastKs, results);

		BigDecimal slowK = slowKs.get(0);
		BigDecimal slowD = IndicatorUtil.avg(slowKs, 2);
		BigDecimal fastK = fastKs.get(0);
		return new SstoResult(slowK, slowD, fastK);
	}

	private List<BigDecimal> fastKs(List<Quote> quotes) {
		int size = SMOOTHING * 2 - 1;
		List<BigDecimal> fastKs = new ArrayList<>(size);

		for (int i = 0, start = 0, end = PERIOD; i < size; i++, start++, end++) {
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
		BigDecimal previousFastK1 = results.get(0).getSstoResult().getFastK();
		BigDecimal previousFastK2 = results.get(1).getSstoResult().getFastK();
		return Arrays.asList(new BigDecimal[] { fastK, previousFastK1, previousFastK2 });
	}

	private BigDecimal fastK(BigDecimal close, BigDecimal lowestLow, BigDecimal highestHigh) {
		if (highestHigh.compareTo(lowestLow) == 0) {
			return BigDecimal.ZERO;
		}

		// (CLOSE - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
		return close.subtract(lowestLow).divide(highestHigh.subtract(lowestLow), 4, RoundingMode.HALF_UP).multiply(_100);
	}

	private List<BigDecimal> slowKs(List<BigDecimal> fastKs, List<IndicatorResult> results) {
		BigDecimal slowK = IndicatorUtil.avg(fastKs, 2);
		BigDecimal previousSlowK1 = results.get(0).getSstoResult().getSlowK();
		BigDecimal previousSlowK2 = results.get(1).getSstoResult().getSlowK();
		return Arrays.asList(new BigDecimal[] { slowK, previousSlowK1, previousSlowK2 });
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

	private List<BigDecimal> sma(List<BigDecimal> values, int smoothing, int decimalPlaces) {
		if (smoothing >= values.size()) {
			return null;
		}

		List<BigDecimal> smas = new ArrayList<>(smoothing);

		for (int i = 0, start = 0, end = smoothing; i < smoothing; i++, start++, end++) {
			BigDecimal avg = IndicatorUtil.avg(values.subList(start, end), decimalPlaces);
			smas.add(avg);
		}

		return smas;
	}

}
