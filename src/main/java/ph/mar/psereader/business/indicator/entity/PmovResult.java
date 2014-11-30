package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PmovResult implements Serializable {

	private static final long serialVersionUID = 1L;

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal price;

	@Column(name = "price_chg", nullable = false, precision = 8, scale = 4)
	private BigDecimal priceChange;

	@Column(name = "price_pct_chg", nullable = false, precision = 6, scale = 4)
	private BigDecimal pricePercentChange;

	@Column(name = "high_52_wk", nullable = false, precision = 8, scale = 4)
	private BigDecimal high52Week;

	@Column(name = "low_52_wk", nullable = false, precision = 8, scale = 4)
	private BigDecimal low52Week;

	@Column(name = "chg_fr_52_wk_high", nullable = false, precision = 8, scale = 4)
	private BigDecimal changeFrom52WeekHigh;

	@Column(name = "pct_chg_fr_52_wk_high", nullable = false, precision = 5, scale = 4)
	private BigDecimal percentChangeFrom52WeekHigh;

	@Column(name = "year_to_date_yield", nullable = false, precision = 8, scale = 4)
	private BigDecimal yearToDateYield;

	@Column(name = "pct_year_to_date_yield", nullable = false, precision = 6, scale = 4)
	private BigDecimal percentYearToDateYield;

	public PmovResult() {
		super();
	}

	public PmovResult(BigDecimal price, BigDecimal priceChange, BigDecimal pricePercentChange, BigDecimal high52Week, BigDecimal low52Week,
			BigDecimal changeFrom52WeekHigh, BigDecimal percentChangeFrom52WeekHigh, BigDecimal yearToDateYield, BigDecimal percentYearToDateYield) {
		this.price = price;
		this.priceChange = priceChange;
		this.pricePercentChange = pricePercentChange;
		this.high52Week = high52Week;
		this.low52Week = low52Week;
		this.changeFrom52WeekHigh = changeFrom52WeekHigh;
		this.percentChangeFrom52WeekHigh = percentChangeFrom52WeekHigh;
		this.yearToDateYield = yearToDateYield;
		this.percentYearToDateYield = percentYearToDateYield;
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

	public BigDecimal getYearToDateYield() {
		return yearToDateYield;
	}

	public BigDecimal getPercentYearToDateYield() {
		return percentYearToDateYield;
	}

	@Override
	public String toString() {
		return String
				.format("PmovResult [price=%s, priceChange=%s, pricePercentChange=%s, high52Week=%s, low52Week=%s, changeFrom52WeekHigh=%s, percentChangeFrom52WeekHigh=%s, yearToDateYield=%s, percentYearToDateYield=%s]",
						price, priceChange, pricePercentChange, high52Week, low52Week, changeFrom52WeekHigh, percentChangeFrom52WeekHigh,
						yearToDateYield, percentYearToDateYield);
	}

}
