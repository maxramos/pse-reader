package ph.mar.psereader.business.indicator.entity;

public enum ActionType {

	BUY("Buy"),
	SELL("Sell"),
	HOLD("Hold");

	private String name;

	private ActionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
