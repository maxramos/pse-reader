package ph.mar.psereader.business.indicator.entity;

public enum VolatilityType {

	WIDE_RANGING("Wide Ranging"),
	BREAKOUT("Breakout"),
	NORMAL("Normal");

	private String name;

	private VolatilityType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
