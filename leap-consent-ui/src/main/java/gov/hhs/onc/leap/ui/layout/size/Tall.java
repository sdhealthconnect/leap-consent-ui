package gov.hhs.onc.leap.ui.layout.size;

public enum Tall implements Size {

	XS("var(--lumo-space-xs)", "spacing-tall-xs"), S("var(--lumo-space-s)",
			"spacing-tall-s"), M("var(--lumo-space-m)", "spacing-tall-m"), L(
			"var(--lumo-space-l)", "spacing-tall-l"), XL(
			"var(--lumo-space-xl)", "spacing-tall-xl");

	private String variable;
	private String spacingClassName;

	Tall(String variable, String spacingClassName) {
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
