package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.VolatilityType;
import ph.mar.psereader.business.indicator.entity.VrResult;
import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

public class VrIndicator implements Callable<VrResult> {

	private static final BigDecimal BREAKOUT = new BigDecimal("0.5");
	private static final BigDecimal WIDE_RANGING = new BigDecimal("2");

	private static final int LOOK_BACK_PERIOD = 14;
	private static final BigDecimal TWO = new BigDecimal("2");
	private static final BigDecimal VR_FACTOR = TWO.divide(new BigDecimal(LOOK_BACK_PERIOD).add(BigDecimal.ONE), 10, RoundingMode.HALF_UP);

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;
	private Stock stock;

	public VrIndicator(Stock stock, List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
		this.stock = stock;
	}

	@Override
	public VrResult call() throws Exception {
		return _results.isEmpty() ? initialVr(_quotes) : succeedingVr(_quotes, _results);
	}

	private VrResult initialVr(List<Quote> quotes) {
		if (stock.getSymbol().equals("TEL")) {
			System.out.println("TEL");
		}

		int size = LOOK_BACK_PERIOD + 1; // 8
		List<Quote> trimmedQuotes = quotes.subList(0, size);

		List<BigDecimal> trList = tr(trimmedQuotes);
		BigDecimal tr = trList.get(0);
		BigDecimal smoothedTr = IndicatorUtil.avg(trList, 10);
		BigDecimal vr = vr(tr, smoothedTr);
		VolatilityType volatility = determineVolatility(vr);

		VrResult result = new VrResult(vr, volatility, smoothedTr);
		return result;
	}

	private VrResult succeedingVr(List<Quote> quotes, List<IndicatorResult> results) {
		if (stock.getSymbol().equals("TEL")) {
			System.out.println("TEL");
		}

		VrResult previousVrResult = results.get(0).getVrResult();
		BigDecimal previousSmoothedTr = previousVrResult.getSmoothedTr();

		Quote currentQuote = quotes.get(0);
		BigDecimal high = currentQuote.getHigh();
		BigDecimal low = currentQuote.getLow();
		BigDecimal previousClose = quotes.get(1).getClose();

		BigDecimal tr = tr(high, low, previousClose);
		BigDecimal smoothedTr = IndicatorUtil.ema(previousSmoothedTr, tr, VR_FACTOR, 10);
		BigDecimal vr = vr(tr, smoothedTr);
		VolatilityType volatility = determineVolatility(vr);

		VrResult result = new VrResult(vr, volatility, smoothedTr);
		return result;
	}

	private List<BigDecimal> tr(List<Quote> quotes) {
		List<BigDecimal> trList = new ArrayList<>(LOOK_BACK_PERIOD);

		for (int i = 0, current = 0, previous = 1; i < LOOK_BACK_PERIOD; i++, previous++, current++) {
			Quote currentQuote = quotes.get(current);
			Quote previousQuote = quotes.get(previous);

			BigDecimal high = currentQuote.getHigh();
			BigDecimal low = currentQuote.getLow();
			BigDecimal previousClose = previousQuote.getClose();

			BigDecimal tr = tr(high, low, previousClose);
			trList.add(tr);
		}

		return trList;
	}

	private BigDecimal tr(BigDecimal high, BigDecimal low, BigDecimal previousClose) {
		BigDecimal trCandidate1 = high.subtract(low);
		BigDecimal trCandidate2 = high.subtract(previousClose);
		BigDecimal trCandidate3 = previousClose.subtract(low);

		return trCandidate1.max(trCandidate2).max(trCandidate3);
	}

	private BigDecimal vr(BigDecimal tr, BigDecimal smoothedTr) {
		if (smoothedTr.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}

		return tr.divide(smoothedTr, 2, RoundingMode.HALF_UP);
	}

	private VolatilityType determineVolatility(BigDecimal vr) {
		VolatilityType volatility;

		if (vr.compareTo(BREAKOUT) < 0) {
			volatility = VolatilityType.NORMAL;
		} else if (vr.compareTo(WIDE_RANGING) < 0) {
			volatility = VolatilityType.BREAKOUT;
		} else {
			volatility = VolatilityType.WIDE_RANGING;
		}

		return volatility;
	}
}
