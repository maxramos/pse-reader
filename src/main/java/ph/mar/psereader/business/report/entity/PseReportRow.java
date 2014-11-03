package ph.mar.psereader.business.report.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import ph.mar.psereader.business.stock.entity.SectorType;
import ph.mar.psereader.business.stock.entity.SubSectorType;

@Entity
@Table(name = "pse_report_row", uniqueConstraints = @UniqueConstraint(columnNames = { "symbol", "report_id" }))
@NamedQueries({ @NamedQuery(name = PseReportRow.BY_DATE, query = "SELECT prr FROM PseReportRow prr WHERE prr.report.date = :date ORDER BY prr.report.date DESC"), })
public class PseReportRow implements Serializable {

	public static final String BY_DATE = "PseReportRow.BY_DATE";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_pse_report_row", sequenceName = "seq_pse_report_row", allocationSize = 1)
	@GeneratedValue(generator = "seq_pse_report_row")
	private Long id;

	@Column(nullable = false, length = 5)
	private String symbol; // 9

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 4)
	private SectorType sector;

	@Enumerated(EnumType.STRING)
	@Column(name = "sub_sector", nullable = false, length = 5)
	private SubSectorType subSector;

	@Column(precision = 8, scale = 4)
	private BigDecimal bid; // 8

	@Column(precision = 8, scale = 4)
	private BigDecimal ask; // 7

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal open; // 6

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal high; // 5

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal low; // 4

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal close; // 3

	@Column(nullable = false)
	private Long volume; // 2

	@Column(nullable = false, precision = 16, scale = 4)
	private BigDecimal value; // 1

	@Column(name = "foreign_buy_sell", precision = 16, scale = 4)
	private BigDecimal foreignBuySell; // 0

	@ManyToOne
	@JoinColumn(name = "report_id")
	private PseReport report; // 10 - N

	@Transient
	private String companyName;

	public PseReportRow() {
		super();
	}

	public PseReportRow(String symbol, SectorType sector, SubSectorType subSector) {
		this.symbol = symbol;
		this.sector = sector;
		this.subSector = subSector;
	}

	public boolean isValid() {
		return open != null && high != null && low != null && close != null && volume != null;
	}

	public Long getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	public SectorType getSector() {
		return sector;
	}

	public SubSectorType getSubSector() {
		return subSector;
	}

	public BigDecimal getBid() {
		return bid;
	}

	public void setBid(BigDecimal bid) {
		this.bid = bid;
	}

	public BigDecimal getAsk() {
		return ask;
	}

	public void setAsk(BigDecimal ask) {
		this.ask = ask;
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

	public BigDecimal getForeignBuySell() {
		return foreignBuySell;
	}

	public void setForeignBuySell(BigDecimal foreignBuySell) {
		this.foreignBuySell = foreignBuySell;
	}

	public PseReport getReport() {
		return report;
	}

	public void setReport(PseReport report) {
		this.report = report;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	@Override
	public String toString() {
		return String
				.format("PseReportRow [id=%s, symbol=%s, sector=%s, subSector=%s, bid=%s, ask=%s, open=%s, high=%s, low=%s, close=%s, volume=%s, value=%s, foreignBuySell=%s, report.id=%s]",
						id, symbol, sector, subSector, bid, ask, open, high, low, close, volume, value, foreignBuySell, report == null ? null
								: report.getId());
	}

}
