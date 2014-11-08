package ph.mar.psereader.business.report.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ph.mar.psereader.business.index.entity.PseIndex;
import ph.mar.psereader.business.market.entity.MarketSummary;

public class PseReport implements Serializable {

	private static final long serialVersionUID = 1L;

	private Date date;
	private List<PseReportRow> rows;
	private MarketSummary marketSummary;
	private List<PseIndex> indeces;
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
		return String.format("PseReport [date=%s, rows=%s, marketSummary=%s, indeces=%s, suspendedStocks=%s]", date, rows, marketSummary, indeces,
				suspendedStocks);
	}

}
