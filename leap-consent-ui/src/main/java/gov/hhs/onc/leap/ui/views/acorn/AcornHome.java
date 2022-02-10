package gov.hhs.onc.leap.ui.views.acorn;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.views.ViewFrame;

@PageTitle("ACORN Project - Home")
@Route(value = "acornhome", layout = MainLayout.class)
public class AcornHome extends ViewFrame {

    public AcornHome() {
        setId("acornhome");
        setViewContent(createContent());
    }

    private Component createContent() {
        String fullFormPath = UIUtils.IMG_PATH + "logos";
        Image logo = UIUtils.createImage(fullFormPath,"acornproject.png", "");
        H1 header = new H1("Veterans Facing Health-Related Social Needs");
        Html intro = new Html("<p><b>The ACORN initiative uses an 11 question assessment</b> to screen Veterans for " +
                "non-clinical needs to provide resources at the point of clinical care. The assessment" +
                "currently screens for the following nine domains of health-related social needs: food, " +
                "housing security; utility, transportation, legal, " +
                "educational, and employment needs; and " +
                "personal safety and social support.");
        FlexBoxLayout content = new FlexBoxLayout(logo, header, intro);
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
