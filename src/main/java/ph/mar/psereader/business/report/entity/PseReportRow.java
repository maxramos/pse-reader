package ph.mar.psereader.business.report.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import ph.mar.psereader.business.stock.entity.SectorType;
import ph.mar.psereader.business.stock.entity.SubSectorType;

public class PseReportRow implements Serializable {

	private static final long serialVersionUID = 1L;

	private String companyName; // 10 - N
	private String symbol; // 9
	private BigDecimal bid; // 8
	private BigDecimal ask; // 7
	private BigDecimal open; // 6
	private BigDecimal high; // 5
	private BigDecimal low; // 4
	private BigDecimal close; // 3
	private Long volume; // 2
	private BigDecimal value; // 1
	private BigDecimal foreignBuySell; // 0

	private PseReport report;
	private SectorType sector;
	private SubSectorType subSector;

	public PseReportRow(String symbol, SectorType sector, SubSectorType subSector) {
		this.symbol = symbol;
		this.sector = sector;
		this.subSector = subSector;
	}

	/**
	 * Used for NEW jpql construct.
	 */
	public PseReportRow(String companyName, String symbol, BigDecimal bid, BigDecimal ask, BigDecimal open, BigDecimal high, BigDecimal low,
			BigDecimal close, Long volume, BigDecimal value, BigDecimal foreignBuySell, SectorType sector, SubSectorType subSector) {
		this.companyName = companyName;
		this.symbol = symbol;
		this.bid = bid;
		this.ask = ask;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.volume = volume;
		this.value = value;
		this.foreignBuySell = foreignBuySell;
		this.sector = sector;
		this.subSector = subSector;
	}

	public boolean isValid() {
		return open != null && high != null && low != null && close != null && volume != null;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getSymbol() {
		return symbol;
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

	public SectorType getSector() {
		return sector;
	}

	public SubSectorType getSubSector() {
		return subSector;
	}

	public PseReport getReport() {
		return report;
	}

	public void setReport(PseReport report) {
		this.report = report;
	}

	@Override
	public String toString() {
		return String
				.format("PseReportRow [companyName=%s, symbol=%s, bid=%s, ask=%s, open=%s, high=%s, low=%s, close=%s, volume=%s, value=%s, foreignBuySell=%s, report=%s, sector=%s, subSector=%s]",
						companyName, symbol, bid, ask, open, high, low, close, volume, value, foreignBuySell, report, sector, subSector);
	}

}
