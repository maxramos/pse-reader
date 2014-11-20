package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.indicator.entity.PriceActionResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class PriceAction implements Callable<PriceActionResult> {

	private List<Quote> _quotes;
	private List<IndicatorResult> _results;

	public PriceAction(List<Quote> quotes, List<IndicatorResult> results) {
		_quotes = quotes;
		_results = results;
	}

	@Override
	public PriceActionResult call() throws Exception {
		return _results.isEmpty() ? initialPriceAction() : succeedingPriceAction();
	}

	private PriceActionResult initialPriceAction() {
		BigDecimal price = _quotes.get(0).getClose();
		BigDecimal priceChange = BigDecimal.ZERO;
		BigDecimal pricePercentChange = BigDecimal.ZERO;
		BigDecimal high52Week = price;
		BigDecimal low52Week = price;
		BigDecimal changeFrom52WeekHigh = priceChange;
		BigDecimal percentChangeFrom52WeekHigh = pricePercentChange;
		return new PriceActionResult(price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh, percentChangeFrom52WeekHigh);
	}

	private PriceActionResult succeedingPriceAction() {
		BigDecimal previousPrice = _quotes.get(1).getClose();
		PriceActionResult previousPriceActionResult = _results.get(0).getPriceActionResult();
		BigDecimal previousHigh52Week = previousPriceActionResult.getHigh52Week();
		BigDecimal previousLow52Week = previousPriceActionResult.getLow52Week();

		BigDecimal price = _quotes.get(0).getClose();
		BigDecimal priceChange = price.subtract(previousPrice);
		BigDecimal pricePercentChange = priceChange.divide(previousPrice, 4, RoundingMode.HALF_UP);
		BigDecimal high52Week = price.compareTo(previousHigh52Week) > 0 ? price : previousHigh52Week;
		BigDecimal low52Week = price.compareTo(previousLow52Week) < 0 ? price : previousLow52Week;
		BigDecimal changeFrom52WeekHigh = priceChange;
		BigDecimal percentChangeFrom52WeekHigh = pricePercentChange;
		return new PriceActionResult(price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh, percentChangeFrom52WeekHigh);
	}
}
