package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PriceActionResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal price;

	@Column(name = "price_change", nullable = false, precision = 8, scale = 4)
	private BigDecimal priceChange;

	@Column(name = "price_pct_change", nullable = false, precision = 6, scale = 4)
	private BigDecimal pricePercentChange;

	@Column(name = "high_52_week", nullable = false, precision = 8, scale = 4)
	private BigDecimal high52Week;

	@Column(name = "low_52_week", nullable = false, precision = 8, scale = 4)
	private BigDecimal low52Week;

	@Column(name = "change_from_52_week_high", nullable = false, precision = 8, scale = 4)
	private BigDecimal changeFrom52WeekHigh;

	@Column(name = "pct_change_from_52_week_high", nullable = false, precision = 5, scale = 4)
	private BigDecimal percentChangeFrom52WeekHigh;

	@Column(name = "change_from_52_week_low", nullable = false, precision = 8, scale = 4)
	private BigDecimal changeFrom52WeekLow;

	@Column(name = "pct_change_from_52_week_low", nullable = false, precision = 5, scale = 4)
	private BigDecimal percentChangeFrom52WeekLow;

	public PriceActionResult() {
		super();
	}

	public PriceActionResult(BigDecimal price, BigDecimal priceChange, BigDecimal pricePercentChange, BigDecimal high52Week, BigDecimal low52Week,
			BigDecimal changeFrom52WeekHigh, BigDecimal percentChangeFrom52WeekHigh, BigDecimal changeFrom52WeekLow,
			BigDecimal percentChangeFrom52WeekLow) {
		this.price = price;
		this.priceChange = priceChange;
		this.pricePercentChange = pricePercentChange;
		this.high52Week = high52Week;
		this.low52Week = low52Week;
		this.changeFrom52WeekHigh = changeFrom52WeekHigh;
		this.percentChangeFrom52WeekHigh = percentChangeFrom52WeekHigh;
		this.changeFrom52WeekLow = changeFrom52WeekLow;
		this.percentChangeFrom52WeekLow = percentChangeFrom52WeekLow;
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

	public BigDecimal getChangeFrom52WeekLow() {
		return changeFrom52WeekLow;
	}

	public BigDecimal getPercentChangeFrom52WeekLow() {
		return percentChangeFrom52WeekLow;
	}

	@Override
	public String toString() {
		return String
				.format("PriceActionResult [price=%s, priceChange=%s, pricePercentChange=%s, high52Week=%s, low52Week=%s, changeFrom52WeekHigh=%s, percentChangeFrom52WeekHigh=%s, changeFrom52WeekLow=%s, percentChangeFrom52WeekLow=%s]",
						price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh, percentChangeFrom52WeekHigh,
						changeFrom52WeekLow, percentChangeFrom52WeekLow);
	}

}
