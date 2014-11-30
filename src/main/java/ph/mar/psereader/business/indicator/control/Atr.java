package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.AtrResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class Atr implements Callable<AtrResult> {

	private static final int PERIOD = 8;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public Atr(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public AtrResult call() throws Exception {
		return _results.isEmpty() ? initialAtr() : succeedingAtr();
	}

	private AtrResult initialAtr() {
		int size = PERIOD + 1 + PERIOD - 1;
		List<Quote> period = _quotes.subList(0, size);
		List<BigDecimal> trs = trs(period, size - 1);

		BigDecimal atr = IndicatorUtil.avg(trs, 10);
		return new AtrResult(atr);
	}

	private AtrResult succeedingAtr() {
		List<Quote> period = _quotes.subList(0, 2);
		AtrResult previousAtrResult = _results.get(0).getAtrResult();
		List<BigDecimal> trs = trs(period, 1);
		BigDecimal tr = trs.get(0);
		BigDecimal previousAtr = previousAtrResult.getAtr();

		BigDecimal atr = wildersMa(tr, previousAtr, PERIOD, 10);
		return new AtrResult(atr);
	}

	private List<BigDecimal> trs(List<Quote> quotes, int period) {
		List<BigDecimal> trs = new ArrayList<>(period);

		for (int i = 0, current = 0, previous = 1; i < period; i++, previous++, current++) {
			Quote quote = quotes.get(current);
			Quote previousQuote = quotes.get(previous);

			BigDecimal high = quote.getHigh();
			BigDecimal low = quote.getLow();
			BigDecimal previousClose = previousQuote.getClose();

			BigDecimal hl = high.subtract(low);
			BigDecimal hpc = high.subtract(previousClose);
			BigDecimal pcl = previousClose.subtract(low);

			BigDecimal tr = hl.max(hpc).max(pcl);
			trs.add(tr);
		}

		return trs;
	}

	private BigDecimal wildersMa(BigDecimal currentVal, BigDecimal previousAvg, int period, int decimalPlaces) {
		// (PREV_AVG * (PERIOD - 1) + CURRENT_VAL) / PERIOD
		BigDecimal _period = new BigDecimal(period);
		return previousAvg.multiply(_period.subtract(BigDecimal.ONE)).add(currentVal).divide(_period, decimalPlaces, RoundingMode.HALF_UP);
	}

}
