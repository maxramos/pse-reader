package ph.mar.psereader.business.index.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "pse_index_quote", uniqueConstraints = @UniqueConstraint(columnNames = { "date", "index_id" }), indexes = @Index(columnList = "index_id,date"))
public class PseIndexQuote implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_pse_index_quote", sequenceName = "seq_pse_index_quote", allocationSize = 1)
	@GeneratedValue(generator = "seq_pse_index_quote")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Column(precision = 10, scale = 4)
	private BigDecimal open;// 7 or 5 for PSEi and All Shares

	@Column(precision = 10, scale = 4)
	private BigDecimal high; // 6 or 4 for PSEi and All Shares

	@Column(precision = 10, scale = 4)
	private BigDecimal low; // 5 or 3 for PSEi and All Shares

	@Column(precision = 10, scale = 4)
	private BigDecimal close; // 4 or 2 for PSEi and All Shares

	@Column(name = "pct_change", precision = 4, scale = 2)
	private BigDecimal percentChange; // 3 or 1 for PSEi and All Shares

	@Column(name = "pt_change", precision = 5, scale = 2)
	private BigDecimal pointChange; // 2 or 0 for PSEi and All Shares

	private Long volume; // 1 or none for PSEi and All Shares

	@Column(precision = 16, scale = 4)
	private BigDecimal value; // 0 or none for PSEi and All Shares

	@ManyToOne
	private PseIndex index;

	public PseIndexQuote() {
		super();
	}

	public PseIndexQuote(Date date) {
		this.date = date;
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getPercentChange() {
		return percentChange;
	}

	public void setPercentChange(BigDecimal percentChange) {
		this.percentChange = percentChange;
	}

	public BigDecimal getPointChange() {
		return pointChange;
	}

	public void setPointChange(BigDecimal pointChange) {
		this.pointChange = pointChange;
	}

	public Long getVolume() {
		return volume;
	}

	public void setVolume(Long volume) {
		this.volume = volume;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public PseIndex getIndex() {
		return index;
	}

	public void setIndex(PseIndex index) {
		this.index = index;
	}

	@Override
	public String toString() {
		return String
				.format("PseIndexQuote [id=%s, date=%s, open=%s, high=%s, low=%s, close=%s, percentChange=%s, pointChange=%s, volume=%s, value=%s, index.id=%s]",
						id, date, open, high, low, close, percentChange, pointChange, volume, value, index == null ? null : index.getId());
	}

}
