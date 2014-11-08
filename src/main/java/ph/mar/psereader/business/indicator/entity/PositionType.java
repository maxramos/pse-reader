package ph.mar.psereader.business.indicator.entity;

public enum PositionType {

	ENTER("Enter"),
	EXIT("Exit"),
	ENTER_WARNING("Enter Warning"),
	EXIT_WARNING("Exit Warning"),
	RISING("Rising"),
	FALLING("Falling"),
	HOLD("Hold");

	private String name;

	private PositionType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
