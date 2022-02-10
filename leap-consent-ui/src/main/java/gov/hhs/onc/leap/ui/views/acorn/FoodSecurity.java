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

@PageTitle("ACORN Project - Food Security")
@Route(value = "foodsecurity", layout = MainLayout.class)
public class FoodSecurity extends ViewFrame {

    public FoodSecurity() {
        setId("foodsecurity");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Food Security");
        Html intro = new Html("<p>Food insecurity is the disruption of typical eating habits " +
                "due to lack of income and other resources. Nearly onequarter of Veterans receiving care in the VA Healthcare " +
                "System report experiencing food insecurity, double the " +
                "rate for the US population of 12%.</p>");
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
