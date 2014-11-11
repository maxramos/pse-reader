package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.ActionType;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.SentimentType;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.indicator.entity.ValueHolder;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the Slow Stochastic (SSto).
 *
 * Computations:
 * n = look-back period (14)
 * m = smoothing period (3)
 *
 * FAST_%K = (LAST_CLOSING - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
 * FAST_%D = SMAm(FAST_%K)
 *
 * SLOW_%K = FAST_%D
 * SLOW_%D = SMAm(SLOW_%K)
 *
 * Actions:
 * BUY --- %K < 20 && %D < 20 && %K > %D && %K > PREV_%K
 * HOLD --- %K < 80
 * SELL --- %K <= 100 && %D <= 100 && %K < %D && %K < PREV_%K
 * HOLD --- Everything Else
 */
public class SstoIndicator implements Callable<SstoResult> {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal BUY = new BigDecimal("20");
	private static final BigDecimal SELL = new BigDecimal("100");

	private static final int PERIOD = 14;
	private static final int SMOOTHING = 3;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;
	private SstoResult result;
	private TrendType trend;

	public SstoIndicator(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public SstoResult call() throws Exception {
		if (result == null) {
			result = _results.size() < 2 ? initialSsto() : succeedingSsto();
		} else {
			result = determineTradingSignal();
		}

		return result;
	}

	public void setTrend(TrendType trend) {
		this.trend = trend;
	}

	private SstoResult initialSsto() {
		int size = PERIOD + SMOOTHING * 2 - 1 - 1; // 18
		List<Quote> trimmedQuotes = _quotes.subList(0, size);

		List<SstoResult.Holder> kDataList = kData(trimmedQuotes);
		List<BigDecimal> fastKList = fastK(kDataList);
		List<BigDecimal> fastDList = IndicatorUtil.sma(fastKList, SMOOTHING, 2);

		BigDecimal slowK = fastDList.get(0);
		BigDecimal slowD = IndicatorUtil.avg(fastDList, 2);
		BigDecimal fastK = fastKList.get(0);

		return new SstoResult(slowK, slowD, fastK);
	}

	private SstoResult succeedingSsto() {
		int size = PERIOD;
		List<Quote> trimmedQuotes = _quotes.subList(0, size);
		Quote currentQuote = trimmedQuotes.get(0);
		SstoResult previous1SstoResult = _results.get(0).getSstoResult();
		SstoResult previous2SstoResult = _results.get(1).getSstoResult();

		BigDecimal fastK = fastK(currentQuote.getClose(), lowestLow(trimmedQuotes), highestHigh(trimmedQuotes));
		List<BigDecimal> fastKList = Arrays.asList(new BigDecimal[] { fastK, previous1SstoResult.getFastK(), previous2SstoResult.getFastK() });
		BigDecimal slowK = IndicatorUtil.avg(fastKList, 2);
		List<BigDecimal> fastDList = Arrays.asList(new BigDecimal[] { slowK, previous1SstoResult.getSlowK(), previous2SstoResult.getSlowK() });
		BigDecimal slowD = IndicatorUtil.avg(fastDList, 2);

		return new SstoResult(slowK, slowD, fastK);
	}

	private List<SstoResult.Holder> kData(List<Quote> quotes) {
		int size = SMOOTHING * 2 - 1;
		List<SstoResult.Holder> kDataList = new ArrayList<>(size);

		for (int i = 0, start = 0, end = PERIOD; i < size; i++, start++, end++) {
			List<Quote> period = quotes.subList(start, end);
			BigDecimal lastClosing = period.get(0).getClose();
			BigDecimal lowestLow = lowestLow(period);
			BigDecimal higestHigh = highestHigh(period);
			SstoResult.Holder data = new SstoResult.Holder(lastClosing, lowestLow, higestHigh);
			kDataList.add(data);
		}

		return kDataList;
	}

	private List<BigDecimal> fastK(List<SstoResult.Holder> dataList) {
		List<BigDecimal> fastKList = new ArrayList<>(dataList.size());

		for (SstoResult.Holder data : dataList) {
			BigDecimal lastClosing = data.getLastClosing();
			BigDecimal lowestLow = data.getLowestLow();
			BigDecimal highestHigh = data.getHighestHigh();
			BigDecimal fastK = fastK(lastClosing, lowestLow, highestHigh);
			fastKList.add(fastK);
		}

		return fastKList;
	}

	private BigDecimal fastK(BigDecimal lastClosing, BigDecimal lowestLow, BigDecimal highestHigh) {
		if (highestHigh.compareTo(lowestLow) == 0) {
			return BigDecimal.ZERO;
		}

		// (LAST_CLOSING - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
		return lastClosing.subtract(lowestLow).divide(highestHigh.subtract(lowestLow), 4, RoundingMode.HALF_UP).multiply(_100);
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

	private SstoResult determineTradingSignal() {
		BigDecimal k = result.getSlowK();
		BigDecimal d = result.getSlowD();
		ActionType action;

		if (trend == TrendType.UP) {
			if (k.compareTo(SELL) > 0 || d.compareTo(SELL) > 0) {
				action = ActionType.SELL;
			} else {
				action = ActionType.HOLD;
			}
		} else if (trend == TrendType.DOWN) {
			if (k.compareTo(BUY) < 0 || d.compareTo(BUY) < 0) {
				action = ActionType.BUY;
			} else {
				action = ActionType.HOLD;
			}
		} else {
			BigDecimal prevK = result.getSlowK();
			BigDecimal prevD = result.getSlowD();

			List<ValueHolder> sstoResults = new ArrayList<>();
			sstoResults.add(result);

			for (IndicatorResult _result : _results) {
				sstoResults.add(_result.getSstoResult());
			}

			SentimentType sentiment = IndicatorUtil.divergence(_quotes, sstoResults);

			if (sentiment == SentimentType.BULLISH && firstTrough(sstoResults).compareTo(BUY) < 0) {
				action = ActionType.BUY;
			} else if (sentiment == SentimentType.BEARISH && firstPeak(sstoResults).compareTo(SELL) > 0) {
				action = ActionType.SELL;
			} else if (prevK.compareTo(BUY) < 0 && k.compareTo(BUY) > 0 || prevD.compareTo(BUY) < 0 && d.compareTo(BUY) > 0) {
				action = ActionType.BUY;
			} else if (prevK.compareTo(SELL) > 0 && k.compareTo(SELL) < 0 || prevD.compareTo(SELL) > 0 && d.compareTo(SELL) < 0) {
				action = ActionType.SELL;
			} else if (prevK.compareTo(prevD) < 0 && k.compareTo(d) > 0) {
				action = ActionType.BUY;
			} else if (prevK.compareTo(prevD) > 0 && k.compareTo(d) < 0) {
				action = ActionType.SELL;
			} else {
				action = ActionType.HOLD;
			}
		}

		result.setAction(action);
		return result;
	}

	private BigDecimal firstTrough(List<ValueHolder> sstoResults) {
		return sstoResults.get(3).getValue();
	}

	private BigDecimal firstPeak(List<ValueHolder> sstoResults) {
		return sstoResults.get(3).getValue();
	}

}
