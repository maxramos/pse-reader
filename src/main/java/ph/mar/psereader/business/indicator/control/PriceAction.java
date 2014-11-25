package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.PriceActionResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class PriceAction implements Callable<PriceActionResult> {

	private List<Quote> _quotes;
	private BigDecimal high52Week;
	private BigDecimal low52Week;

	public PriceAction(List<Quote> quotes, BigDecimal[] highAndLow52Week) {
		_quotes = quotes;
		high52Week = highAndLow52Week[0];
		low52Week = highAndLow52Week[1];
	}

	@Override
	public PriceActionResult call() throws Exception {
		BigDecimal price = _quotes.get(0).getClose();
		BigDecimal previousPrice = _quotes.get(1).getClose();

		BigDecimal priceChange = price.subtract(previousPrice);
		BigDecimal pricePercentChange = priceChange.divide(previousPrice, 4, RoundingMode.HALF_UP);
		BigDecimal changeFrom52WeekHigh = price.subtract(high52Week);
		BigDecimal percentChangeFrom52WeekHigh = changeFrom52WeekHigh.divide(high52Week, 4, RoundingMode.HALF_UP);
		return new PriceActionResult(price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh, percentChangeFrom52WeekHigh);
	}

}
