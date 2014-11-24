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
		List<Quote> quotes = _quotes.subList(0, 5);
		List<IndicatorResult> results = _results.subList(0, 4);
		Map<String, List<BigDecimal>> ohlc = extractOhlcs(quotes);
		List<BigDecimal> opens = ohlc.get("open");
		List<BigDecimal> highs = ohlc.get("high");
		List<BigDecimal> lows = ohlc.get("low");
		List<BigDecimal> closes = ohlc.get("close");
		List<BigDecimal> emas = extractEmas(ema, results);

		if (higher(lows)) {
			if (above(opens, closes, emas)) {

			} else if (below(opens, closes, emas)) {

			} else {

			}
		} else if (lower(highs)) {
			if (above(opens, closes, emas)) {

			} else if (below(opens, closes, emas)) {

			} else {

			}
		} else {
			if (above(opens, closes, emas)) {

			} else if (below(opens, closes, emas)) {

			} else {

			}
		}
	}

	private boolean higher(List<BigDecimal> lows) {
		return gt(0, 2, lows) && gt(2, 4, lows);
	}

	private boolean lower(List<BigDecimal> highs) {
		return lt(0, 2, highs) && lt(2, 4, highs);
	}

	private boolean above(List<BigDecimal> opens, List<BigDecimal> closes, List<BigDecimal> emas) {
		for (int i = 0; i < emas.size(); i++) {
			if (gt(i, opens, emas) && gt(i, closes, emas)) {
				continue;
			}

			return false;
		}

		return true;
	}

	private boolean below(List<BigDecimal> opens, List<BigDecimal> closes, List<BigDecimal> emas) {
		for (int i = 0; i < emas.size(); i++) {
			if (lt(i, opens, emas) && lt(i, closes, emas)) {
				continue;
			}

			return false;
		}

		return true;
	}

	private boolean gt(int current, int previous, List<BigDecimal> values) {
		return values.get(current).compareTo(values.get(previous)) > 0;
	}

	private boolean gt(int index, List<BigDecimal> values1, List<BigDecimal> values2) {
		return values1.get(index).compareTo(values2.get(index)) > 0;
	}

	private boolean lt(int current, int previous, List<BigDecimal> values) {
		return values.get(current).compareTo(values.get(previous)) < 0;
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

	private Map<String, List<BigDecimal>> extractOhlcs(List<Quote> quotes) {
		List<BigDecimal> opens = new ArrayList<>(quotes.size());
		List<BigDecimal> highs = new ArrayList<>(quotes.size());
		List<BigDecimal> lows = new ArrayList<>(quotes.size());
		List<BigDecimal> closes = new ArrayList<>(quotes.size());

		for (Quote quote : quotes) {
			opens.add(quote.getOpen());
			highs.add(quote.getHigh());
			lows.add(quote.getLow());
			closes.add(quote.getClose());
		}

		Map<String, List<BigDecimal>> ohlc = new HashMap<>(4);
		ohlc.put("open", opens);
		ohlc.put("high", highs);
		ohlc.put("low", lows);
		ohlc.put("close", closes);
		return ohlc;
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
