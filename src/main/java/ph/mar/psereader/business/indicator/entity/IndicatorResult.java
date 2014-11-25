package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import ph.mar.psereader.business.stock.entity.Quote;
import ph.mar.psereader.business.stock.entity.Stock;

@Entity
@Table(name = "indicator_result", uniqueConstraints = @UniqueConstraint(columnNames = { "date", "stock_id" }), indexes = @Index(columnList = "stock_id,date"))
@NamedQueries({
	@NamedQuery(name = IndicatorResult.ALL_BY_STOCK_AND_DATE, query = "SELECT ir FROM IndicatorResult ir WHERE ir.stock = :stock AND ir.date BETWEEN :start AND :end ORDER BY ir.date DESC"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.trend, ir.recommendation, ir.risk, ir.movement, ir.emaResult, ir.sstoResult, ir.obvResult, ir.atrResult, ir.priceActionResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.trend, ir.recommendation, ir.risk, ir.movement, ir.emaResult, ir.sstoResult, ir.obvResult, ir.atrResult, ir.priceActionResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date AND ir.recommendation = :recommendation ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.emaResult, ir.sstoResult, ir.obvResult, ir.atrResult, ir.priceActionResult) FROM IndicatorResult ir WHERE ir.stock = :stock ORDER BY ir.date DESC") })
public class IndicatorResult implements Serializable {

	public static final String ALL_BY_STOCK_AND_DATE = "IndicatorResult.ALL_BY_STOCK_AND_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION";
	public static final String ALL_INDICATOR_DATA_BY_STOCK = "IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK";

	private static final long serialVersionUID = 1L;
	private static final BigDecimal BUY_DANGER_LEVEL = new BigDecimal("-0.08");
	private static final BigDecimal CRITICAL_LEVEL = BigDecimal.ZERO;

	@Id
	@SequenceGenerator(name = "seq_indicator_result", sequenceName = "seq_indicator_result", allocationSize = 1)
	@GeneratedValue(generator = "seq_indicator_result")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 11)
	private TrendType trend;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 18)
	private RecommendationType recommendation;

	@Enumerated(EnumType.STRING)
	@Column(length = 8)
	private RiskType risk;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private MovementType movement;

	@Embedded
	private EmaResult emaResult;

	@Embedded
	private SstoResult sstoResult;

	@Embedded
	private ObvResult obvResult;

	@Embedded
	private AtrResult atrResult;

	@Embedded
	private PriceActionResult priceActionResult;

	@ManyToOne
	private Stock stock;

	@Transient
	private Quote quote;

	public IndicatorResult() {
		super();
	}

	public IndicatorResult(Stock stock, Date date) {
		this.stock = stock;
		this.date = date;
	}

	/**
	 * Used for processing indicator results.
	 */
	public IndicatorResult(EmaResult emaResult, SstoResult sstoResult, ObvResult obvResult, AtrResult atrResult, PriceActionResult priceActionResult) {
		this.emaResult = emaResult;
		this.sstoResult = sstoResult;
		this.obvResult = obvResult;
		this.atrResult = atrResult;
		this.priceActionResult = priceActionResult;
	}

	/**
	 * Used for displaying indicator results.
	 */
	public IndicatorResult(TrendType trend, RecommendationType recommendation, RiskType risk, MovementType movement, EmaResult emaResult,
			SstoResult sstoResult, ObvResult obvResult, AtrResult atrResult, PriceActionResult priceActionResult, Stock stock, Quote quote) {
		this.trend = trend;
		this.recommendation = recommendation;
		this.risk = risk;
		this.movement = movement;
		this.emaResult = emaResult;
		this.sstoResult = sstoResult;
		this.obvResult = obvResult;
		this.atrResult = atrResult;
		this.priceActionResult = priceActionResult;
		this.stock = stock;
		this.quote = quote;
	}

	public void process(List<Quote> quotes, List<IndicatorResult> results) {
		trend = emaResult.getTrend();
		recommendation = emaResult.getRecommendation();
		determineRisk();
		determineMovement();
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public TrendType getTrend() {
		return trend;
	}

	public RecommendationType getRecommendation() {
		return recommendation;
	}

	public RiskType getRisk() {
		return risk;
	}

	public MovementType getMovement() {
		return movement;
	}

	public EmaResult getEmaResult() {
		return emaResult;
	}

	public void setEmaResult(EmaResult emaResult) {
		this.emaResult = emaResult;
	}

	public SstoResult getSstoResult() {
		return sstoResult;
	}

	public void setSstoResult(SstoResult sstoResult) {
		this.sstoResult = sstoResult;
	}

	public ObvResult getObvResult() {
		return obvResult;
	}

	public void setObvResult(ObvResult obvResult) {
		this.obvResult = obvResult;
	}

	public AtrResult getAtrResult() {
		return atrResult;
	}

	public void setAtrResult(AtrResult atrResult) {
		this.atrResult = atrResult;
	}

	public PriceActionResult getPriceActionResult() {
		return priceActionResult;
	}

	public void setPriceActionResult(PriceActionResult priceActionResult) {
		this.priceActionResult = priceActionResult;
	}

	public Stock getStock() {
		return stock;
	}

	public Quote getQuote() {
		return quote;
	}

	@Override
	public String toString() {
		return String
				.format("IndicatorResult [id=%s, date=%s, trend=%s, recommendation=%s, risk=%s, movement=%s, emaResult=%s, sstoResult=%s, obvResult=%s, atrResult=%s, priceActionResult=%s, stock=%s]",
						id, date, trend, recommendation, risk, movement, emaResult, sstoResult, obvResult, atrResult, priceActionResult,
						stock == null ? null : stock.getId());
	}

	private void determineRisk() {
		BigDecimal percentChangeFrom52WeekHigh = priceActionResult.getPercentChangeFrom52WeekHigh();

		if (percentChangeFrom52WeekHigh.compareTo(CRITICAL_LEVEL) == 0) {
			risk = RiskType.CRITICAL;
		} else if (percentChangeFrom52WeekHigh.compareTo(BUY_DANGER_LEVEL) >= 0) {
			risk = RiskType.DANGER;
		} else {
			risk = RiskType.SAFE;
		}
	}

	private void determineMovement() {
		BigDecimal priceChange = priceActionResult.getPriceChange();

		if (priceChange.compareTo(BigDecimal.ZERO) > 0) {
			movement = MovementType.GAIN;
		} else if (priceChange.compareTo(BigDecimal.ZERO) < 0) {
			movement = MovementType.LOSS;
		} else {
			movement = MovementType.NO_CHANGED;
		}
	}

}
