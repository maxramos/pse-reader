package ph.mar.psereader.business.indicator.entity;

public enum MomentumType {

	BULLISH("Bullish"),
	BEARISH("Bearish"),
	NEUTRAL("Neutral");

	private String name;

	private MomentumType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
