package ph.mar.psereader.business.indicator.entity;

public enum MovementType {

	UP("Up"),
	DOWN("Down"),
	UNCHANGED("Unchanged");

	private String name;

	private MovementType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
