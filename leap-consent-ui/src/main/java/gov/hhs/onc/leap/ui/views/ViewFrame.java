package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import gov.hhs.onc.leap.ui.MainLayout;

/**
 * A view frame that establishes app design guidelines. It consists of three
 * parts:
 * <ul>
 * <li>Topmost {@link #setViewHeader(Component...) header}</li>
 * <li>Center {@link #setViewContent(Component...) content}</li>
 * <li>Bottom {@link #setViewFooter(Component...) footer}</li>
 * </ul>
 */
@CssImport("./styles/components/view-frame.css")
public class ViewFrame extends Composite<Div> implements HasStyle {

	private String CLASS_NAME = "view-frame";

	private Div header;
	private Div content;
	private Div footer;

	public ViewFrame() {
		setClassName(CLASS_NAME);

		header = new Div();
		header.setClassName(CLASS_NAME + "__header");

		content = new Div();
		content.setClassName(CLASS_NAME + "__content");

		footer = new Div();
		footer.setClassName(CLASS_NAME + "__footer");

		getContent().add(header, content, footer);
	}

	/**
	 * Sets the header slot's components.
	 */
	public void setViewHeader(Component... components) {
		header.removeAll();
		header.add(components);
	}

	/**
	 * Sets the content slot's components.
	 */
	public void setViewContent(Component... components) {
		content.removeAll();
		content.add(components);
	}

	/**
	 * Sets the footer slot's components.
	 */
	public void setViewFooter(Component... components) {
		footer.removeAll();
		footer.add(components);
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		MainLayout.get().getAppBar().reset();
	}

}
