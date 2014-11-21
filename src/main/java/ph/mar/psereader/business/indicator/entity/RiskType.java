package ph.mar.psereader.business.indicator.entity;

public enum RiskType {

	SAFE("Safe"),
	DANGER("Danger"),
	CRITICAL("Critical");

	private String name;

	private RiskType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
