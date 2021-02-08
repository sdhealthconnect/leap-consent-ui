package gov.hhs.onc.leap.ui.util.css;

public enum AlignItems {

	BASELINE("baseline"),
	CENTER("center"),
	END("end"),
	FLEX_END("flex-end"),
	FLEX_START("flex-start"),
	LEFT("left"),
	NORMAL("normal"),
	RIGHT("right"),
	SELF_END("self-end"),
	SELF_START("self-start"),
	START("start"),
	STRETCH("stretch");

	private String value;

	AlignItems(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
