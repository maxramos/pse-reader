package ph.mar.psereader.business.stock.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
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

import ph.mar.psereader.business.indicator.entity.BoardLotAndPriceFluctuation;
import ph.mar.psereader.business.report.entity.PseReportRow;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "date", "stock_id" }), indexes = @Index(columnList = "stock_id,date"))
@NamedQueries({
	@NamedQuery(name = Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE, query = "SELECT NEW ph.mar.psereader.business.stock.entity.Quote(q.date, q.open, q.high, q.low, q.close, q.volume) FROM Quote q WHERE q.stock = :stock AND q.date <= :date ORDER BY q.date DESC"),
	@NamedQuery(name = Quote.ALL_REPORT_ROW_BY_DATE, query = "SELECT NEW ph.mar.psereader.business.report.entity.PseReportRow(q.stock.name, q.stock.symbol, q.bid, q.ask, q.open, q.high, q.low, q.close, q.volume, q.value, q.foreignBuySell, q.stock.sector, q.stock.subSector) FROM Quote q WHERE q.date = :date ORDER BY q.stock.symbol"),
	@NamedQuery(name = Quote.BY_STOCK, query = "SELECT q FROM Quote q WHERE q.stock = :stock ORDER BY q.date DESC") })
public class Quote implements Serializable {

	public static final String ALL_INDICATOR_DATA_BY_STOCK_AND_DATE = "Quote.ALL_INDICATOR_DATA_BY_STOCK_AND_DATE";
	public static final String ALL_REPORT_ROW_BY_DATE = "Quote.ALL_REPORT_ROW_BY_DATE";
	public static final String BY_STOCK = "Quote.BY_STOCK";

	public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_quote", sequenceName = "seq_quote", allocationSize = 1)
	@GeneratedValue(generator = "seq_quote")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false)
	private Date date;

	@Column(precision = 8, scale = 4)
	private BigDecimal bid;

	@Column(precision = 8, scale = 4)
	private BigDecimal ask;

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal open;

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal high;

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal low;

	@Column(nullable = false, precision = 8, scale = 4)
	private BigDecimal close;

	@Column(nullable = false)
	private Long volume;

	@Column(nullable = false)
	private BigDecimal value;

	@Column(name = "foreign_buy_sell", precision = 16, scale = 4)
	private BigDecimal foreignBuySell;

	@ManyToOne
	private Stock stock;

	public Quote() {
		super();
	}

	public Quote(Stock stock) {
		this.stock = stock;
	}

	/**
	 * Used for NEW jpql construct.
	 */
	public Quote(Date date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume) {
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
	}

	public static Quote convert(PseReportRow row) {
		Quote quote = new Quote();
		quote.setDate(row.getReport().getDate());
		quote.setBid(row.getBid());
		quote.setAsk(row.getAsk());
		quote.setOpen(row.getOpen());
		quote.setHigh(row.getHigh());
		quote.setLow(row.getLow());
		quote.setClose(row.getClose());
		quote.setVolume(row.getVolume());
		quote.setValue(row.getValue());
		quote.setForeignBuySell(row.getForeignBuySell());
		return quote;
	}

	public int getBoardLot() {
		return BoardLotAndPriceFluctuation.determineBoardLot(close);
	}

	public BigDecimal getPriceFluctuation() {
		return BoardLotAndPriceFluctuation.determinePriceFluctuation(close);
	}

	public Long getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}

	@Override
	public String toString() {
		return String.format(
				"Quote [id=%s, date=%s, bid=%s, ask=%s, open=%s, high=%s, low=%s, close=%s, volume=%s, value=%s, foreignBuySell=%s, stock.id=%s]",
				id, date, bid, ask, open, high, low, close, volume, value, foreignBuySell, stock == null ? null : stock.getId());
	}

}
