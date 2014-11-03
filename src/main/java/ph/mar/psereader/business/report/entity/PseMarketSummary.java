package ph.mar.psereader.business.report.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PseMarketSummary implements Serializable {

	private static final long serialVersionUID = 1L;

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
		return String.format(
				"PseMarketSummary [advancesCount=%s, declinesCount=%s, unchangedCount=%s, tradesCount=%s, totalForeignBuy=%s, totalForeignSell=%s]",
				advancesCount, declinesCount, unchangedCount, tradesCount, totalForeignBuy, totalForeignSell);
	}

}
