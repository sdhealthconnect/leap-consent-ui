package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Right;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.layout.size.Uniform;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;

@PageTitle("LEAP FHIR Consent - Home")
@Route(value = "", layout = MainLayout.class)
public class Home extends ViewFrame {

	public Home() {
		setId("home");
		setViewContent(createContent());
	}

	private Component createContent() {
		Html intro = new Html("<p><b>LEAP FHIR Consent</b> client demonstration platform for <b>HL7 FHIR Connectathon 26</b> " +
				"will utilize FHIR consent, investigate workflows in a number of key use cases including but not limited too "+
				"Consent to \"Share\", Consent to \"Treat\", Consent for \"Research\", and \"Advanced Directives\". "+
				"The use of FHIR based Consent Decision Service(CDS), Consent Enforcement Services(CES), " +
				"Security Labeling Services(SLS), and Privacy Protective Services(PPS) previously demonstrated " +
				"again will lay the foundation for this track.");


		Anchor documentation = new Anchor("https://sdhealthconnect.github.io/leap/", UIUtils.createButton("Read the documentation", VaadinIcon.EXTERNAL_LINK));
		Anchor starter = new Anchor("https://github.com/sdhealthconnect", UIUtils.createButton("Access LEAP Projects on GitHub", VaadinIcon.EXTERNAL_LINK));

		FlexBoxLayout links = new FlexBoxLayout(documentation, starter);
		links.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		links.setSpacing(Right.S);

		FlexBoxLayout content = new FlexBoxLayout(intro, links);
		content.setBoxSizing(BoxSizing.BORDER_BOX);
		content.setHeightFull();
		content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

		content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
		content.setMargin(Horizontal.AUTO);
		content.setMaxWidth("840px");
		//content.setPadding(Uniform.RESPONSIVE_L);
		return content;
	}

}
