package gov.hhs.onc.leap.ui.layout.size;

public enum Vertical implements Size {

	AUTO("auto", null),

	XS("var(--lumo-space-xs)", "spacing-v-xs"), S("var(--lumo-space-s)",
			"spacing-v-s"), M("var(--lumo-space-m)", "spacing-v-m"), L(
			"var(--lumo-space-l)",
			"spacing-v-l"), XL("var(--lumo-space-xl)", "spacing-v-xl"),

	RESPONSIVE_M("var(--lumo-space-r-m)", null), RESPONSIVE_L(
			"var(--lumo-space-r-l)",
			null), RESPONSIVE_X("var(--lumo-space-r-x)", null);

	private String variable;
	private String spacingClassName;

	Vertical(String variable, String spacingClassName) {
		this.variable = variable;
		this.spacingClassName = spacingClassName;
	}

	@Override
	public String[] getMarginAttributes() {
		return new String[]{"margin-bottom", "margin-top"};
	}

	@Override
	public String[] getPaddingAttributes() {
		return new String[]{"padding-bottom", "padding-top"};
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
