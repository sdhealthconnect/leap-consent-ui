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

@PageTitle("ACORN Project - Housing Insecurity")
@Route(value = "housinginsecurity", layout = MainLayout.class)
public class HousingInsecurity extends ViewFrame {

    public HousingInsecurity() {
        setId("housinginsecurity");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Housing Insecurity");
        Html intro = new Html("<p>Housing instability encompasses a number of " +
                "challenges, including homelessness. Over 40,000 " +
                "Veterans experience homelessness on any given day, " +
                "and are more likely to experience poorer physical and " +
                "mental health outcomes than the general US population " +
                "who are homeless.</p>");
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
