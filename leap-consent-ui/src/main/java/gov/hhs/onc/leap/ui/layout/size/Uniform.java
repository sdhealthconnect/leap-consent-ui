package gov.hhs.onc.leap.ui.layout.size;

public enum Uniform implements Size {

	AUTO("auto", null),

	XS("var(--lumo-space-xs)", "spacing-xs"), S("var(--lumo-space-s)",
			"spacing-s"), M("var(--lumo-space-m)", "spacing-m"), L(
			"var(--lumo-space-l)",
			"spacing-l"), XL("var(--lumo-space-xl)", "spacing-xl"),

	RESPONSIVE_M("var(--lumo-space-r-m)",
			null), RESPONSIVE_L("var(--lumo-space-r-l)", null);

	private String variable;
	private String spacingClassName;

	Uniform(String variable, String spacingClassName) {
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
