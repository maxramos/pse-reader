package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.util.Date;

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
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.movement, ir.trend, ir.recommendation, ir.risk, ir.pmovResult, ir.emaResult, ir.fstoResult, ir.obvResult, ir.atrResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.movement, ir.trend, ir.recommendation, ir.risk, ir.pmovResult, ir.emaResult, ir.fstoResult, ir.obvResult, ir.atrResult, ir.stock, q) FROM IndicatorResult ir, Quote q WHERE ir.stock = q.stock AND ir.date = :date AND q.date = :date AND ir.recommendation = :recommendation ORDER BY ir.stock.symbol"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.pmovResult, ir.emaResult, ir.fstoResult, ir.obvResult, ir.atrResult) FROM IndicatorResult ir WHERE ir.stock = :stock ORDER BY ir.date DESC") })
public class IndicatorResult implements Serializable {

	public static final String ALL_BY_STOCK_AND_DATE = "IndicatorResult.ALL_BY_STOCK_AND_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE";
	public static final String ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION = "IndicatorResult.ALL_INDICATOR_DATA_BY_DATE_AND_RECOMMENDATION";
	public static final String ALL_INDICATOR_DATA_BY_STOCK = "IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_indicator_result", sequenceName = "seq_indicator_result", allocationSize = 1)
	@GeneratedValue(generator = "seq_indicator_result")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private MovementType movement;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 11)
	private TrendType trend;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 18)
	private RecommendationType recommendation;

	@Enumerated(EnumType.STRING)
	@Column(length = 8)
	private RiskType risk;

	@Embedded
	private PmovResult pmovResult;

	@Embedded
	private EmaResult emaResult;

	@Embedded
	private FstoResult fstoResult;

	@Embedded
	private ObvResult obvResult;

	@Embedded
	private AtrResult atrResult;

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
	public IndicatorResult(PmovResult pmovResult, EmaResult emaResult, FstoResult fstoResult, ObvResult obvResult, AtrResult atrResult) {
		this.pmovResult = pmovResult;
		this.emaResult = emaResult;
		this.fstoResult = fstoResult;
		this.obvResult = obvResult;
		this.atrResult = atrResult;
	}

	/**
	 * Used for displaying indicator results.
	 */
	public IndicatorResult(MovementType movement, TrendType trend, RecommendationType recommendation, RiskType risk, PmovResult pmovResult,
			EmaResult emaResult, FstoResult fstoResult, ObvResult obvResult, AtrResult atrResult, Stock stock, Quote quote) {
		this.movement = movement;
		this.trend = trend;
		this.recommendation = recommendation;
		this.risk = risk;
		this.pmovResult = pmovResult;
		this.emaResult = emaResult;
		this.fstoResult = fstoResult;
		this.obvResult = obvResult;
		this.atrResult = atrResult;
		this.stock = stock;
		this.quote = quote;
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public MovementType getMovement() {
		return movement;
	}

	public void setMovement(MovementType movement) {
		this.movement = movement;
	}

	public TrendType getTrend() {
		return trend;
	}

	public void setTrend(TrendType trend) {
		this.trend = trend;
	}

	public RecommendationType getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(RecommendationType recommendation) {
		this.recommendation = recommendation;
	}

	public RiskType getRisk() {
		return risk;
	}

	public void setRisk(RiskType risk) {
		this.risk = risk;
	}

	public PmovResult getPmovResult() {
		return pmovResult;
	}

	public void setPmovResult(PmovResult pmovResult) {
		this.pmovResult = pmovResult;
	}

	public EmaResult getEmaResult() {
		return emaResult;
	}

	public void setEmaResult(EmaResult emaResult) {
		this.emaResult = emaResult;
	}

	public FstoResult getFstoResult() {
		return fstoResult;
	}

	public void setFstoResult(FstoResult fstoResult) {
		this.fstoResult = fstoResult;
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

	public Stock getStock() {
		return stock;
	}

	public Quote getQuote() {
		return quote;
	}

	@Override
	public String toString() {
		return String
				.format("IndicatorResult [id=%s, date=%s, movement=%s, trend=%s, recommendation=%s, risk=%s, pmovResult=%s, emaResult=%s, fstoResult=%s, obvResult=%s, atrResult=%s, stock=%s]",
						id, date, movement, trend, recommendation, risk, pmovResult, emaResult, fstoResult, obvResult, atrResult,
						stock == null ? null : stock.getId());
	}

}
