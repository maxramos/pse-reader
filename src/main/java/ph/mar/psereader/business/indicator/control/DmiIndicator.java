package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.PositionType;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the Directional Movement Index (DMI) with Average Directional Index (ADX).
 *
 * Computations:
 * n = look-back period (14)
 * TR_CANDIDATE_1 = CURRENT_HIGH - CURRENT_LOW
 * TR_CANDIDATE_2 = ABS(CURRENT_HIGH - PREVIOUS_CLOSE)
 * TR_CANDIDATE_3 = ABS(CURRENT_LOW - PREVIOUS_CLOSE)
 * UP_MOVE = CURRENT_HIGH - PREVIOUS_HIGH
 * DOWN_MOVE - PREVIOUS_LOW - CURRENT_LOW
 * TR = MAX(TR_CANDIDATE_1, TR_CANDIDATE_2, TR_CANDIDATE_2)
 * SMA = (PREV_AVG * (PERIOD - 1) + CURRENT_VAL) / PERIOD
 *
 * ATR = SMAn(TR)
 * +DM = UP_MOVE > DOWN_MOVE && UP_MOVE > 0 ? UP_MOVE : 0
 * -DM = DOWN_MOVE > UP_MOVE && DOWN_MOVE > 0 ? DOWN_MOVE : 0
 * +DI = SMAn(+DM) / ATR * 100
 * -DI = SMAn(-DM) / ATR * 100
 * DX = ABS(+DI - -DI) / (+DI + -DI) * 100
 * ADX = SMAn(DX)
 *
 * Trends:
 * STRONG_UP --- ADX > 40 && +DI > -DI
 * STRONG_DOWN --- ADX > 40 && -DI > +DI
 * UP --- ADX > 20 && +DI > -DI
 * DOWN --- ADX > 20 && -DI > +DI
 * SIDEWAYS --- ADX <= 20
 *
 * Positions:
 * ENTER --- PREV_-DI > PREV_+DI && +DI > -D
 * EXIT --- PREV_+DI > PREV_-DI && -DI > +DI
 * ENTER_WARNING --- PREV_-DI > PREV_+DI && PREV_-DI > -DI && PREV_+DI < +DI && -DI - +DI <= 5
 * EXIT_WARNING --- PREV_+DI > PREV_-DI && PREV_+DI > +DI && PREV_-DI < -DI && +DI - -DI <= 5
 * RISING --- +DI > PREV_+DI
 * FALLING --- +DI < PREV_+DI
 * HOLD --- Initial State
 */
public class DmiIndicator implements Callable<DmiResult> {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal TREND_WARNING = new BigDecimal("5");
	private static final BigDecimal TREND_SIGNAL = new BigDecimal("20");
	private static final BigDecimal STRONG_TREND_SIGNAL = new BigDecimal("40");

	private static final int LOOK_BACK_PERIOD = 14;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public DmiIndicator(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public DmiResult call() throws Exception {
		return _results.isEmpty() ? initialDmi(_quotes) : succeedingDmi(_quotes, _results);
	}

	private DmiResult initialDmi(List<Quote> quotes) {
		int size = LOOK_BACK_PERIOD + 1 + LOOK_BACK_PERIOD - 1; // 28
		List<Quote> trimmedQuotes = quotes.subList(0, size);

		DmiResult.Holder trAndDmDataList = trAndDmData(trimmedQuotes, size - 1);
		DmiResult.SmoothedHolder adxDataList = adxData(trAndDmDataList);
		BigDecimal adx = IndicatorUtil.avg(adxDataList.getDxList(), 10);
		BigDecimal plusDi = adxDataList.getLastPlusDi();
		BigDecimal minusDi = adxDataList.getLastMinusDi();
		TrendType trend = determineTrend(adx, plusDi, minusDi);
		PositionType position = PositionType.HOLD;
		BigDecimal smoothedPlusDm = adxDataList.getLastSmoothedPlusDm();
		BigDecimal smoothedMinusDm = adxDataList.getLastSmoothedMinusDm();
		BigDecimal atr = adxDataList.getLastAtr();

		DmiResult result = new DmiResult(adx, plusDi, minusDi, trend, position, smoothedPlusDm, smoothedMinusDm, atr);
		return result;
	}

	private DmiResult succeedingDmi(List<Quote> quotes, List<IndicatorResult> results) {
		int size = 2;
		List<Quote> trimmedQuotes = quotes.subList(0, size);
		DmiResult previousDmiResult = results.get(0).getDmiResult();
		BigDecimal previousAdx = previousDmiResult.getAdx();
		BigDecimal previousPlusDi = previousDmiResult.getPlusDi();
		BigDecimal previousMinusDi = previousDmiResult.getMinusDi();
		BigDecimal previousSmoothedPlusDm = previousDmiResult.getSmoothedPlusDm();
		BigDecimal previousSmoothedMinusDm = previousDmiResult.getSmoothedMinusDm();
		BigDecimal previousAtr = previousDmiResult.getAtr();

		DmiResult.Holder trAndDmDataList = trAndDmData(trimmedQuotes, 1);
		BigDecimal currentTr = trAndDmDataList.getTrList().get(0);
		BigDecimal currentPlusDm = trAndDmDataList.getPlusDmList().get(0);
		BigDecimal currentMinusDm = trAndDmDataList.getMinusDmList().get(0);
		BigDecimal period = new BigDecimal(LOOK_BACK_PERIOD);

		BigDecimal atr = IndicatorUtil.sma(previousAtr, currentTr, period, 10);
		BigDecimal smoothedPlusDm = IndicatorUtil.sma(previousSmoothedPlusDm, currentPlusDm, period, 10);
		BigDecimal smoothedMinusDm = IndicatorUtil.sma(previousSmoothedMinusDm, currentMinusDm, period, 10);
		BigDecimal plusDi = di(smoothedPlusDm, atr);
		BigDecimal minusDi = di(smoothedMinusDm, atr);
		BigDecimal dx = dx(plusDi, minusDi);
		BigDecimal adx = IndicatorUtil.sma(previousAdx, dx, period, 10);
		TrendType trend = determineTrend(adx, plusDi, minusDi);
		PositionType position = determinePosition(plusDi, minusDi, previousPlusDi, previousMinusDi);

		DmiResult result = new DmiResult(adx, plusDi, minusDi, trend, position, smoothedPlusDm, smoothedMinusDm, atr);
		return result;
	}

	private DmiResult.Holder trAndDmData(List<Quote> quotes, int period) {
		DmiResult.Holder trAndDmDataList = new DmiResult.Holder(period);

		for (int i = 0, current = 0, previous = 1; i < period; i++, previous++, current++) {
			Quote currentQuote = quotes.get(current);
			Quote previousQuote = quotes.get(previous);

			BigDecimal currentHigh = currentQuote.getHigh();
			BigDecimal currentLow = currentQuote.getLow();
			BigDecimal previousClose = previousQuote.getClose();
			BigDecimal previousHigh = previousQuote.getHigh();
			BigDecimal previousLow = previousQuote.getLow();

			BigDecimal trCandidate1 = currentHigh.subtract(currentLow);
			BigDecimal trCandidate2 = currentHigh.subtract(previousClose).abs();
			BigDecimal trCandidate3 = currentLow.subtract(previousClose).abs();
			BigDecimal upMove = currentHigh.subtract(previousHigh);
			BigDecimal downMove = previousLow.subtract(currentLow);

			BigDecimal tr = trCandidate1.max(trCandidate2).max(trCandidate3);

			// +DM = UP_MOVE > DOWN_MOVE && UP_MOVE > 0 ? UP_MOVE : 0
			BigDecimal plusDm = upMove.compareTo(downMove) > 0 && upMove.compareTo(BigDecimal.ZERO) > 0 ? upMove : BigDecimal.ZERO;

			// -DM = DOWN_MOVE > UP_MOVE && DOWN_MOVE > 0 ? DOWN_MOVE : 0
			BigDecimal minusDm = downMove.compareTo(upMove) > 0 && downMove.compareTo(BigDecimal.ZERO) > 0 ? downMove : BigDecimal.ZERO;

			trAndDmDataList.add(tr, plusDm, minusDm);
		}

		return trAndDmDataList;
	}

	private DmiResult.SmoothedHolder adxData(DmiResult.Holder dataList) {
		dataList.reverse();
		DmiResult.SmoothedHolder adxDataList = new DmiResult.SmoothedHolder(LOOK_BACK_PERIOD);
		BigDecimal period = new BigDecimal(LOOK_BACK_PERIOD);

		for (int i = 0, start = 0, end = LOOK_BACK_PERIOD; i < LOOK_BACK_PERIOD; i++, start++, end++) {
			List<BigDecimal> trList = dataList.getTrList().subList(start, end);
			List<BigDecimal> plusDmList = dataList.getPlusDmList().subList(start, end);
			List<BigDecimal> minusDmList = dataList.getMinusDmList().subList(start, end);
			BigDecimal atr;
			BigDecimal smoothedPlusDm;
			BigDecimal smoothedMinusDm;

			if (i == 0) {
				atr = IndicatorUtil.avg(trList, 10);
				smoothedPlusDm = IndicatorUtil.avg(plusDmList, 10);
				smoothedMinusDm = IndicatorUtil.avg(minusDmList, 10);
			} else {
				BigDecimal currentTr = trList.get(LOOK_BACK_PERIOD - 1);
				BigDecimal previousAtr = adxDataList.getLastAtr();
				atr = IndicatorUtil.sma(previousAtr, currentTr, period, 10);

				BigDecimal currentPlusDm = plusDmList.get(LOOK_BACK_PERIOD - 1);
				BigDecimal previousSmoothedPlusDm = adxDataList.getLastSmoothedPlusDm();
				smoothedPlusDm = IndicatorUtil.sma(previousSmoothedPlusDm, currentPlusDm, period, 10);

				BigDecimal currentMinusDm = minusDmList.get(LOOK_BACK_PERIOD - 1);
				BigDecimal previousSmoothedMinusDm = adxDataList.getLastSmoothedMinusDm();
				smoothedMinusDm = IndicatorUtil.sma(previousSmoothedMinusDm, currentMinusDm, period, 10);
			}

			BigDecimal plusDi = di(smoothedPlusDm, atr);
			BigDecimal minusDi = di(smoothedMinusDm, atr);
			BigDecimal dx = dx(plusDi, minusDi);
			adxDataList.add(atr, smoothedPlusDm, smoothedMinusDm, plusDi, minusDi, dx);
		}

		return adxDataList;
	}

	private BigDecimal di(BigDecimal smoothedDm, BigDecimal atr) {
		// +DI = SMA(+DM) / ATR * 100
		// -DI = SMA(-DM) / ATR * 100
		return smoothedDm.divide(atr, 12, RoundingMode.HALF_UP).multiply(_100);
	}

	private BigDecimal dx(BigDecimal plusDi, BigDecimal minusDi) {
		// ABS(+DI - -DI) / (+DI + -DI) * 100
		return plusDi.subtract(minusDi).abs().divide(plusDi.add(minusDi), 12, RoundingMode.HALF_UP).multiply(_100);
	}

	private TrendType determineTrend(BigDecimal adx, BigDecimal plusDi, BigDecimal minusDi) {
		BigDecimal _adx = adx.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _plusDi = plusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _minusDi = minusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		TrendType trend;

		if (_adx.compareTo(STRONG_TREND_SIGNAL) > 0) {
			// STRONG_UP_TREND --- ADX > 40 && +DI > -DI
			// STRONG_DOWN_TREND --- ADX > 40 && -DI > +DI
			trend = _plusDi.compareTo(_minusDi) > 0 ? TrendType.STRONG_UP : TrendType.STRONG_DOWN;
		} else if (_adx.compareTo(TREND_SIGNAL) > 0) {
			// UP_TREND --- ADX > 20 && +DI > -DI
			// DOWN_TREND --- ADX > 20 && -DI > +DI
			trend = _plusDi.compareTo(_minusDi) > 0 ? TrendType.UP : TrendType.DOWN;
		} else {
			// SIDEWAYS --- ADX <= 20
			trend = TrendType.SIDEWAYS;
		}

		return trend;
	}

	private PositionType determinePosition(BigDecimal plusDi, BigDecimal minusDi, BigDecimal prevPlusDi, BigDecimal prevMinusDi) {
		BigDecimal _plusDi = plusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _minusDi = minusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _prevPlusDi = prevPlusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _prevMinusDi = prevMinusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		PositionType position;

		if (_prevMinusDi.compareTo(_prevPlusDi) > 0 && _plusDi.compareTo(_minusDi) > 0) {
			position = PositionType.ENTER; // PREV_-DI > PREV_+DI && +DI > -DI
		} else if (_prevPlusDi.compareTo(_prevMinusDi) > 0 && _minusDi.compareTo(_plusDi) > 0) {
			position = PositionType.EXIT; // PREV_+DI > PREV_-DI && -DI > +DI
		} else if (_minusDi.compareTo(_plusDi) > 0 && _prevMinusDi.compareTo(_minusDi) > 0 && _prevPlusDi.compareTo(_plusDi) < 0
				&& _minusDi.subtract(_plusDi).compareTo(TREND_WARNING) <= 0) {
			position = PositionType.ENTER_WARNING; // PREV_-DI > PREV_+DI && PREV_-DI > -DI && PREV_+DI < +DI && -DI - +DI <= 5
		} else if (_plusDi.compareTo(_minusDi) > 0 && _prevPlusDi.compareTo(_plusDi) > 0 && _prevMinusDi.compareTo(_minusDi) < 0
				&& _plusDi.subtract(_minusDi).compareTo(TREND_WARNING) <= 0) {
			position = PositionType.EXIT_WARNING; // PREV_+DI > PREV_-DI && PREV_+DI > +DI && PREV_-DI < -DI && +DI - -DI <= 5
		} else if (_plusDi.compareTo(_prevPlusDi) > 0) {
			position = PositionType.RISING; // +DI > PREV_+DI
		} else if (_plusDi.compareTo(_prevPlusDi) < 0) {
			position = PositionType.FALLING; // +DI < PREV_+DI
		} else {
			position = PositionType.HOLD;
		}

		return position;
	}

}
