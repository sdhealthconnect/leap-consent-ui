package gov.hhs.onc.leap.ui.components.navigation.tab;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import gov.hhs.onc.leap.ui.util.FontSize;
import gov.hhs.onc.leap.ui.util.UIUtils;

public class ClosableNaviTab extends NaviTab {

	private final Button close;

	public ClosableNaviTab(String label,
	                       Class<? extends Component> navigationTarget) {
		super(label, navigationTarget);
		getElement().setAttribute("closable", true);

		close = UIUtils.createButton(VaadinIcon.CLOSE, ButtonVariant.LUMO_TERTIARY_INLINE);
		// ButtonVariant.LUMO_SMALL isn't small enough.
		UIUtils.setFontSize(FontSize.XXS, close);
		add(close);
	}

	public Button getCloseButton() {
		return close;
	}
}
