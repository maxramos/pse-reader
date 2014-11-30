package ph.mar.psereader.business.indicator.control;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.Callable;

import ph.mar.psereader.business.indicator.entity.MovementType;
import ph.mar.psereader.business.indicator.entity.PmovResult;
import ph.mar.psereader.business.stock.entity.Quote;

public class Pmov implements Callable<PmovResult> {

	private List<Quote> _quotes;
	BigDecimal[] _highAndLow52Week;

	private MovementType movement;

	public Pmov(List<Quote> quotes, BigDecimal[] highAndLow52Week) {
		_quotes = quotes;
		_highAndLow52Week = highAndLow52Week;
	}

	@Override
	public PmovResult call() throws Exception {
		BigDecimal previousPrice = _quotes.get(1).getClose();

		BigDecimal price = _quotes.get(0).getClose();
		BigDecimal priceChange = price.subtract(previousPrice);
		BigDecimal pricePercentChange = priceChange.divide(previousPrice, 4, RoundingMode.HALF_UP);
		BigDecimal high52Week = _highAndLow52Week[0];
		BigDecimal low52Week = _highAndLow52Week[1];
		BigDecimal changeFrom52WeekHigh = price.subtract(high52Week);
		BigDecimal percentChangeFrom52WeekHigh = changeFrom52WeekHigh.divide(high52Week, 4, RoundingMode.HALF_UP);
		PmovResult result = new PmovResult(price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh,
				percentChangeFrom52WeekHigh);

		determineMovement(priceChange);

		return result;
	}

	public MovementType getMovement() {
		return movement;
	}

	private void determineMovement(BigDecimal priceChange) {
		if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
			movement = MovementType.GAIN;
		} else if (priceChange.compareTo(BigDecimal.ZERO) < 0) {
			movement = MovementType.LOSS;
		} else {
			movement = MovementType.NO_CHANGED;
		}
	}

}
