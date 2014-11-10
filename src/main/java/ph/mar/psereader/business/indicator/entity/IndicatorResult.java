package ph.mar.psereader.business.indicator.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
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
import javax.persistence.UniqueConstraint;

import ph.mar.psereader.business.stock.entity.Stock;

@Entity
@Table(name = "indicator_result", uniqueConstraints = @UniqueConstraint(columnNames = { "date", "stock_id" }), indexes = @Index(columnList = "stock_id,date"))
@NamedQueries({
	@NamedQuery(name = IndicatorResult.ALL_BY_DATE, query = "SELECT ir FROM IndicatorResult ir WHERE ir.date = :date"),
	@NamedQuery(name = IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK, query = "SELECT NEW ph.mar.psereader.business.indicator.entity.IndicatorResult(ir.sstoResult, ir.rsiResult, ir.dmiResult, ir.macdResult, ir.obvResult, ir.vrResult) FROM IndicatorResult ir WHERE ir.stock = :stock ORDER BY ir.date DESC") })
public class IndicatorResult implements Serializable {

	public static final String ALL_BY_DATE = "IndicatorResult.ALL_BY_DATE";
	public static final String ALL_INDICATOR_DATA_BY_STOCK = "IndicatorResult.ALL_INDICATOR_DATA_BY_STOCK";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_indicator_result", sequenceName = "seq_indicator_result", allocationSize = 1)
	@GeneratedValue(generator = "seq_indicator_result")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Embedded
	private SstoResult sstoResult;

	@Embedded
	private RsiResult rsiResult;

	@Embedded
	private DmiResult dmiResult;

	@Embedded
	private MacdResult macdResult;

	@Embedded
	private ObvResult obvResult;

	@Embedded
	private VrResult vrResult;

	@ManyToOne
	private Stock stock;

	public IndicatorResult() {
		super();
	}

	public IndicatorResult(Stock stock, Date date) {
		this.stock = stock;
		this.date = date;
	}

	/**
	 * Used for NEW jpql construct.
	 */
	public IndicatorResult(SstoResult sstoResult, RsiResult rsiResult, DmiResult dmiResult, MacdResult macdResult, ObvResult obvResult,
			VrResult vrResult) {
		this.sstoResult = sstoResult;
		this.rsiResult = rsiResult;
		this.dmiResult = dmiResult;
		this.macdResult = macdResult;
		this.obvResult = obvResult;
		this.vrResult = vrResult;
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public SstoResult getSstoResult() {
		return sstoResult;
	}

	public void setSstoResult(SstoResult sstoResult) {
		this.sstoResult = sstoResult;
	}

	public RsiResult getRsiResult() {
		return rsiResult;
	}

	public void setRsiResult(RsiResult rsiResult) {
		this.rsiResult = rsiResult;
	}

	public Stock getStock() {
		return stock;
	}

	public DmiResult getDmiResult() {
		return dmiResult;
	}

	public void setDmiResult(DmiResult dmiResult) {
		this.dmiResult = dmiResult;
	}

	public MacdResult getMacdResult() {
		return macdResult;
	}

	public void setMacdResult(MacdResult macdResult) {
		this.macdResult = macdResult;
	}

	public ObvResult getObvResult() {
		return obvResult;
	}

	public void setObvResult(ObvResult obvResult) {
		this.obvResult = obvResult;
	}

	public VrResult getVrResult() {
		return vrResult;
	}

	public void setVrResult(VrResult vrResult) {
		this.vrResult = vrResult;
	}

	@Override
	public String toString() {
		return String.format(
				"IndicatorResult [id=%s, date=%s, sstoResult=%s, rsiResult=%s, dmiResult=%s, macdResult=%s, obvResult=%s, vrResult=%s, stock.id=%s]",
				id, date, sstoResult, rsiResult, dmiResult, macdResult, obvResult, vrResult, stock == null ? null : stock.getId());
	}

}
