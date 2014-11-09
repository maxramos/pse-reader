package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RecommendationType;
import ph.mar.psereader.business.indicator.entity.RsiResult;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the Relative Strength Index (RSI).
 *
 * Computations:
 * n = look-back period (14)
 * if CURRENT_CLOSE > PREVIOUS_CLOSE then GAIN = CURRENT_CLOSE - PREVIOUS_CLOSE, LOSS = 0
 * if PREVIOUS_CLOSE < CURRENT_CLOSE then GAIN = 0, LOSS = PREVIOUS_CLOSE - CURRENT_CLOSE
 * SMA = (PREV_AVG * (PERIOD - 1) + CURRENT_VAL) / PERIOD
 *
 * AVG_GAIN = SMAn(GAIN)
 * AVG_LOSS = SMAn(LOSS)
 * RS = AVG_GAIN / AVG_LOSS
 * RSI = 100 - 100 / (RS + 1)
 *
 * Recommendations:
 * MUST_BUY --- RSI < 30 && PREV_RSI < 30 && RSI > PREV_RSI
 * BUY --- RSI < 30
 * BUY_WARNING --- RSI <= 35 && RSI < PREV_RSI
 * HOLD --- RSI <= 35
 * HOLD --- RSI < 65
 * SELL_WARNING --- RSI <= 70 && RSI > PREV_RSI
 * HOLD --- RSI <= 70
 * MUST_SELL --- RSI <= 100 && PREV_RSI <= 100 && RSI < PREV_RSI
 * SELL --- RSI <= 100
 * HOLD --- Everything Else
 */
public class RsiIndicator implements Callable<RsiResult> {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal BUY_CEILING = new BigDecimal("30");
	private static final BigDecimal HOLD_FLOOR = new BigDecimal("35");
	private static final BigDecimal HOLD_CEILING = new BigDecimal("65");
	private static final BigDecimal SELL_FLOOR = new BigDecimal("70");
	private static final BigDecimal SELL_CEILING = new BigDecimal("100");

	private static final int LOOK_BACK_PERIOD = 14;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public RsiIndicator(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public RsiResult call() throws Exception {
		return _results.isEmpty() ? initialRsi(_quotes) : succeedingRsi(_quotes, _results);
	}

	private RsiResult initialRsi(List<Quote> quotes) {
		int size = LOOK_BACK_PERIOD + 1; // 15
		List<Quote> trimmedQuotes = quotes.subList(0, size);

		RsiResult.Holder gainsAndLosses = gainsAndLosses(trimmedQuotes, LOOK_BACK_PERIOD);
		BigDecimal avgGain = IndicatorUtil.avg(gainsAndLosses.getGains(), 10);
		BigDecimal avgLoss = IndicatorUtil.avg(gainsAndLosses.getLosses(), 10);
		BigDecimal rsi = rsi(avgGain, avgLoss);
		RecommendationType recommendation = determineRecommendation(rsi, null);

		RsiResult result = new RsiResult(rsi, recommendation, avgGain, avgLoss);
		return result;
	}

	private RsiResult succeedingRsi(List<Quote> quotes, List<IndicatorResult> results) {
		int size = 2;
		List<Quote> trimmedQuotes = quotes.subList(0, size);
		RsiResult previousRsiResult = results.get(0).getRsiResult();
		BigDecimal previousRsi = previousRsiResult.getRsi();
		BigDecimal previousAvgGain = previousRsiResult.getAvgGain();
		BigDecimal previousAvgLoss = previousRsiResult.getAvgLoss();

		RsiResult.Holder gainsAndLosses = gainsAndLosses(trimmedQuotes, 1);
		BigDecimal currentGain = gainsAndLosses.getGains().get(0);
		BigDecimal currentLoss = gainsAndLosses.getLosses().get(0);
		BigDecimal period = new BigDecimal(LOOK_BACK_PERIOD);

		BigDecimal avgGain = IndicatorUtil.sma(previousAvgGain, currentGain, period, 10);
		BigDecimal avgLoss = IndicatorUtil.sma(previousAvgLoss, currentLoss, period, 10);
		BigDecimal rsi = rsi(avgGain, avgLoss);
		RecommendationType recommendation = determineRecommendation(rsi, previousRsi);

		RsiResult result = new RsiResult(rsi, recommendation, avgGain, avgLoss);
		return result;
	}

	private RsiResult.Holder gainsAndLosses(List<Quote> quotes, int period) {
		RsiResult.Holder gainsAndLosses = new RsiResult.Holder(period);

		for (int i = 0, current = 0, previous = 1; i < period; i++, previous++, current++) {
			BigDecimal currentClose = quotes.get(current).getClose();
			BigDecimal previousClose = quotes.get(previous).getClose();
			BigDecimal gain;
			BigDecimal loss;

			if (currentClose.compareTo(previousClose) > 0) {
				// if CURRENT_CLOSE > PREVIOUS_CLOSE then GAIN = CURRENT_CLOSE - PREVIOUS_CLOSE, LOSS = 0
				gain = currentClose.subtract(previousClose);
				loss = BigDecimal.ZERO;
			} else {
				// if PREVIOUS_CLOSE < CURRENT_CLOSE then GAIN = 0, LOSS = PREVIOUS_CLOSE - CURRENT_CLOSE
				loss = previousClose.subtract(currentClose);
				gain = BigDecimal.ZERO;
			}

			gainsAndLosses.add(gain, loss);
		}

		return gainsAndLosses;
	}

	private BigDecimal rsi(BigDecimal avgGain, BigDecimal avgLoss) {
		if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
			return _100;
		}

		// AVG_GAIN / AVG_LOSS
		BigDecimal rs = avgGain.divide(avgLoss, 10, RoundingMode.HALF_UP);

		// 100 - 100 / (RS + 1)
		return _100.subtract(_100.divide(rs.add(BigDecimal.ONE), 2, RoundingMode.HALF_UP));
	}

	private RecommendationType determineRecommendation(BigDecimal rsi, BigDecimal prevRsi) {
		RecommendationType recommendation;

		if (rsi.compareTo(BUY_CEILING) < 0) {
			if (prevRsi != null && prevRsi.compareTo(BUY_CEILING) < 0 && rsi.compareTo(prevRsi) > 0) {
				recommendation = RecommendationType.MUST_BUY; // RSI < 30 && PREV_RSI < 30 && RSI > PREV_RSI
			} else {
				recommendation = RecommendationType.BUY; // RSI < 30
			}
		} else if (rsi.compareTo(HOLD_FLOOR) <= 0) {
			if (prevRsi != null && rsi.compareTo(prevRsi) < 0) {
				recommendation = RecommendationType.BUY_WARNING; // RSI <= 35 && RSI < PREV_RSI
			} else {
				recommendation = RecommendationType.HOLD; // RSI <= 35
			}
		} else if (rsi.compareTo(HOLD_CEILING) < 0) {
			recommendation = RecommendationType.HOLD; // RSI < 65
		} else if (rsi.compareTo(SELL_FLOOR) <= 0) {
			if (prevRsi != null && rsi.compareTo(prevRsi) > 0) {
				recommendation = RecommendationType.SELL_WARNING; // RSI <= 70 && RSI > PREV_RSI
			} else {
				recommendation = RecommendationType.HOLD; // RSI <= 70
			}
		} else if (rsi.compareTo(SELL_CEILING) <= 0) {
			if (prevRsi != null && prevRsi.compareTo(SELL_CEILING) <= 0 && rsi.compareTo(prevRsi) < 0) {
				recommendation = RecommendationType.MUST_SELL; // RSI <= 100 && PREV_RSI <= 100 && RSI < PREV_RSI
			} else {
				recommendation = RecommendationType.SELL; // RSI <= 100
			}
		} else {
			recommendation = RecommendationType.HOLD;
		}

		return recommendation;
	}

}
