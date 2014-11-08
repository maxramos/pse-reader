package ph.mar.psereader.business.market.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "market_summary", indexes = @Index(columnList = "date"))
@NamedQueries({ @NamedQuery(name = MarketSummary.ALL, query = "SELECT ms FROM MarketSummary ms ORDER BY ms.date DESC"),
	@NamedQuery(name = MarketSummary.ALL_DATES, query = "SELECT ms.date FROM MarketSummary ms ORDER BY ms.date DESC"),
	@NamedQuery(name = MarketSummary.ALL_DATES_BY_DATE, query = "SELECT ms.date FROM MarketSummary ms WHERE ms.date > :date ORDER BY ms.date"),
	@NamedQuery(name = MarketSummary.BY_DATE, query = "SELECT ms FROM MarketSummary ms WHERE ms.date = :date") })
public class MarketSummary implements Serializable {

	public static final String ALL = "MarketSummary.ALL";
	public static final String ALL_DATES = "MarketSummary.ALL_DATES";
	public static final String ALL_DATES_BY_DATE = "PseReport.ALL_DATES_BY_DATE";
	public static final String BY_DATE = "PseReport.BY_DATE";

	private static final long serialVersionUID = 1L;

	@Id
	@SequenceGenerator(name = "seq_market_summary", sequenceName = "seq_market_summary", allocationSize = 1)
	@GeneratedValue(generator = "seq_market_summary")
	private Long id;

	@Temporal(TemporalType.DATE)
	@Column(nullable = false, unique = true)
	private Date date;

	@Column(name = "advances_count", nullable = false)
	private int advancesCount;

	@Column(name = "declines_count", nullable = false)
	private int declinesCount;

	@Column(name = "unchanged_count", nullable = false)
	private int unchangedCount;

	@Column(name = "trades_count", nullable = false)
	private int tradesCount;

	@Column(name = "total_foreign_buy", nullable = false, precision = 16, scale = 4)
	private BigDecimal totalForeignBuy;

	@Column(name = "total_foreign_sell", nullable = false, precision = 16, scale = 4)
	private BigDecimal totalForeignSell;

	public MarketSummary() {
		super();
	}

	public MarketSummary(Date date) {
		this.date = date;
	}

	public int getTradedStocksCount() {
		return advancesCount + declinesCount + unchangedCount;
	}

	public BigDecimal getNetForeign() {
		return totalForeignBuy.subtract(totalForeignSell);
	}

	public BigDecimal getTotalForeign() {
		return totalForeignBuy.add(totalForeignSell);
	}

	public int getAdvancesCount() {
		return advancesCount;
	}

	public void setAdvancesCount(int advancesCount) {
		this.advancesCount = advancesCount;
	}

	public int getDeclinesCount() {
		return declinesCount;
	}

	public void setDeclinesCount(int declinesCount) {
		this.declinesCount = declinesCount;
	}

	public int getUnchangedCount() {
		return unchangedCount;
	}

	public void setUnchangedCount(int unchangedCount) {
		this.unchangedCount = unchangedCount;
	}

	public int getTradesCount() {
		return tradesCount;
	}

	public void setTradesCount(int tradesCount) {
		this.tradesCount = tradesCount;
	}

	public BigDecimal getTotalForeignBuy() {
		return totalForeignBuy;
	}

	public void setTotalForeignBuy(BigDecimal totalForeignBuy) {
		this.totalForeignBuy = totalForeignBuy;
	}

	public BigDecimal getTotalForeignSell() {
		return totalForeignSell;
	}

	public void setTotalForeignSell(BigDecimal totalForeignSell) {
		this.totalForeignSell = totalForeignSell;
	}

	@Override
	public String toString() {
		return String
				.format("MarketSummary [id=%s, date=%s, advancesCount=%s, declinesCount=%s, unchangedCount=%s, tradesCount=%s, totalForeignBuy=%s, totalForeignSell=%s]",
						id, date, advancesCount, declinesCount, unchangedCount, tradesCount, totalForeignBuy, totalForeignSell);
	}

}
