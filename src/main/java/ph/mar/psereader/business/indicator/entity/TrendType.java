package ph.mar.psereader.business.indicator.entity;

public enum TrendType {

	UP("Up"),
	STRONG_UP("Strong Up"),
	DOWN("Down"),
	STRONG_DOWN("String Down"),
	SIDEWAYS("Sideways");

	private String name;

	private TrendType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
