package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.MacdResult;
import ph.mar.psereader.business.indicator.entity.PositionType;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

/**
 * This implements the Moving Average Convergence/Divergence (MACD).
 */
public class MacdIndicator implements Callable<MacdResult> {

	private static final int FAST_EMA_LOOK_BACK_PERIOD = 12;
	private static final int SLOW_EMA_LOOK_BACK_PERIOD = 26;
	private static final int SIGNAL_LINE_LOOK_BACK_PERIOD = 9;
	private static final BigDecimal TWO = new BigDecimal("2");
	private static final BigDecimal FAST_EMA_FACTOR = TWO.divide(new BigDecimal(FAST_EMA_LOOK_BACK_PERIOD).add(BigDecimal.ONE), 10,
			RoundingMode.HALF_UP);
	private static final BigDecimal SLOW_EMA_FACTOR = TWO.divide(new BigDecimal(SLOW_EMA_LOOK_BACK_PERIOD).add(BigDecimal.ONE), 10,
			RoundingMode.HALF_UP);
	private static final BigDecimal SIGNAL_LINE_EMA_FACTOR = TWO.divide(new BigDecimal(SIGNAL_LINE_LOOK_BACK_PERIOD).add(BigDecimal.ONE), 10,
			RoundingMode.HALF_UP);

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;
	Stock stock;

	public MacdIndicator(Stock stock, List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
		this.stock = stock;
	}

	@Override
	public MacdResult call() throws Exception {
		return _results.isEmpty() ? initialMacd(_quotes) : succeedingMacd(_quotes, _results);
	}

	private MacdResult initialMacd(List<Quote> quotes) {
		int size = SLOW_EMA_LOOK_BACK_PERIOD + SIGNAL_LINE_LOOK_BACK_PERIOD - 1; // 34
		List<Quote> trimmedQuotes = quotes.subList(0, size);
		List<BigDecimal> closeList = extractCloseValues(trimmedQuotes);
		Collections.reverse(closeList);
		int fastEmaStartIndex = SLOW_EMA_LOOK_BACK_PERIOD - FAST_EMA_LOOK_BACK_PERIOD; // 14

		List<BigDecimal> fastEmaList = IndicatorUtil.ema(closeList, FAST_EMA_LOOK_BACK_PERIOD, FAST_EMA_FACTOR, 10);
		List<BigDecimal> slowEmaList = IndicatorUtil.ema(closeList, SLOW_EMA_LOOK_BACK_PERIOD, SLOW_EMA_FACTOR, 10);
		List<BigDecimal> macdList = macd(fastEmaList, slowEmaList, fastEmaStartIndex);

		BigDecimal signalLine = IndicatorUtil.avg(macdList, 10);
		BigDecimal macd = macdList.get(macdList.size() - 1);
		BigDecimal histogram = macd.subtract(signalLine);
		TrendType trend = determineTrend(histogram);
		PositionType position = PositionType.HOLD;
		BigDecimal fastEma = fastEmaList.get(fastEmaList.size() - 1);
		BigDecimal slowEma = slowEmaList.get(slowEmaList.size() - 1);

		return new MacdResult(macd, signalLine, histogram, trend, position, fastEma, slowEma);
	}

	private MacdResult succeedingMacd(List<Quote> quotes, List<IndicatorResult> results) {
		Quote currentQuote = quotes.get(0);
		MacdResult previousMacdResult = results.get(0).getMacdResult();
		BigDecimal previousFastEma = previousMacdResult.getFastEma();
		BigDecimal previousSlowEma = previousMacdResult.getSlowEma();
		BigDecimal previousSignalLine = previousMacdResult.getSignalLine();
		BigDecimal previousHistogram = previousMacdResult.getHistogram();
		BigDecimal currentClose = currentQuote.getClose();

		BigDecimal fastEma = IndicatorUtil.ema(previousFastEma, currentClose, FAST_EMA_FACTOR, 10);
		BigDecimal slowEma = IndicatorUtil.ema(previousSlowEma, currentClose, SLOW_EMA_FACTOR, 10);
		BigDecimal macd = macd(fastEma, slowEma);
		BigDecimal signalLine = IndicatorUtil.ema(previousSignalLine, macd, SIGNAL_LINE_EMA_FACTOR, 10);
		BigDecimal histogram = macd.subtract(signalLine);
		TrendType trend = determineTrend(histogram);
		PositionType position = determinePosition(histogram, previousHistogram);

		return new MacdResult(macd, signalLine, histogram, trend, position, fastEma, slowEma);
	}

	private List<BigDecimal> extractCloseValues(List<Quote> quotes) {
		List<BigDecimal> closeList = new ArrayList<>(quotes.size());

		for (Quote quote : quotes) {
			closeList.add(quote.getClose());
		}

		return closeList;
	}

	private List<BigDecimal> macd(List<BigDecimal> fastEmaList, List<BigDecimal> slowEmaList, int fastEmaStartIndex) {
		List<BigDecimal> macdList = new ArrayList<>(slowEmaList.size());

		for (int fastIndex = fastEmaStartIndex, slowIndex = 0; slowIndex < slowEmaList.size(); fastIndex++, slowIndex++) {
			BigDecimal macd = macd(fastEmaList.get(fastIndex), slowEmaList.get(slowIndex));
			macdList.add(macd);
		}

		return macdList;
	}

	private BigDecimal macd(BigDecimal fastEma, BigDecimal slowEma) {
		return fastEma.subtract(slowEma);
	}

	private TrendType determineTrend(BigDecimal histogram) {
		TrendType trend;

		if (histogram.compareTo(BigDecimal.ZERO) > 0) {
			trend = TrendType.UP;
		} else if (histogram.compareTo(BigDecimal.ZERO) < 0) {
			trend = TrendType.DOWN;
		} else {
			trend = TrendType.SIDEWAYS;
		}

		return trend;
	}

	private PositionType determinePosition(BigDecimal histogram, BigDecimal prevHistogram) {
		PositionType position;

		if (prevHistogram.compareTo(BigDecimal.ZERO) < 0 && histogram.compareTo(BigDecimal.ZERO) > 0) {
			position = PositionType.ENTER;
		} else if (prevHistogram.compareTo(BigDecimal.ZERO) > 0 && histogram.compareTo(BigDecimal.ZERO) < 0) {
			position = PositionType.EXIT;
		} else if (prevHistogram.compareTo(BigDecimal.ZERO) > 0 && histogram.compareTo(prevHistogram) > 0) {
			position = PositionType.RISING;
		} else if (prevHistogram.compareTo(BigDecimal.ZERO) < 0 && histogram.compareTo(prevHistogram) < 0) {
			position = PositionType.FALLING;
		} else {
			position = PositionType.HOLD;
		}

		return position;
	}

}
