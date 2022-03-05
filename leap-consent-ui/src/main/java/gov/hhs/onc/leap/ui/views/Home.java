package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
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
	private Image logo;

	public Home() {
		setId("home");
		setViewContent(createContent());
	}

	private Component createContent() {
		//Html intro = new Html(getTranslation("home-intro"));


		String fullFormPath = UIUtils.IMG_PATH + "logos";
		logo = UIUtils.createImage(fullFormPath,"aboutimage.png", "");
		logo.getStyle().set("background", "blue");


		Html intro = new Html("<p>This HIMSS 2022 Interoperability Showcase <b>Consent for Referral</b> demonstration is a notional <b>FHIR " +
				"based</b> implementation of <b>US Dept. of Veterans Affairs Acorn Initiative</b> " +
				"as it captures the Veterans answers to a suite of questions based on the CMS AHC HRSN Screening tool <b>encoding them in FHIR resources</b>.  " +
				"It identifies the Veteran's social needs and organizations that can provide help.</p>");
		Html intro2 = new Html("<p>This utilizes the LEAP FHIR Consent framework, an artifact of " +
				"a <b>US Office of National Coordinator For Health Information Technology (ONC)</b> multi-year <b>Leading Edge " +
				"Acceleration Project (LEAP)</b>. LEAP FHIR Consent " +
				"demonstrates a broad set of use-cases including privacy consent (or consent to share information), " +
				"consent for treatment, consent for research, advance care directives, DNR, National Portable Medical Order (POLST), " +
						"and now, <b>Social Determinants of Health (SDOH)</b>. " +
				"Special emphasis is given to <b>Computable Consents</b> --i.e., consents that record patient preferences in the " +
				"form of machine-readable rules by capturing and encoding them in the FHIR Consent provision structure. ");

		Anchor documentation = new Anchor("https://sdhealthconnect.github.io/leap/", UIUtils.createButton(getTranslation(getTranslation("home-Read_the_documentation")), VaadinIcon.EXTERNAL_LINK));
		Anchor starter = new Anchor("https://github.com/sdhealthconnect", UIUtils.createButton(getTranslation(getTranslation("home-Access_leap_projects_on_github")), VaadinIcon.EXTERNAL_LINK));

		FlexBoxLayout links = new FlexBoxLayout(documentation, starter);
		links.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		links.setSpacing(Right.S);

		FlexBoxLayout content = new FlexBoxLayout(logo, intro, intro2, links);
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
