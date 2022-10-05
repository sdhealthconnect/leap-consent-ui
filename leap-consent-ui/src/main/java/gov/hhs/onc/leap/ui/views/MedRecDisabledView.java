package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.util.UIUtils;

@PageTitle("Service Disabled")
@Route(value = "medrecservicedisabled", layout = MainLayout.class)
public class MedRecDisabledView extends ViewFrame {

    public MedRecDisabledView() {
        setViewContent(getView());
    }

    private VerticalLayout getView() {
        VerticalLayout v = new VerticalLayout();
        v.setSpacing(false);

        String fullFormPath = UIUtils.IMG_PATH + "logos";
        Image img = UIUtils.createImage(fullFormPath,"sdhealthconnect.png", "");
        img.setWidth("200px");
        v.add(img);

        v.add(new H2("This service view has been temporarily disabled."));
        v.add(new Paragraph("For more information on this functionality please contact Nic Hess at nhess@sdhealthconnect.com"));

        v.setSizeFull();
        v.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        v.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        getStyle().set("text-align", "center");
        return v;
    }

}
