package gov.hhs.onc.leap.ui.components.navigation.drawer;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.StreamResource;
import gov.hhs.onc.leap.ui.util.UIUtils;
import org.apache.commons.io.IOUtils;


import java.io.ByteArrayInputStream;
import java.io.IOException;

@CssImport("./styles/components/brand-expression.css")
public class BrandExpression extends Div {

	private String CLASS_NAME = "brand-expression";

	private Image logo;
	private Label title;

	public BrandExpression(String text) {
		setClassName(CLASS_NAME);
		String fullFormPath = UIUtils.IMG_PATH + "logos";
		logo = UIUtils.createImage(fullFormPath,"healthit-logo.png", "");
       	logo.setAlt(text + " logo");
		logo.setClassName(CLASS_NAME + "__logo");

		title = UIUtils.createH3Label(text);
		title.addClassName(CLASS_NAME + "__title");

		add(logo, title);
	}

}
