package ph.mar.psereader.business.stock.entity;

public enum SectorType {

	ETF("Exchange Traded Funds", "E X C H A N G E   T R A D E D   F U N D S", SubSectorType.ETF), // Not an index sector
	FIN("Financials", "F I N A N C I A L S", SubSectorType.BANK),
	HLDG("Holding Firms", "H O L D I N G   F I R M S", SubSectorType.HLDG),
	IND("Industrial", "I N D U S T R I A L", SubSectorType.EEPW),
	MO("Mining & Oil", "M I N I N G   &   O I L", SubSectorType.MIN),
	PDR("Phil. Depositary Receipts", "P H I L .   D E P O S I T A R Y   R E C E I P T S", SubSectorType.PDR), // Not an index sector
	PREF("Preferred", "P R E F E R R E D", SubSectorType.PREF), // Not an index sector
	PRO("Property", "P R O P E R T Y", SubSectorType.PRO),
	SVC("Services", "S E R V I C E S", SubSectorType.MEDIA),
	SME("Small & Medium Enterprises", "S M A L L   &   M E D I U M   E N T E R P R I S E S", SubSectorType.SME), // Not an index sector
	WRT("Warrants", "W A R R A N T S", SubSectorType.WRT); // Not an index sector

	private String name;
	private String regex;
	private SubSectorType firstSubSector;

	private SectorType(String name, String regex, SubSectorType firstSubSector) {
		this.name = name;
		this.regex = regex;
		this.firstSubSector = firstSubSector;
	}

	public String getName() {
		return name;
	}

	public String getRegex() {
		return regex;
	}

	public SubSectorType getFirstSubSector() {
		return firstSubSector;
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

}
