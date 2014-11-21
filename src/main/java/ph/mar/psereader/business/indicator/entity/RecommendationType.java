package ph.mar.psereader.business.indicator.entity;

public enum RecommendationType {

	BUY("Buy"),
	SELL("Sell"),
	HOLD("Hold");

	private String name;

	private RecommendationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
