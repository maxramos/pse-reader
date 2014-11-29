package ph.mar.psereader.business.indicator.entity;

public enum RiskType {

	LOW("Low"),
	MODERATE("Moderate"),
	HIGH("High");

	private String name;

	private RiskType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
