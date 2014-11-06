package ph.mar.psereader.business.indicator.control;

import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.MacdResult;
import ph.mar.psereader.business.stock.entity.Quote;

/**
 * This implements the Moving Average Convergence/Divergence (MACD).
 */
public class MacdIndicator implements Callable<MacdResult> {

	int fastEmaLookBackPeriod = 12;
	int slowEmaLookBackPeriod = 26;
	int signalLineLookBackPeriod = 9;

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public MacdIndicator(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public MacdResult call() throws Exception {
		return _results.isEmpty() ? initialMacd(_quotes) : succeedingMacd(_quotes, _results);
	}

	private MacdResult initialMacd(List<Quote> quotes) {
		int size = fastEmaLookBackPeriod - 1 + slowEmaLookBackPeriod - 1 + signalLineLookBackPeriod - 1; // 34
		List<Quote> trimmedQuotes = quotes.subList(0, size);
		return null;
	}

	private MacdResult succeedingMacd(List<Quote> quotes, List<IndicatorResult> results) {
		Quote currentQuote = quotes.get(0);
		return null;
	}

}
