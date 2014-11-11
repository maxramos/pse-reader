package ph.mar.psereader.business.indicator.entity;

public enum ActionType {

	BUY("Buy"),
	HOLD("Hold"),
	SELL("Sell");

	private String name;

	private ActionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
