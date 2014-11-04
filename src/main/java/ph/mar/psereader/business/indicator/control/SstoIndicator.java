package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RecommendationType;
import ph.mar.psereader.business.indicator.entity.SstoResult;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the Slow Stochastic (SSto) indicator.
 *
 * Computations:
 * m = smoothing period
 * n = look-back period
 *
 * FAST_%K = (LAST_CLOSING - LOWEST_LOW(n)) / (HIGHEST_HIGH(n) - LOWEST_LOW(n)) * 100
 * FAST_%D = SMAm(FAST_%K)
 *
 * SLOW_%K = FAST_%D
 * SLOW_%D = SMAm(SLOW_%K)
 *
 * Recommendations:
 * MUST_BUY --- %K < 20 && %D < 20 && %K > %D && %K > PREV_%K
 * BUY --- %K < 20
 * BUY_WARNING --- %K <= 25 && %K < %D
 * HOLD --- %K <= 25
 * HOLD --- %K < 75
 * SELL_WARNING --- %K <= 80 && %K > %D
 * HOLD --- %K <= 80
 * MUST_SELL --- %K <= 100 && %D <= 100 && %K < %D && %K < PREV_%K
 * SELL --- %K <= 100
 * HOLD --- Everything Else
 */
@Singleton
public class SstoIndicator {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal BUY_CEILING = new BigDecimal("20");
	private static final BigDecimal HOLD_FLOOR = new BigDecimal("25");
	private static final BigDecimal HOLD_CEILING = new BigDecimal("75");
	private static final BigDecimal SELL_FLOOR = new BigDecimal("80");
	private static final BigDecimal SELL_CEILING = new BigDecimal("100");

	int lookBackPeriod = 14;
	int smaSmoothing = 3;

	@Asynchronous
	public Future<SstoResult> run(List<Quote> quotes, List<IndicatorResult> results) {
		return results.size() < 2 ? initialSsto(quotes) : succeedingSsto(quotes, results);
	}

	private Future<SstoResult> initialSsto(List<Quote> quotes) {
		int size = lookBackPeriod + smaSmoothing * 2 - 1 - 1; // 18
		List<Quote> trimmedQuotes = quotes.subList(0, size);
		List<SstoResult.Holder> kDataList = kData(trimmedQuotes);
		List<BigDecimal> fastKList = fastK(kDataList);
		List<BigDecimal> fastDList = IndicatorUtil.sma(fastKList, smaSmoothing, 2);

		BigDecimal slowK = fastDList.get(0);
		BigDecimal slowD = IndicatorUtil.avg(fastDList, 2);
		RecommendationType recommendation = determineRecommendation(slowK, slowD, null);
		BigDecimal fastK = fastKList.get(0);

		SstoResult result = new SstoResult(slowK, slowD, recommendation, fastK);
		return new AsyncResult<>(result);
	}

	private Future<SstoResult> succeedingSsto(List<Quote> quotes, List<IndicatorResult> results) {
		int size = lookBackPeriod;
		List<Quote> trimmedQuotes = quotes.subList(0, size);
		Quote currentQuote = trimmedQuotes.get(0);
		SstoResult previousSstoResult = results.get(0).getSstoResult();
		SstoResult previous2SstoResult = results.get(1).getSstoResult();

		BigDecimal fastK = fastK(currentQuote.getClose(), lowestLow(trimmedQuotes), highestHigh(trimmedQuotes));
		List<BigDecimal> fastKList = Arrays.asList(new BigDecimal[] { fastK, previousSstoResult.getFastK(), previous2SstoResult.getFastK() });
		BigDecimal slowK = IndicatorUtil.avg(fastKList, 2);
		List<BigDecimal> fastDList = Arrays.asList(new BigDecimal[] { slowK, previousSstoResult.getSlowK(), previous2SstoResult.getSlowK() });
		BigDecimal slowD = IndicatorUtil.avg(fastDList, 2);
		RecommendationType recommendation = determineRecommendation(slowK, slowD, previousSstoResult.getSlowK());

		SstoResult result = new SstoResult(slowK, slowD, recommendation, fastK);
		return new AsyncResult<>(result);
	}

	private List<SstoResult.Holder> kData(List<Quote> quotes) {
		int size = smaSmoothing * 2 - 1;
		List<SstoResult.Holder> kDataList = new ArrayList<>(size);

		for (int i = 0, start = 0, end = lookBackPeriod; i < size; i++, start++, end++) {
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

	private RecommendationType determineRecommendation(BigDecimal k, BigDecimal d, BigDecimal prevK) {
		RecommendationType recommendation;

		if (k.compareTo(BUY_CEILING) < 0) {
			if (d.compareTo(BUY_CEILING) < 0 && k.compareTo(d) > 0 && prevK != null && k.compareTo(prevK) > 0) {
				recommendation = RecommendationType.MUST_BUY; // %K < 20 && %D < 20 && %K > %D && %K > PREV_%K
			} else {
				recommendation = RecommendationType.BUY; // %K < 20
			}
		} else if (k.compareTo(HOLD_FLOOR) <= 0) {
			if (k.compareTo(d) < 0) {
				recommendation = RecommendationType.BUY_WARNING; // %K <= 25 && %K < %D
			} else {
				recommendation = RecommendationType.HOLD; // %K <= 25
			}
		} else if (k.compareTo(HOLD_CEILING) < 0) {
			recommendation = RecommendationType.HOLD; // %K < 75
		} else if (k.compareTo(SELL_FLOOR) <= 0) {
			if (k.compareTo(d) > 0) {
				recommendation = RecommendationType.SELL_WARNING; // %K <= 80 && %K > %D
			} else {
				recommendation = RecommendationType.HOLD; // %K <= 80
			}
		} else if (k.compareTo(SELL_CEILING) <= 0) {
			if (d.compareTo(SELL_CEILING) <= 0 && k.compareTo(d) < 0 && prevK != null && k.compareTo(prevK) < 0) {
				recommendation = RecommendationType.MUST_SELL; // %K <= 100 && %D <= 100 && %K < %D && %K < PREV_%K
			} else {
				recommendation = RecommendationType.SELL; // %K <= 100
			}
		} else {
			recommendation = RecommendationType.HOLD;
		}

		return recommendation;
	}

}
