package gov.hhs.onc.leap.ui.util.css.lumo;

public enum BadgeShape {

	NORMAL("normal"), PILL("pill");

	private String style;

	BadgeShape(String style) {
		this.style = style;
	}

	public String getThemeName() {
		return style;
	}

}
