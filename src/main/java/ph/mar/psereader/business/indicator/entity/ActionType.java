package ph.mar.psereader.business.indicator.entity;

public enum ActionType {

	BUY("Buy"),
	MUST_BUY("Must Buy"),
	BUY_WARNING("Buy Warning"),
	HOLD("Hold"),
	SELL_WARNING("Sell Warning"),
	SELL("Sell"),
	MUST_SELL("Must Sell");

	private String name;

	private ActionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
