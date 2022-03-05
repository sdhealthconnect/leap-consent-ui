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

@PageTitle("ACORN Project - Social Support")
@Route(value = "socialsupport", layout = MainLayout.class)
public class SocialSupport extends ViewFrame {

    public SocialSupport() {
        setId("socialsupport");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Social Support");
        Html intro = new Html("<p>The more a Veteran can identify sources of support in " +
                "their life, the higher the likelihood of them having " +
                "positive perceptions of belonging and experiencing " +
                "lower rates of isolation. With the Veteran suicide rate " +
                "being 1.5 times the rate for the US general population, " +
                "the presence of a social support system is closely linked " +
                "to a Veteran’s mental wellbeing and behaviors.</p>");
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
