package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.EmaResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class EMA implements Callable<EmaResult> {

	private static final int PERIOD = 21;
	private static final BigDecimal FACTOR = new BigDecimal("2").divide(new BigDecimal(PERIOD).add(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

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
		return new EmaResult(ema);
	}

	private EmaResult succeedingEma() {
		BigDecimal close = _quotes.get(0).getClose();
		BigDecimal previousEma = _results.get(0).getEmaResult().getEma();

		BigDecimal ema = ema(close, previousEma, FACTOR, 10);
		return new EmaResult(ema);
	}

	private List<BigDecimal> extractCloses(List<Quote> quotes) {
		List<BigDecimal> closes = new ArrayList<>(quotes.size());

		for (Quote quote : quotes) {
			closes.add(quote.getClose());
		}

		return closes;
	}

	private BigDecimal ema(BigDecimal currentVal, BigDecimal previousAvg, BigDecimal factor, int decimalPlaces) {
		// (CURRENT_VAL - PREV_AVG) * FACTOR + PREV_AVG
		BigDecimal ema = currentVal.subtract(previousAvg).multiply(factor).add(previousAvg);
		return ema.divide(BigDecimal.ONE, decimalPlaces, RoundingMode.HALF_UP);
	}

}
