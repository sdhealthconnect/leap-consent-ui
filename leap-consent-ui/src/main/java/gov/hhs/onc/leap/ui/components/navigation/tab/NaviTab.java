package gov.hhs.onc.leap.ui.components.navigation.tab;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;

import java.util.Objects;

public class NaviTab extends Tab {

	private Class<? extends Component> navigationTarget;

	public NaviTab(String label, Class<? extends Component> navigationTarget) {
		super(label);
		setNavigationTarget(navigationTarget);
	}

	public Class<? extends Component> getNavigationTarget() {
		return navigationTarget;
	}

	public void setNavigationTarget(
			Class<? extends Component> navigationTarget) {
		this.navigationTarget = Objects.requireNonNull(navigationTarget);
	}
}
