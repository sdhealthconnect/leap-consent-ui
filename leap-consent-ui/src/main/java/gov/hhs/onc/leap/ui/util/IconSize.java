package gov.hhs.onc.leap.ui.util;

public enum IconSize {

	S("size-s"),
	M("size-m"),
	L("size-l");

	private String style;

	IconSize(String style) {
		this.style = style;
	}

	public String getClassName() {
		return style;
	}

}
