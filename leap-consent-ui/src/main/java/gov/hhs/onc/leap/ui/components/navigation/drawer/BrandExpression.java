package gov.hhs.onc.leap.ui.components.navigation.drawer;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.StreamResource;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.views.LivingWill;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;

@CssImport("./styles/components/brand-expression.css")
public class BrandExpression extends Div {
	private static final Logger log = LoggerFactory.getLogger(BrandExpression.class);

	private String CLASS_NAME = "brand-expression";

	private Image logo;
	private Label title;

	public BrandExpression(String text) {
		setClassName(CLASS_NAME);

		String fullFormPath = "/images/logos/healthit-logo.png";
		try {
			byte[] imageBytes = IOUtils.toByteArray(getClass().getResourceAsStream(fullFormPath));
			StreamResource resource = new StreamResource("healthit-logo.png", () -> new ByteArrayInputStream(imageBytes));
			logo = new Image(resource, "");
			logo.setAlt((text + " logo"));
			logo.setClassName(CLASS_NAME + "__logo");
		}
		catch (Exception ex) {
			log.error("Failed to stream app logo "+ex.getMessage());
		}

		title = UIUtils.createH3Label(text);
		title.addClassName(CLASS_NAME + "__title");

		add(logo, title);
	}

}
