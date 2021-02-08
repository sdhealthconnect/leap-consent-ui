package gov.hhs.onc.leap.ui.util.css;

public enum PointerEvents {

	AUTO("auto"), NONE("none");

	private String value;

	PointerEvents(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
