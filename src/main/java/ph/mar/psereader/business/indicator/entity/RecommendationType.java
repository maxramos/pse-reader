package ph.mar.psereader.business.indicator.entity;

public enum RecommendationType {

	BUY("Buy"),
	TAKE_PROFIT("Take Profit"),
	HOLD("Hold"),
	SELL("Sell"),
	SELL_INTO_STRENGTH("Sell Into Strength"),
	RANGE_TRADE("Range Trade"),
	LIGHTEN("Lighten");

	private String name;

	private RecommendationType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
