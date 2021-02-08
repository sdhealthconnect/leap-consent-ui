package gov.hhs.onc.leap.ui.layout.size;

public interface Size {

	// Margins and paddings can have multiple attributes (e.g. horizontal and
	// vertical)
	public String[] getMarginAttributes();

	public String[] getPaddingAttributes();

	// Spacing is applied via the class attribute
	public String getSpacingClassName();

	// Returns the size variable (Lumo custom property)
	public String getVariable();

}
