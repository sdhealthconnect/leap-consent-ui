package gov.hhs.onc.leap.ui.util.css;

public enum BorderRadius {

	S("var(--lumo-border-radius-s)"), M("var(--lumo-border-radius-m)"), L(
			"var(--lumo-border-radius-l)"),

	_50("50%");

	private String value;

	BorderRadius(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
