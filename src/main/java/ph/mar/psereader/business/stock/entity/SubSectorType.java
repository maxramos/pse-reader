package ph.mar.psereader.business.stock.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Count (Total - 21):
 * FINANCIALS - 2
 * INDUSTRIAL - 6
 * HOLDING FIRMS - 1
 * PROPERTY - 1
 * SERVICES - 9
 * MINING AND OIL - 2
 */
public enum SubSectorType {

	BANK("Banks", "**** BANKS ****", SectorType.FIN),
	CG("Casinos & Gaming", "**** CASINOS & GAMING ****", SectorType.SVC),
	CHEM("Chemicals", "**** CHEMICALS ****", SectorType.IND),
	CIAS("Construction, Infrastructure & Allied Services", "**** CONSTRUCTION, INFRASTRUCTURE & ALLIED SERVICES ****", SectorType.IND),
	EDU("Education", "**** EDUCATION ****", SectorType.SVC),
	ECE("Electrical Components & Equipment", "**** ELECTRICAL COMPONENTS & EQUIPMENT ****", SectorType.IND),
	EEPW("Electricity, Energy, Power & Water", "**** ELECTRICITY, ENERGY, POWER & WATER ****", SectorType.IND),
	ETF("Exchange Traded Funds", "", null), // Not an index sub-sector
	FBT("Food, Beverage & Tobacco", "**** FOOD, BEVERAGE & TOBACCO ****", SectorType.IND),
	HLDG("Holding Firms", "**** HOLDING FIRMS ****", SectorType.HLDG),
	HL("Hotel & Leisure", "**** HOTEL & LEISURE ****", SectorType.SVC),
	IT("Information Technology", "**** INFORMATION TECHNOLOGY ****", SectorType.SVC),
	MEDIA("Media", "**** MEDIA ****", SectorType.SVC),
	MIN("Mining", "**** MINING ****", SectorType.MO),
	OIL("Oil", "**** OIL ****", SectorType.MO),
	OFI("Other Financial Institutions", "**** OTHER FINANCIAL INSTITUTIONS ****", SectorType.FIN),
	OI("Other Industrials", "**** OTHER INDUSTRIALS ****", SectorType.IND),
	OS("Other Services", "**** OTHER SERVICES ****", SectorType.SVC),
	PDR("Phil. Depositary Receipts", "", null), // Not an index sub-sector
	PREF("Preferred", "", null), // Not an index sub-sector
	PRO("Property", "**** PROPERTY ****", SectorType.PRO),
	RET("Retail", "**** RETAIL ****", SectorType.SVC),
	SME("Small & Medium Enterprises", "", null), // Not an index sub-sector
	TELE("Telecommunications", "**** TELECOMMUNICATIONS ****", SectorType.SVC),
	TS("Transportation Services", "**** TRANSPORTATION SERVICES ****", SectorType.SVC),
	WRT("Warrants", "", null); // Not an index sub-sector

	private String name;
	private String regex;
	private SectorType sector;

	private SubSectorType(String name, String regex, SectorType sector) {
		this.name = name;
		this.regex = regex;
		this.sector = sector;
	}

	public String getName() {
		return name;
	}

	public String getRegex() {
		return regex;
	}

	public SectorType getSector() {
		return sector;
	}

	public static SubSectorType get(String regex) {
		String trimmedRegex = regex.trim();

		for (SubSectorType subSector : values()) {
			if (subSector.getRegex().equals(trimmedRegex)) {
				return subSector;
			}
		}

		return null;
	}

	public static SubSectorType[] subSectorsOf(SectorType sector) {
		if (sector == null) {
			return new SubSectorType[0];
		}

		List<SubSectorType> subSectors = new ArrayList<>();

		for (SubSectorType subSector : SubSectorType.values()) {
			if (subSector.getSector() == sector) {
				subSectors.add(subSector);
			}
		}

		return subSectors.toArray(new SubSectorType[0]);
	}

}
