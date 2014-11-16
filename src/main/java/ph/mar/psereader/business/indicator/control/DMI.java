package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class DMI implements Callable<DmiResult> {

	private static final int PERIOD = 8;
	private static final BigDecimal _100 = new BigDecimal("100");

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public DMI(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public DmiResult call() throws Exception {
		return _results.isEmpty() ? initialDmi() : succeedingDmi();
	}

	private DmiResult initialDmi() {
		int size = PERIOD + 1 + PERIOD - 1;
		List<Quote> period = _quotes.subList(0, size);
		DmiResult.Holder trsAndDms = trsAndDms(period, size - 1);
		DmiResult.SmoothedHolder atrsAndSmoothedDmsAndDisAndDxs = atrsAndSmoothedDmsAndDisAndDxs(trsAndDms);

		BigDecimal atr = atrsAndSmoothedDmsAndDisAndDxs.getLastAtr();
		BigDecimal smoothedPlusDm = atrsAndSmoothedDmsAndDisAndDxs.getLastSmoothedPlusDm();
		BigDecimal smoothedMinusDm = atrsAndSmoothedDmsAndDisAndDxs.getLastSmoothedMinusDm();
		BigDecimal plusDi = atrsAndSmoothedDmsAndDisAndDxs.getLastPlusDi();
		BigDecimal minusDi = atrsAndSmoothedDmsAndDisAndDxs.getLastMinusDi();
		BigDecimal adx = IndicatorUtil.avg(atrsAndSmoothedDmsAndDisAndDxs.getDxs(), 10);
		return new DmiResult(adx, plusDi, minusDi, smoothedPlusDm, smoothedMinusDm, atr);
	}

	private DmiResult succeedingDmi() {
		List<Quote> period = _quotes.subList(0, 2);
		DmiResult previousDmiResult = _results.get(0).getDmiResult();
		DmiResult.Holder trsAndDms = trsAndDms(period, 1);
		BigDecimal previousAtr = previousDmiResult.getAtr();
		BigDecimal previousSmoothedPlusDm = previousDmiResult.getSmoothedPlusDm();
		BigDecimal previousSmoothedMinusDm = previousDmiResult.getSmoothedMinusDm();
		BigDecimal previousAdx = previousDmiResult.getAdx();
		BigDecimal tr = trsAndDms.getTrs().get(0);
		BigDecimal plusDm = trsAndDms.getPlusDms().get(0);
		BigDecimal minusDm = trsAndDms.getMinusDms().get(0);

		BigDecimal atr = wildersMa(tr, previousAtr, PERIOD, 10);
		BigDecimal smoothedPlusDm = wildersMa(plusDm, previousSmoothedPlusDm, PERIOD, 10);
		BigDecimal smoothedMinusDm = wildersMa(minusDm, previousSmoothedMinusDm, PERIOD, 10);
		BigDecimal plusDi = di(smoothedPlusDm, atr);
		BigDecimal minusDi = di(smoothedMinusDm, atr);
		BigDecimal adx = adx(plusDi, minusDi, previousAdx);
		return new DmiResult(adx, plusDi, minusDi, smoothedPlusDm, smoothedMinusDm, atr);
	}

	private DmiResult.Holder trsAndDms(List<Quote> quotes, int period) {
		DmiResult.Holder trsAndDms = new DmiResult.Holder(period);

		for (int i = 0, current = 0, previous = 1; i < period; i++, previous++, current++) {
			Quote quote = quotes.get(current);
			Quote previousQuote = quotes.get(previous);

			BigDecimal high = quote.getHigh();
			BigDecimal low = quote.getLow();
			BigDecimal previousClose = previousQuote.getClose();
			BigDecimal previousHigh = previousQuote.getHigh();
			BigDecimal previousLow = previousQuote.getLow();

			BigDecimal hl = high.subtract(low);
			BigDecimal hpc = high.subtract(previousClose);
			BigDecimal pcl = previousClose.subtract(low);
			BigDecimal upMove = high.subtract(previousHigh);
			BigDecimal downMove = previousLow.subtract(low);

			BigDecimal tr = hl.max(hpc).max(pcl);
			BigDecimal plusDm = upMove.compareTo(downMove) > 0 && upMove.compareTo(BigDecimal.ZERO) > 0 ? upMove : BigDecimal.ZERO;
			BigDecimal minusDm = downMove.compareTo(upMove) > 0 && downMove.compareTo(BigDecimal.ZERO) > 0 ? downMove : BigDecimal.ZERO;
			trsAndDms.add(tr, plusDm, minusDm);
		}

		return trsAndDms;
	}

	private DmiResult.SmoothedHolder atrsAndSmoothedDmsAndDisAndDxs(DmiResult.Holder trsAndDms) {
		trsAndDms.reverse();
		DmiResult.SmoothedHolder atrsAndSmoothedDmsAndDisAndDxs = new DmiResult.SmoothedHolder(PERIOD);

		for (int i = 0, start = 0, end = PERIOD; i < PERIOD; i++, start++, end++) {
			List<BigDecimal> trs = trsAndDms.getTrs().subList(start, end);
			List<BigDecimal> plusDms = trsAndDms.getPlusDms().subList(start, end);
			List<BigDecimal> minusDms = trsAndDms.getMinusDms().subList(start, end);
			BigDecimal atr;
			BigDecimal smoothedPlusDm;
			BigDecimal smoothedMinusDm;

			if (i == 0) {
				atr = IndicatorUtil.avg(trs, 10);
				smoothedPlusDm = IndicatorUtil.avg(plusDms, 10);
				smoothedMinusDm = IndicatorUtil.avg(minusDms, 10);
			} else {
				BigDecimal tr = trs.get(PERIOD - 1);
				BigDecimal previousAtr = atrsAndSmoothedDmsAndDisAndDxs.getLastAtr();
				atr = wildersMa(tr, previousAtr, PERIOD, 10);

				BigDecimal plusDm = plusDms.get(PERIOD - 1);
				BigDecimal previousSmoothedPlusDm = atrsAndSmoothedDmsAndDisAndDxs.getLastSmoothedPlusDm();
				smoothedPlusDm = wildersMa(plusDm, previousSmoothedPlusDm, PERIOD, 10);

				BigDecimal minusDm = minusDms.get(PERIOD - 1);
				BigDecimal previousSmoothedMinusDm = atrsAndSmoothedDmsAndDisAndDxs.getLastSmoothedMinusDm();
				smoothedMinusDm = wildersMa(minusDm, previousSmoothedMinusDm, PERIOD, 10);
			}

			BigDecimal plusDi = di(smoothedPlusDm, atr);
			BigDecimal minusDi = di(smoothedMinusDm, atr);
			BigDecimal dx = dx(plusDi, minusDi);
			atrsAndSmoothedDmsAndDisAndDxs.add(atr, smoothedPlusDm, smoothedMinusDm, plusDi, minusDi, dx);
		}

		return atrsAndSmoothedDmsAndDisAndDxs;
	}

	private BigDecimal di(BigDecimal smoothedDm, BigDecimal atr) {
		// +DI = SMA(+DM) / ATR * 100
		// -DI = SMA(-DM) / ATR * 100
		return smoothedDm.divide(atr, 12, RoundingMode.HALF_UP).multiply(_100);
	}

	private BigDecimal adx(BigDecimal plusDi, BigDecimal minusDi, BigDecimal previousAdx) {
		BigDecimal dx = dx(plusDi, minusDi);
		return wildersMa(dx, previousAdx, PERIOD, 10);
	}

	private BigDecimal dx(BigDecimal plusDi, BigDecimal minusDi) {
		// ABS(+DI - -DI) / (+DI + -DI) * 100
		return plusDi.subtract(minusDi).abs().divide(plusDi.add(minusDi), 12, RoundingMode.HALF_UP).multiply(_100);
	}

	private BigDecimal wildersMa(BigDecimal currentVal, BigDecimal previousAvg, int period, int decimalPlaces) {
		// (PREV_AVG * (PERIOD - 1) + CURRENT_VAL) / PERIOD
		BigDecimal _period = new BigDecimal(period);
		return previousAvg.multiply(_period.subtract(BigDecimal.ONE)).add(currentVal).divide(_period, decimalPlaces, RoundingMode.HALF_UP);
	}

}
