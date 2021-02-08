package gov.hhs.onc.leap.ui.util.css;

public enum JustifyContent {

	BASELINE("baseline"),
	CENTER("center"),
	END("end"),
	FLEX_END("flex-end"),
	FLEX_START("flex-start"),
	LEFT("left"),
	RIGHT("right"),
	SPACE_AROUND("space-around"),
	SPACE_BETWEEN("space-between"),
	SPACE_EVENLY("space-evenly"),
	START("start"),
	STRETCH("stretch");

	private String value;

	JustifyContent(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
