package ph.mar.psereader.business.indicator.entity;

public enum RecommendationType {

	BUY("Buy"),
	MUST_BUY("Must Buy"),
	BUY_WARNING("Buy Warning"),
	HOLD("Hold"),
	SELL_WARNING("Sell Warning"),
	SELL("Sell"),
	MUST_SELL("Must Sell");

	private String name;

	private RecommendationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
