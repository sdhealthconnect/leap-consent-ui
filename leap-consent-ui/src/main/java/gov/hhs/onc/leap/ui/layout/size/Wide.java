package gov.hhs.onc.leap.ui.layout.size;

public enum Wide implements Size {

	XS("var(--lumo-space-wide-xs)", "spacing-wide-xs"), S(
			"var(--lumo-space-wide-s)",
			"spacing-wide-s"), M("var(--lumo-space-wide-m)",
			"spacing-wide-m"), L("var(--lumo-space-wide-l)",
			"spacing-wide-l"), XL("var(--lumo-space-wide-xl)",
			"spacing-wide-xl"),

	RESPONSIVE_M("var(--lumo-space-wide-r-m)",
			null), RESPONSIVE_L("var(--lumo-space-wide-r-l)", null);

	private String variable;
	private String spacingClassName;

	Wide(String variable, String spacingClassName) {
		this.variable = variable;
		this.spacingClassName = spacingClassName;
	}

	@Override
	public String[] getMarginAttributes() {
		return new String[]{"margin"};
	}

	@Override
	public String[] getPaddingAttributes() {
		return new String[]{"padding"};
	}

	@Override
	public String getSpacingClassName() {
		return this.spacingClassName;
	}

	@Override
	public String getVariable() {
		return this.variable;
	}
}
