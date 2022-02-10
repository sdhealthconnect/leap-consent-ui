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

@PageTitle("ACORN Project - Utility Needs")
@Route(value = "utilityneeds", layout = MainLayout.class)
public class UtilityNeeds extends ViewFrame {

    public UtilityNeeds() {
        setId("utilityneeds");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Utility Needs");
        Html intro = new Html("<p>With nearly 1.4 million Veterans at risk for " +
                "homelessness, utility bill assistance is an essential " +
                "benefit for Veterans with financial burdens. Over " +
                "666,000 Veterans in low-income households paid more " +
                "than half their income for rent and utilities in 2017.</p>");
        FlexBoxLayout content = new FlexBoxLayout(header,intro);
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
