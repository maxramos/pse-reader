package ph.mar.psereader.business.indicator.control;

import static ph.mar.psereader.business.repository.control.QueryParameter.with;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RecommendationType;
import ph.mar.psereader.business.indicator.entity.RsiResult;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

/**
 * This implements the Relative Strength Index (RSI) indicator with SMA as smoothing.
 *
 * Computations:
 * PERIOD = look-back period
 *
 * initial AVG_GAIN = AVG(GAIN)
 * initial AVG_LOSS = AVG(LOSS)
 * succeeding AVG_GAIN = (PREV_AVG_GAIN * (PERIOD - 1) + CURRENT_GAIN) / PERIOD
 * succeeding AVG_LOSS = (PREV_AVG_LOSS * (PERIOD - 1) + CURRENT_LOSS) / PERIOD
 *
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
@Singleton
public class RsiIndicator {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal BUY_CEILING = new BigDecimal("30");
	private static final BigDecimal HOLD_FLOOR = new BigDecimal("35");
	private static final BigDecimal HOLD_CEILING = new BigDecimal("65");
	private static final BigDecimal SELL_FLOOR = new BigDecimal("70");
	private static final BigDecimal SELL_CEILING = new BigDecimal("100");

	@Inject
	Logger log;

	@Inject
	Repository repository;

	int lookBackPeriod = 14;

	@Asynchronous
	public Future<RsiResult> run(Stock stock, Date date) {
		return stock.getIndicatorResults().isEmpty() ? initialRsi(stock, date) : succeedingRsi(stock, date);
	}

	private Future<RsiResult> initialRsi(Stock stock, Date date) {
		int minSize = lookBackPeriod + 1;
		List<Quote> quotes = repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(),
				Quote.class, minSize);

		if (quotes.size() < minSize) {
			throw new IndicatorException(String.format("Not enough quotes: %s for %s.", quotes.size(), stock.getSymbol()));
		}

		Quote currentQuote = quotes.get(0);

		if (date.compareTo(currentQuote.getDate()) != 0) {
			throw new IndicatorException(String.format("No quote for date: %s for %s.", Quote.DATE_FORMAT.format(date), stock.getSymbol()));
		}

		RsiResult.Holder gainsAndLosses = gainsAndLosses(quotes, lookBackPeriod);

		BigDecimal avgGain = IndicatorUtil.avg(gainsAndLosses.getGains(), 10);
		BigDecimal avgLoss = IndicatorUtil.avg(gainsAndLosses.getLosses(), 10);
		BigDecimal rsi = rsi(avgGain, avgLoss);
		RecommendationType recommendation = determineRecommendation(rsi, null);
		RsiResult result = new RsiResult(rsi, recommendation, avgGain, avgLoss);
		return new AsyncResult<>(result);
	}

	private Future<RsiResult> succeedingRsi(Stock stock, Date date) {
		List<Quote> quotes = repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(),
				Quote.class, 2);
		Quote currentQuote = quotes.get(0);

		if (date.compareTo(currentQuote.getDate()) != 0) {
			throw new IndicatorException(String.format("No quote for date: %s for %s.", Quote.DATE_FORMAT.format(date), stock.getSymbol()));
		}

		// Quick fix for issue with @Order By and Join Fetch.
		List<RsiResult> rsiResults = repository.find(IndicatorResult.ALL_RSI_RESULTS_BY_STOCK, with("stock", stock).asParameters(), RsiResult.class,
				1);
		RsiResult previousRsiResult = rsiResults.get(0);
		BigDecimal previousRsi = previousRsiResult.getRsi();
		BigDecimal previousAvgGain = previousRsiResult.getAvgGain();
		BigDecimal previousAvgLoss = previousRsiResult.getAvgLoss();

		RsiResult.Holder gainsAndLosses = gainsAndLosses(quotes, 1);
		BigDecimal currentGain = gainsAndLosses.getGains().get(0);
		BigDecimal currentLoss = gainsAndLosses.getLosses().get(0);
		BigDecimal period = new BigDecimal(lookBackPeriod);

		BigDecimal avgGain = IndicatorUtil.ema(previousAvgGain, currentGain, period, 10);
		BigDecimal avgLoss = IndicatorUtil.ema(previousAvgLoss, currentLoss, period, 10);
		BigDecimal rsi = rsi(avgGain, avgLoss);
		RecommendationType recommendation = determineRecommendation(rsi, previousRsi);
		RsiResult result = new RsiResult(rsi, recommendation, avgGain, avgLoss);
		return new AsyncResult<>(result);
	}

	private RsiResult.Holder gainsAndLosses(List<Quote> quotes, int period) {
		RsiResult.Holder gainsAndLosses = new RsiResult.Holder(period);

		for (int i = 0, current = 0, previous = 1; i < period; i++, previous++, current++) {
			BigDecimal currentClose = quotes.get(current).getClose();
			BigDecimal previousClose = quotes.get(previous).getClose();
			BigDecimal gain;
			BigDecimal loss;

			if (currentClose.compareTo(previousClose) > 0) {
				gain = currentClose.subtract(previousClose);
				loss = BigDecimal.ZERO;
			} else {
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
