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

import ph.mar.psereader.business.indicator.entity.DmiResult;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.PositionType;
import ph.mar.psereader.business.indicator.entity.TrendType;
import ph.mar.psereader.business.repository.control.Repository;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Singleton
public class DmiIndicator {

	private static final BigDecimal _100 = new BigDecimal("100");
	private static final BigDecimal TREND_WARNING = new BigDecimal("5");
	private static final BigDecimal TREND_SIGNAL = new BigDecimal("20");
	private static final BigDecimal STRONG_TREND_SIGNAL = new BigDecimal("40");

	@Inject
	Logger log;

	@Inject
	Repository repository;

	int lookBackPeriod = 14;

	@Asynchronous
	public Future<DmiResult> run(Stock stock, Date date) {
		return stock.getIndicatorResults().isEmpty() ? initialDmi(stock, date) : succeedingDmi(stock, date);
	}

	private Future<DmiResult> initialDmi(Stock stock, Date date) {
		int minSize = lookBackPeriod + 1 + lookBackPeriod - 1;
		List<Quote> quotes = repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(),
				Quote.class, minSize);

		if (quotes.size() < minSize) {
			throw new IndicatorException(String.format("Not enough quotes: %s for %s.", quotes.size(), stock.getSymbol()));
		}

		Quote currentQuote = quotes.get(0);

		if (date.compareTo(currentQuote.getDate()) != 0) {
			throw new IndicatorException(String.format("No quote for date: %s for %s.", Quote.DATE_FORMAT.format(date), stock.getSymbol()));
		}

		DmiResult.Holder trAndDmDataList = trAndDmData(quotes, minSize - 1);
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
		return new AsyncResult<>(result);
	}

	private Future<DmiResult> succeedingDmi(Stock stock, Date date) {
		List<Quote> quotes = repository.find(Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, with("stock", stock).and("date", date).asParameters(),
				Quote.class, 2);
		Quote currentQuote = quotes.get(0);

		if (date.compareTo(currentQuote.getDate()) != 0) {
			throw new IndicatorException(String.format("No quote for date: %s for %s.", Quote.DATE_FORMAT.format(date), stock.getSymbol()));
		}

		List<DmiResult> dmiResults = repository.find(IndicatorResult.ALL_DMI_RESULTS_BY_STOCK, with("stock", stock).asParameters(), DmiResult.class,
				1);
		DmiResult previousDmiResult = dmiResults.get(0);
		BigDecimal previousAdx = previousDmiResult.getAdx();
		BigDecimal previousPlusDi = previousDmiResult.getPlusDi();
		BigDecimal previousMinusDi = previousDmiResult.getMinusDi();
		BigDecimal previousSmoothedPlusDm = previousDmiResult.getSmoothedPlusDm();
		BigDecimal previousSmoothedMinusDm = previousDmiResult.getSmoothedMinusDm();
		BigDecimal previousAtr = previousDmiResult.getAtr();

		DmiResult.Holder trAndDmDataList = trAndDmData(quotes, 1);
		BigDecimal currentTr = trAndDmDataList.getTrList().get(0);
		BigDecimal currentPlusDm = trAndDmDataList.getPlusDmList().get(0);
		BigDecimal currentMinusDm = trAndDmDataList.getMinusDmList().get(0);
		BigDecimal period = new BigDecimal(lookBackPeriod);

		BigDecimal atr = IndicatorUtil.ema(previousAtr, currentTr, period, 10);
		BigDecimal smoothedPlusDm = IndicatorUtil.ema(previousSmoothedPlusDm, currentPlusDm, period, 10);
		BigDecimal smoothedMinusDm = IndicatorUtil.ema(previousSmoothedMinusDm, currentMinusDm, period, 10);
		BigDecimal plusDi = di(smoothedPlusDm, atr);
		BigDecimal minusDi = di(smoothedMinusDm, atr);
		BigDecimal dx = dx(plusDi, minusDi);
		BigDecimal adx = IndicatorUtil.ema(previousAdx, dx, period, 10);
		TrendType trend = determineTrend(adx, plusDi, minusDi);
		PositionType position = determinePosition(plusDi, minusDi, previousPlusDi, previousMinusDi);

		DmiResult result = new DmiResult(adx, plusDi, minusDi, trend, position, smoothedPlusDm, smoothedMinusDm, atr);
		return new AsyncResult<>(result);
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
			BigDecimal plusDm = upMove.compareTo(downMove) > 0 && upMove.compareTo(BigDecimal.ZERO) > 0 ? upMove : BigDecimal.ZERO;
			BigDecimal minusDm = downMove.compareTo(upMove) > 0 && downMove.compareTo(BigDecimal.ZERO) > 0 ? downMove : BigDecimal.ZERO;

			trAndDmDataList.add(tr, plusDm, minusDm);
		}

		return trAndDmDataList;
	}

	private DmiResult.SmoothedHolder adxData(DmiResult.Holder dataList) {
		dataList.reverse();
		DmiResult.SmoothedHolder adxDataList = new DmiResult.SmoothedHolder(lookBackPeriod);
		BigDecimal period = new BigDecimal(lookBackPeriod);

		for (int i = 0, start = 0, end = lookBackPeriod; i < lookBackPeriod; i++, start++, end++) {
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
				BigDecimal currentTr = trList.get(lookBackPeriod - 1);
				BigDecimal previousAtr = adxDataList.getLastAtr();
				atr = IndicatorUtil.ema(previousAtr, currentTr, period, 10);

				BigDecimal currentPlusDm = plusDmList.get(lookBackPeriod - 1);
				BigDecimal previousSmoothedPlusDm = adxDataList.getLastSmoothedPlusDm();
				smoothedPlusDm = IndicatorUtil.ema(previousSmoothedPlusDm, currentPlusDm, period, 10);

				BigDecimal currentMinusDm = minusDmList.get(lookBackPeriod - 1);
				BigDecimal previousSmoothedMinusDm = adxDataList.getLastSmoothedMinusDm();
				smoothedMinusDm = IndicatorUtil.ema(previousSmoothedMinusDm, currentMinusDm, period, 10);
			}

			BigDecimal plusDi = di(smoothedPlusDm, atr);
			BigDecimal minusDi = di(smoothedMinusDm, atr);
			BigDecimal dx = dx(plusDi, minusDi);
			adxDataList.add(atr, smoothedPlusDm, smoothedMinusDm, plusDi, minusDi, dx);
		}

		return adxDataList;
	}

	private BigDecimal di(BigDecimal smoothedDm, BigDecimal atr) {
		// +DI = EMA(+DM) / ATR * 100
		// -DI = EMA(-DM) / ATR * 100
		return smoothedDm.divide(atr, 12, RoundingMode.HALF_UP).multiply(_100);
	}

	private BigDecimal dx(BigDecimal plusDi, BigDecimal minusDi) {
		// abs(+DI - -DI) / (+DI + -DI) * 100
		return plusDi.subtract(minusDi).abs().divide(plusDi.add(minusDi), 12, RoundingMode.HALF_UP).multiply(_100);
	}

	private TrendType determineTrend(BigDecimal adx, BigDecimal plusDi, BigDecimal minusDi) {
		BigDecimal _adx = adx.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _plusDi = plusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		BigDecimal _minusDi = minusDi.divide(BigDecimal.ONE, 2, RoundingMode.HALF_UP);
		TrendType trend;

		if (_adx.compareTo(STRONG_TREND_SIGNAL) > 0) {
			trend = _plusDi.compareTo(_minusDi) > 0 ? TrendType.STRONG_UP : TrendType.STRONG_DOWN;
		} else if (_adx.compareTo(TREND_SIGNAL) > 0) {
			trend = _plusDi.compareTo(_minusDi) > 0 ? TrendType.UP : TrendType.DOWN;
		} else {
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
			position = PositionType.ENTER;
		} else if (_prevPlusDi.compareTo(_prevMinusDi) > 0 && _minusDi.compareTo(_plusDi) > 0) {
			position = PositionType.EXIT;
		} else if (_prevMinusDi.compareTo(_prevPlusDi) > 0 && _prevMinusDi.compareTo(_minusDi) > 0 && _prevPlusDi.compareTo(_plusDi) < 0
				&& _minusDi.subtract(_plusDi).compareTo(TREND_WARNING) <= 0) {
			position = PositionType.UP_WARNING;
		} else if (_prevPlusDi.compareTo(_prevMinusDi) > 0 && _prevPlusDi.compareTo(_plusDi) > 0 && _prevMinusDi.compareTo(_minusDi) < 0
				&& _plusDi.subtract(_minusDi).compareTo(TREND_WARNING) <= 0) {
			position = PositionType.DOWN_WARNING;
		} else if (_plusDi.compareTo(_prevPlusDi) > 0) {
			position = PositionType.RISING;
		} else if (_plusDi.compareTo(_prevPlusDi) < 0) {
			position = PositionType.FALLING;
		} else {
			position = PositionType.HOLD;
		}

		return position;
	}
}
