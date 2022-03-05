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

@PageTitle("ACORN Project - Legal Support")
@Route(value = "legalsupport", layout = MainLayout.class)
public class LegalSupport extends ViewFrame {

    public LegalSupport() {
        setId("legalsupport");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Legal Support");
        Html intro = new Html("<p>Legal support is often an overlooked area of need for " +
                "Veterans, who may have difficulties addressing legal " +
                "issues such as divorce, child support/custody, benefit " +
                "appeals, and resolving disputes, among others. " +
                "Needing or using legal services can be a significant " +
                "stressor for Veterans, and can increase mental distress " +
                "and negatively impact their quality of life.</p>");
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
