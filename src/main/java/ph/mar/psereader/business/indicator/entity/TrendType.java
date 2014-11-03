package ph.mar.psereader.business.indicator.entity;

public enum TrendType {

	STRONG_UP("Strong Up"),
	STRONG_DOWN("Strong Down"),
	UP("Up"),
	DOWN("Down"),
	SIDEWAYS("Sideways");

	private String name;

	private TrendType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
