package ph.mar.psereader.business.stock.entity;

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

	BANK("Banks", "**** BANKS ****"), // FINANCIALS
	CG("Casinos & Gaming", "**** CASINOS & GAMING ****"), // SERVICES
	CHEM("Chemicals", "**** CHEMICALS ****"), // INDUSTRIAL
	CIAS("Construction, Infrastructure & Allied Services", "**** CONSTRUCTION, INFRASTRUCTURE & ALLIED SERVICES ****"), // INDUSTRIAL
	EDU("Education", "**** EDUCATION ****"), // SERVICES
	ECE("Electrical Components & Equipment", "**** ELECTRICAL COMPONENTS & EQUIPMENT ****"), // INDUSTRIAL
	EEPW("Electricity, Energy, Power & Water", "**** ELECTRICITY, ENERGY, POWER & WATER ****"), // INDUSTRIAL
	ETF("Exchange Traded Funds", ""), // Not an index sub-sector
	FBT("Food, Beverage & Tobacco", "**** FOOD, BEVERAGE & TOBACCO ****"), // INDUSTRIAL
	HLDG("Holding Firms", "**** HOLDING FIRMS ****"), // HOLDING FIRMS
	HL("Hotel & Leisure", "**** HOTEL & LEISURE ****"), // SERVICES
	IT("Information Technology", "**** INFORMATION TECHNOLOGY ****"), // SERVICES
	MEDIA("Media", "**** MEDIA ****"), // SERVICES
	MIN("Mining", "**** MINING ****"), // MINING AND OIL
	OIL("Oil", "**** OIL ****"), // MINING AND OIL
	OFI("Other Financial Institutions", "**** OTHER FINANCIAL INSTITUTIONS ****"), // FINANCIALS
	OI("Other Industrials", "**** OTHER INDUSTRIALS ****"), // INDUSTRIAL
	OS("Other Services", "**** OTHER SERVICES ****"), // SERVICES
	PDR("Phil. Depositary Receipts", ""), // Not an index sub-sector
	PREF("Preferred", ""), // Not an index sub-sector
	PRO("Property", "**** PROPERTY ****"), // PROPERTY
	RET("Retail", "**** RETAIL ****"), // SERVICES
	SME("Small & Medium Enterprises", ""), // Not an index sub-sector
	TELE("Telecommunications", "**** TELECOMMUNICATIONS ****"), // SERVICES
	TS("Transportation Services", "**** TRANSPORTATION SERVICES ****"), // SERVICES
	WRT("Warrants", ""); // Not an index sub-sector

	private String name;
	private String regex;

	private SubSectorType(String name, String regex) {
		this.name = name;
		this.regex = regex;
	}

	public String getName() {
		return name;
	}

	public String getRegex() {
		return regex;
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

}
