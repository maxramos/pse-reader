package ph.mar.psereader.business.stock.entity;

import java.util.ArrayList;
import java.util.List;

public enum SectorType {

	ETF("Exchange Traded Funds", "E X C H A N G E   T R A D E D   F U N D S", "ETF", false), // Not an index sector
	FIN("Financials", "F I N A N C I A L S", "BANK", true),
	HLDG("Holding Firms", "H O L D I N G   F I R M S", "HLDG", true),
	IND("Industrial", "I N D U S T R I A L", "EEPW", true),
	MO("Mining & Oil", "M I N I N G   &   O I L", "MIN", true),
	PDR("Phil. Depositary Receipts", "P H I L .   D E P O S I T A R Y   R E C E I P T S", "PDR", false), // Not an index sector
	PREF("Preferred", "P R E F E R R E D", "PREF", false), // Not an index sector
	PRO("Property", "P R O P E R T Y", "PRO", true),
	SVC("Services", "S E R V I C E S", "MEDIA", true),
	SME("Small & Medium Enterprises", "S M A L L   &   M E D I U M   E N T E R P R I S E S", "SME", true), // Not an index sector
	WRT("Warrants", "W A R R A N T S", "WRT", false); // Not an index sector

	private String name;
	private String regex;
	private String firstSubSector;
	private boolean stockRelated;

	private SectorType(String name, String regex, String firstSubSector, boolean stockRelated) {
		this.name = name;
		this.regex = regex;
		this.firstSubSector = firstSubSector;
		this.stockRelated = stockRelated;
	}

	public String getName() {
		return name;
	}

	public String getRegex() {
		return regex;
	}

	public String getFirstSubSector() {
		return firstSubSector;
	}

	public boolean isStockRelated() {
		return stockRelated;
	}

	public static SectorType get(String regex) {
		String trimmedRegex = regex.trim();

		for (SectorType sector : values()) {
			if (sector.getRegex().equals(trimmedRegex)) {
				return sector;
			}
		}

		return null;
	}

	public static SectorType[] getStockRelatedSectors() {
		List<SectorType> sectors = new ArrayList<>();

		for (SectorType sector : SectorType.values()) {
			if (sector.isStockRelated()) {
				sectors.add(sector);
			}
		}

		return sectors.toArray(new SectorType[0]);
	}

}
