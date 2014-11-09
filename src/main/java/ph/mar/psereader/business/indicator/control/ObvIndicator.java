package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.MovementType;
import ph.mar.psereader.business.indicator.entity.ObvResult;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the On Balance Volume (OBV).
 *
 * Computations:
 * Initial OBV = 0
 * OBV = if CLOSE > PREV_CLOSE then +VOLUME else if CLOSE < PREV_CLOSE then -VOLUME else PREV_OBV
 *
 * Movements:
 * UP --- OBV > PREV_OBV
 * DOWN --- OBV < PREV_OBV
 * UNCHANGED --- OBV == PREV_OBV
 */
public class ObvIndicator implements Callable<ObvResult> {

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public ObvIndicator(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public ObvResult call() throws Exception {
		return _results.isEmpty() ? initialObv(_quotes) : succeedingObv(_quotes, _results);
	}

	private ObvResult initialObv(List<Quote> quotes) {
		Quote currentQuote = quotes.get(0);
		BigDecimal currentClose = currentQuote.getClose();
		BigDecimal previousClose = quotes.get(1).getClose();
		Long volume = currentQuote.getVolume();
		Long initialObv = 0L;

		Long obv = obv(currentClose, previousClose, volume, initialObv);
		MovementType movement = determineMovement(obv, initialObv);
		return new ObvResult(obv, movement);
	}

	private ObvResult succeedingObv(List<Quote> quotes, List<IndicatorResult> results) {
		Quote currentQuote = quotes.get(0);
		BigDecimal currentClose = currentQuote.getClose();
		BigDecimal previousClose = quotes.get(1).getClose();
		Long volume = currentQuote.getVolume();
		Long prevObv = results.get(0).getObvResult().getObv();

		Long obv = obv(currentClose, previousClose, volume, prevObv);
		MovementType movement = determineMovement(obv, prevObv);
		return new ObvResult(obv, movement);
	}

	private Long obv(BigDecimal close, BigDecimal prevClose, Long volume, Long prevObv) {
		Long obv = prevObv; // if CLOSE == PREV_CLOSE then PREV_OBV

		if (close.compareTo(prevClose) > 0) {
			obv += volume; // if CLOSE > PREV_CLOSE then +VOLUME
		} else if (close.compareTo(prevClose) < 0) {
			obv -= volume; // if CLOSE < PREV_CLOSE then -VOLUME
		}

		return obv;
	}

	private MovementType determineMovement(Long obv, Long prevObv) {
		MovementType movement;

		if (obv.compareTo(prevObv) > 0) {
			movement = MovementType.UP; // OBV > PREV_OBV
		} else if (obv.compareTo(prevObv) < 0) {
			movement = MovementType.DOWN; // OBV < PREV_OBV
		} else {
			movement = MovementType.UNCHANGED; // OBV == PREV_OBV
		}

		return movement;
	}

}
