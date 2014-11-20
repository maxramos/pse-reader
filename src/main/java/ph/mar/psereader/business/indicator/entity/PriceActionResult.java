package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PriceActionResult implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final BigDecimal PERCENT_CHANGE_FROM_52_WEEK_HIGH_CRITICAL_LEVEL = new BigDecimal("-0.08");

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal price;

	@Column(name = "price_change", nullable = false, precision = 8, scale = 4)
	private BigDecimal priceChange;

	@Column(name = "price_percent_change", nullable = false, precision = 6, scale = 4)
	private BigDecimal pricePercentChange;

	@Column(name = "52_week_high", nullable = false, precision = 8, scale = 4)
	private BigDecimal high52Week;

	@Column(name = "52_week_low", nullable = false, precision = 8, scale = 4)
	private BigDecimal low52Week;

	@Column(name = "change_from_52_week_high", nullable = false, precision = 8, scale = 4)
	private BigDecimal changeFrom52WeekHigh;

	@Column(name = "percent_change_from_52_week_high", nullable = false, precision = 5, scale = 4)
	private BigDecimal percentChangeFrom52WeekHigh;

	public PriceActionResult() {
		super();
	}

	public PriceActionResult(BigDecimal price, BigDecimal priceChange, BigDecimal pricePercentChange, BigDecimal high52Week, BigDecimal low52Week,
			BigDecimal changeFrom52WeekHigh, BigDecimal percentChangeFrom52WeekHigh) {
		this.price = price;
		this.priceChange = priceChange;
		this.pricePercentChange = pricePercentChange;
		this.high52Week = high52Week;
		this.low52Week = low52Week;
		this.changeFrom52WeekHigh = changeFrom52WeekHigh;
		this.percentChangeFrom52WeekHigh = percentChangeFrom52WeekHigh;
	}

	public PriceMovementType getPriceMovement() {
		PriceMovementType priceMovement;

		if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
			priceMovement = PriceMovementType.GAIN;
		} else if (priceChange.compareTo(BigDecimal.ZERO) < 0) {
			priceMovement = PriceMovementType.LOSS;
		} else {
			priceMovement = PriceMovementType.NO_CHANGED;
		}

		return priceMovement;
	}

	public boolean isChangeFrom52WeekHighCritical() {
		return percentChangeFrom52WeekHigh.compareTo(PERCENT_CHANGE_FROM_52_WEEK_HIGH_CRITICAL_LEVEL) < 0;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public BigDecimal getPriceChange() {
		return priceChange;
	}

	public BigDecimal getPricePercentChange() {
		return pricePercentChange;
	}

	public BigDecimal getHigh52Week() {
		return high52Week;
	}

	public BigDecimal getLow52Week() {
		return low52Week;
	}

	public BigDecimal getChangeFrom52WeekHigh() {
		return changeFrom52WeekHigh;
	}

	public BigDecimal getPercentChangeFrom52WeekHigh() {
		return percentChangeFrom52WeekHigh;
	}

	@Override
	public String toString() {
		return String
				.format("PriceActionResult [price=%s, priceChange=%s, pricePercentChange=%s, high52Week=%s, low52Week=%s, changeFrom52WeekHigh=%s, percentChangeFrom52WeekHigh=%s]",
						price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh, percentChangeFrom52WeekHigh);
	}

}
