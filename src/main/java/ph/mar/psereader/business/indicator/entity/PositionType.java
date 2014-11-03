package ph.mar.psereader.business.indicator.entity;

public enum PositionType {

	ENTER("Enter"),
	EXIT("Exit"),
	UP_WARNING("Up Warning"),
	DOWN_WARNING("Down Warning"),
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
