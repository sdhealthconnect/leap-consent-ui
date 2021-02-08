package gov.hhs.onc.leap.ui.util;

public enum LineHeight {

	XS("var(--lumo-line-height-xs)"),
	S("var(--lumo-line-height-s)"),
	M("var(--lumo-line-height-m)");

	private String value;

	LineHeight(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
