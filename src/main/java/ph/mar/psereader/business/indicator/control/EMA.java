package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.EmaResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.RecommendationType;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.stock.entity.Quote;

public class EMA implements Callable<EmaResult> {

	private static final int PERIOD = 21;
	private static final BigDecimal FACTOR = new BigDecimal("2").divide(new BigDecimal(PERIOD).add(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

	private static final BigDecimal SPIKE_LEVEL = new BigDecimal("0.20");

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	private TrendType trend;
	private RecommendationType recommendation;

	public EMA(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public EmaResult call() throws Exception {
		return _results.isEmpty() ? initialEma() : succeedingEma();
	}

	public TrendType getTrend() {
		return trend;
	}

	public RecommendationType getRecommendation() {
		return recommendation;
	}

	private EmaResult initialEma() {
		List<Quote> period = _quotes.subList(0, PERIOD);
		List<BigDecimal> closes = extractCloses(period);

		BigDecimal ema = IndicatorUtil.avg(closes, 10);
		EmaResult result = new EmaResult(ema);

		determineTrendAndRecommendation(ema);

		return result;
	}

	private EmaResult succeedingEma() {
		BigDecimal close = _quotes.get(0).getClose();
		BigDecimal previousEma = _results.get(0).getEmaResult().getEma();

		BigDecimal ema = ema(close, previousEma, FACTOR, 10);
		EmaResult result = new EmaResult(ema);

		determineTrendAndRecommendation(ema);

		return result;
	}

	private BigDecimal ema(BigDecimal currentVal, BigDecimal previousAvg, BigDecimal factor, int decimalPlaces) {
		// (CURRENT_VAL - PREVIOUS_AVG) * FACTOR + PREVIOUS_AVG
		BigDecimal ema = currentVal.subtract(previousAvg).multiply(factor).add(previousAvg);
		return ema.divide(BigDecimal.ONE, decimalPlaces, RoundingMode.HALF_UP);
	}

	private void determineTrendAndRecommendation(BigDecimal ema) {
		if (_results.size() < 4) {
			trend = TrendType.SIDEWAYS;
			recommendation = RecommendationType.HOLD;
			return;
		}

		List<Quote> quotes = _quotes.subList(0, 5);
		Map<String, List<BigDecimal>> highsAndLows = extractHighsAndLows(quotes);
		List<BigDecimal> highs = highsAndLows.get("high");
		List<BigDecimal> lows = highsAndLows.get("low");

		List<IndicatorResult> results = _results.subList(0, 4);
		List<BigDecimal> emas = extractEmas(ema, results);

		BigDecimal price = quotes.get(0).getClose();
		BigDecimal previousPrice = quotes.get(1).getClose();

		if (above(lows, emas)) {
			if (higher(lows)) {
				trend = TrendType.STRONG_UP;
				recommendation = spike(price, previousPrice) ? RecommendationType.TAKE_PROFIT : RecommendationType.BUY;
			} else {
				trend = TrendType.UP;
				recommendation = spike(price, previousPrice) ? RecommendationType.TAKE_PROFIT : RecommendationType.HOLD;
			}
		} else if (below(highs, emas)) {
			if (lower(highs)) {
				trend = TrendType.STRONG_DOWN;
				recommendation = RecommendationType.SELL;
			} else {
				trend = TrendType.DOWN;
				recommendation = higher(lows) ? RecommendationType.SELL_INTO_STRENGTH : RecommendationType.SELL;
			}
		} else {
			trend = TrendType.SIDEWAYS;

			if (higher(lows)) {
				recommendation = spike(price, previousPrice) ? RecommendationType.TAKE_PROFIT : RecommendationType.RANGE_TRADE;
			} else if (lower(highs)) {
				recommendation = RecommendationType.LIGHTEN;
			} else {
				recommendation = RecommendationType.HOLD;
			}
		}
	}

	private boolean above(List<BigDecimal> lows, List<BigDecimal> emas) {
		for (int i = 0; i < emas.size(); i++) {
			if (gt(i, lows, emas)) {
				continue;
			}

			return false;
		}

		return true;
	}

	private boolean below(List<BigDecimal> highs, List<BigDecimal> emas) {
		for (int i = 0; i < emas.size(); i++) {
			if (lt(i, highs, emas)) {
				continue;
			}

			return false;
		}

		return true;
	}

	private boolean higher(List<BigDecimal> lows) {
		return gt(0, 2, lows) && gt(2, 4, lows);
	}

	private boolean lower(List<BigDecimal> highs) {
		return lt(0, 2, highs) && lt(2, 4, highs);
	}

	private boolean spike(BigDecimal price, BigDecimal previousPrice) {
		BigDecimal priceChange = price.subtract(previousPrice);
		BigDecimal pricePercentChange = priceChange.divide(previousPrice, 4, RoundingMode.HALF_UP);
		return pricePercentChange.compareTo(SPIKE_LEVEL) >= 0;
	}

	private boolean gt(int index1, int index2, List<BigDecimal> values) {
		return values.get(index1).compareTo(values.get(index2)) > 0;
	}

	private boolean gt(int index, List<BigDecimal> values1, List<BigDecimal> values2) {
		return values1.get(index).compareTo(values2.get(index)) > 0;
	}

	private boolean lt(int index1, int index2, List<BigDecimal> values) {
		return values.get(index1).compareTo(values.get(index2)) < 0;
	}

	private boolean lt(int index, List<BigDecimal> values1, List<BigDecimal> values2) {
		return values1.get(index).compareTo(values2.get(index)) < 0;
	}

	private List<BigDecimal> extractCloses(List<Quote> quotes) {
		List<BigDecimal> closes = new ArrayList<>(quotes.size());

		for (Quote quote : quotes) {
			closes.add(quote.getClose());
		}

		return closes;
	}

	private Map<String, List<BigDecimal>> extractHighsAndLows(List<Quote> quotes) {
		List<BigDecimal> highs = new ArrayList<>(quotes.size());
		List<BigDecimal> lows = new ArrayList<>(quotes.size());

		for (Quote quote : quotes) {
			highs.add(quote.getHigh());
			lows.add(quote.getLow());
		}

		Map<String, List<BigDecimal>> highsAndLows = new HashMap<>(4);
		highsAndLows.put("high", highs);
		highsAndLows.put("low", lows);
		return highsAndLows;
	}

	private List<BigDecimal> extractEmas(BigDecimal ema, List<IndicatorResult> results) {
		List<BigDecimal> emas = new ArrayList<>(results.size() + 1);
		emas.add(ema);

		for (IndicatorResult result : results) {
			emas.add(result.getEmaResult().getEma());
		}

		return emas;
	}

}
