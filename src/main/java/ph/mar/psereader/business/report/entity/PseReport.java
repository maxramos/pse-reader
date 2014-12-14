package ph.mar.psereader.business.report.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ph.mar.psereader.business.market.entity.MarketSummary;
import ph.mar.psereader.business.pseindex.entity.PseIndex;

public class PseReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date date;
	private List<PseReportRow> rows;
	private MarketSummary marketSummary;
	private List<PseIndex> indices;
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

	public Date getDate() {
		return date;
	}

	public List<PseReportRow> getRows() {
		return rows;
	}

	public MarketSummary getMarketSummary() {
		return marketSummary;
	}

	public void setMarketSummary(MarketSummary marketSummary) {
		this.marketSummary = marketSummary;
	}

	public List<PseIndex> getIndices() {
		return indices;
	}

	public void setIndices(List<PseIndex> indices) {
		this.indices = indices;
	}

	public List<String> getSuspendedStocks() {
		return suspendedStocks;
	}

	public void setSuspendedStocks(List<String> suspendedStocks) {
		this.suspendedStocks = suspendedStocks;
	}

	@Override
	public String toString() {
		return String.format("PseReport [date=%s, rows=%s, marketSummary=%s, indices=%s, suspendedStocks=%s]", date, rows, marketSummary, indices,
				suspendedStocks);
	}

}
