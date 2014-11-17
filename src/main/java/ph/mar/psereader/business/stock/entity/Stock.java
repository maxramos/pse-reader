package ph.mar.psereader.business.stock.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.report.entity.PseReportRow;

@Entity
@Table(indexes = @Index(columnList = "symbol"))
@NamedQueries({
	@NamedQuery(name = Stock.ALL, query = "SELECT s FROM Stock s ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_SYMBOLS, query = "SELECT s.symbol FROM Stock s ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_WITH_QUOTES_BY_SYMBOL, query = "SELECT DISTINCT s FROM Stock s JOIN FETCH s.quotes WHERE s.symbol = :symbol"),
	@NamedQuery(name = Stock.ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT, query = "SELECT DISTINCT s FROM Stock s LEFT JOIN FETCH s.indicatorResults WHERE EXISTS (SELECT q FROM Quote q WHERE q.stock = s AND q.date = :date) AND (SELECT COUNT(q) FROM Quote q WHERE q.stock = s AND q.date <= :date) >= :count AND s.suspended = FALSE ORDER BY s.symbol"),
	@NamedQuery(name = Stock.BY_SYMBOL, query = "SELECT s FROM Stock s WHERE s.symbol = :symbol ORDER BY s.symbol") })
public class Stock implements Serializable {

	public static final String ALL = "Stock.ALL";
	public static final String ALL_SYMBOLS = "Stock.ALL_SYMBOLS";
	public static final String ALL_WITH_QUOTES_BY_SYMBOL = "Stock.ALL_WITH_QUOTES_BY_SYMBOL";
	// WOWO means with or without
	public static final String ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT = "Stock.ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT";
	public static final String BY_SYMBOL = "Stock.BY_SYMBOL";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_stock", sequenceName = "seq_stock", allocationSize = 1)
	@GeneratedValue(generator = "seq_stock")
	private Long id;

	@Column(nullable = false, length = 5, unique = true)
	private String symbol;

	@Column(nullable = false, length = 20)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 4)
	private SectorType sector;

	@Enumerated(EnumType.STRING)
	@Column(name = "sub_sector", nullable = false, length = 5)
	private SubSectorType subSector;

	@OrderBy("date DESC")
	@OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
	private List<Quote> quotes;

	@OrderBy("date DESC")
	@OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
	private List<IndicatorResult> indicatorResults;

	@Column(nullable = false)
	private boolean suspended;

	public Stock() {
		super();
	}

	public Stock(String symbol, String name, SectorType sector, SubSectorType subSector) {
		this.symbol = symbol;
		this.name = name;
		this.sector = sector;
		this.subSector = subSector;
		quotes = new ArrayList<>();
		indicatorResults = new ArrayList<>();
		suspended = false;
	}

	public static Stock convert(PseReportRow row) {
		return new Stock(row.getSymbol(), row.getCompanyName(), row.getSector(), row.getSubSector());
	}

	public boolean add(Quote quote) {
		quote.setStock(this);
		return quotes.add(quote);
	}

	public boolean add(IndicatorResult indicatorResult) {
		return indicatorResults.add(indicatorResult);
	}

	public Long getId() {
		return id;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getName() {
		return name;
	}

	public SectorType getSector() {
		return sector;
	}

	public SubSectorType getSubSector() {
		return subSector;
	}

	public List<Quote> getQuotes() {
		return quotes;
	}

	public List<IndicatorResult> getIndicatorResults() {
		return indicatorResults;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	@Override
	public String toString() {
		return String.format("Stock [id=%s, symbol=%s, name=%s, sector=%s, subSector=%s, quotes=%s, indicatorResults=%s, suspended=%s]", id, symbol,
				name, sector, subSector, quotes, indicatorResults, suspended);
	}

}
