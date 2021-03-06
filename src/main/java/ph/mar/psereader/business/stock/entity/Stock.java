package ph.mar.psereader.business.stock.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import ph.mar.psereader.business.fundamental.entity.Fundamental;
import ph.mar.psereader.business.indicator.entity.IndicatorResult;
import ph.mar.psereader.business.pseindex.entity.PseIndex;
import ph.mar.psereader.business.report.entity.PseReportRow;

@Entity
@Table(indexes = @Index(columnList = "symbol"))
@NamedQueries({
	@NamedQuery(name = Stock.ALL, query = "SELECT s FROM Stock s ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_ALL_SHARES, query = "SELECT s FROM Stock s WHERE s.allShares = TRUE ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_PSEI, query = "SELECT s FROM Stock s WHERE s.psei = TRUE ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_SYMBOLS, query = "SELECT s.symbol FROM Stock s ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_SYMBOLS_WO_FUNDAMENTAL, query = "SELECT s.symbol FROM Stock s WHERE s.fundamental IS NULL ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_WITH_FUNDAMENTAL, query = "SELECT DISTINCT s FROM Stock s JOIN FETCH s.fundamental ORDER BY s.symbol"),
	@NamedQuery(name = Stock.ALL_WITH_QUOTES_BY_SYMBOL, query = "SELECT DISTINCT s FROM Stock s JOIN FETCH s.quotes WHERE s.symbol = :symbol"),
	@NamedQuery(name = Stock.ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT, query = "SELECT DISTINCT s FROM Stock s LEFT JOIN FETCH s.indicatorResults WHERE EXISTS (SELECT q FROM Quote q WHERE q.stock = s AND q.date = :date) AND (SELECT COUNT(q) FROM Quote q WHERE q.stock = s AND q.date <= :date) >= :count AND s.suspended = FALSE ORDER BY s.symbol"),
	@NamedQuery(name = Stock.BY_SECTORAL_INDEX, query = "SELECT s FROM Stock s WHERE s.sectoralIndex = :sectoralIndex ORDER BY s.symbol"),
	@NamedQuery(name = Stock.BY_SYMBOL, query = "SELECT s FROM Stock s WHERE s.symbol = :symbol") })
public class Stock implements Serializable {

	public static final String ALL = "Stock.ALL";
	public static final String ALL_ALL_SHARES = "Stock.ALL_ALL_SHARES";
	public static final String ALL_PSEI = "Stock.ALL_PSEI";
	public static final String ALL_SYMBOLS = "Stock.ALL_SYMBOLS";
	public static final String ALL_SYMBOLS_WO_FUNDAMENTAL = "Stock.ALL_SYMBOLS_WO_FUNDAMENTAL";
	public static final String ALL_WITH_FUNDAMENTAL = "Stock.ALL_WITH_FUNDAMENTAL";
	public static final String ALL_WITH_QUOTES_BY_SYMBOL = "Stock.ALL_WITH_QUOTES_BY_SYMBOL";
	// WOWO means with or without
	public static final String ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT = "Stock.ALL_WOWO_INDICATOR_RESULTS_BY_DATE_AND_COUNT";
	public static final String BY_SECTORAL_INDEX = "Stock.BY_SECTORAL_INDEX";
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

	@Column(nullable = false)
	private boolean suspended;

	private boolean psei;

	@Column(name = "all_shares")
	private boolean allShares;

	@Enumerated(EnumType.STRING)
	@Column(name = "sectoral_index", length = 4)
	private PseIndex.Type sectoralIndex;

	@OrderBy("date DESC")
	@OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
	private List<Quote> quotes;

	@OrderBy("date DESC")
	@OneToMany(mappedBy = "stock", cascade = CascadeType.ALL)
	private List<IndicatorResult> indicatorResults;

	@OneToOne(fetch = FetchType.LAZY, mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
	private Fundamental fundamental;

	public Stock() {
		super();
	}

	public Stock(String symbol, String name, SectorType sector, SubSectorType subSector) {
		this.symbol = symbol;
		this.name = name;
		this.sector = sector;
		this.subSector = subSector;
		suspended = false;
		quotes = new ArrayList<>();
		indicatorResults = new ArrayList<>();
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

	public void setSector(SectorType sector) {
		this.sector = sector;
	}

	public SubSectorType getSubSector() {
		return subSector;
	}

	public void setSubSector(SubSectorType subSector) {
		this.subSector = subSector;
	}

	public boolean isSuspended() {
		return suspended;
	}

	public void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}

	public boolean isPsei() {
		return psei;
	}

	public void setPsei(boolean psei) {
		this.psei = psei;
	}

	public boolean isAllShares() {
		return allShares;
	}

	public void setAllShares(boolean allShares) {
		this.allShares = allShares;
	}

	public PseIndex.Type getSectoralIndex() {
		return sectoralIndex;
	}

	public void setSectoralIndex(PseIndex.Type sectoralIndex) {
		this.sectoralIndex = sectoralIndex;
	}

	public List<Quote> getQuotes() {
		return quotes;
	}

	public List<IndicatorResult> getIndicatorResults() {
		return indicatorResults;
	}

	public Fundamental getFundamental() {
		return fundamental;
	}

	public void setFundamental(Fundamental fundamental) {
		this.fundamental = fundamental;
	}

	@Override
	public String toString() {
		return String
				.format("Stock [id=%s, symbol=%s, name=%s, sector=%s, subSector=%s, suspended=%s, psei=%s, allShares=%s, sectoralIndex=%s, quotes=%s, indicatorResults=%s, fundamental=%s]",
						id, symbol, name, sector, subSector, suspended, psei, allShares, sectoralIndex, quotes, indicatorResults, fundamental);
	}

}
