package ph.mar.psereader.business.report.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name = "pse_report", indexes = @Index(columnList = "date"))
@NamedQueries({ @NamedQuery(name = PseReport.ALL, query = "SELECT pr FROM PseReport pr ORDER BY pr.date DESC"),
	@NamedQuery(name = PseReport.ALL_DATES, query = "SELECT pr.date FROM PseReport pr ORDER BY pr.date DESC"),
	@NamedQuery(name = PseReport.ALL_DATES_BY_DATE, query = "SELECT pr.date FROM PseReport pr WHERE pr.date > :date ORDER BY pr.date"),
	@NamedQuery(name = PseReport.BY_DATE, query = "SELECT pr FROM PseReport pr WHERE pr.date = :date") })
public class PseReport implements Serializable {

	public static final String ALL = "PseReport.ALL";
	public static final String ALL_DATES = "PseReport.ALL_DATES";
	public static final String ALL_DATES_BY_DATE = "PseReport.ALL_DATES_BY_DATE";
	public static final String BY_DATE = "PseReport.BY_DATE";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_pse_report", sequenceName = "seq_pse_report", allocationSize = 1)
	@GeneratedValue(generator = "seq_pse_report")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false, unique = true)
	private Date date;

	@OrderBy("id")
	@OneToMany(mappedBy = "report", cascade = CascadeType.ALL)
	private List<PseReportRow> rows;

	@Embedded
	private PseMarketSummary marketSummary;

	@Transient
	private List<PseIndex> indeces;

	@Transient
	private List<String> suspendedStocks;

	public PseReport() {
		super();
	}

	public PseReport(Date date) {
		this.date = date;
		rows = new ArrayList<>();
	}

	public boolean add(PseReportRow row) {
		return rows.add(row);
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public List<PseReportRow> getRows() {
		return rows;
	}

	public PseMarketSummary getMarketSummary() {
		return marketSummary;
	}

	public void setMarketSummary(PseMarketSummary marketSummary) {
		this.marketSummary = marketSummary;
	}

	public List<PseIndex> getIndeces() {
		return indeces;
	}

	public void setIndeces(List<PseIndex> indeces) {
		this.indeces = indeces;
	}

	public List<String> getSuspendedStocks() {
		return suspendedStocks;
	}

	public void setSuspendedStocks(List<String> suspendedStocks) {
		this.suspendedStocks = suspendedStocks;
	}

	@Override
	public String toString() {
		return String.format("PseReport [id=%s, date=%s, rows=%s, marketSummary=%s]", id, date, rows, marketSummary);
	}

}
