package gov.hhs.onc.leap.ui.views.acorn;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.views.ViewFrame;

@PageTitle("ACORN Project - Personal Safety")
@Route(value = "personalsafety", layout = MainLayout.class)
public class PersonalSafety extends ViewFrame {

    public PersonalSafety() {
        setId("personalsafety");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Personal Safety");
        Html intro = new Html("<p>Exposure to abuse and violence includes intimate " +
                "partner violence (IPV) and elder abuse, among other " +
                "forms of exposure to violence from friends and loved " +
                "ones. In addition to immediate safety concerns " +
                "and physical injuries, exposure to abuse and " +
                "violence can promote emotional and mental health " +
                "conditions like depression and PTSD.</p>");
        FlexBoxLayout content = new FlexBoxLayout(header, intro);
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
