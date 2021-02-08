package gov.hhs.onc.leap.ui.util;

public enum FontSize {

	XXS("var(--lumo-font-size-xxs)"),
	XS("var(--lumo-font-size-xs)"),
	S("var(--lumo-font-size-s)"),
	M("var(--lumo-font-size-m)"),
	L("var(--lumo-font-size-l)"),
	XL("var(--lumo-font-size-xl)"),
	XXL("var(--lumo-font-size-xxl)"),
	XXXL("var(--lumo-font-size-xxxl)");

	private String value;

	FontSize(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
