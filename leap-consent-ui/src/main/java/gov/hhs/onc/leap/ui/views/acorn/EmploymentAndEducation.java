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

@PageTitle("ACORN Project - Employment and Education")
@Route(value = "employmentandeducation", layout = MainLayout.class)
public class EmploymentAndEducation extends ViewFrame {

    public EmploymentAndEducation() {
        setId("employmentandeducation");
        setViewContent(createContent());
    }

    private Component createContent() {
        H1 header = new H1("Employment and Education");
        Html intro = new Html("<p>Transferring the skills and knowledge learned during " +
                "their military service can prove difficult for Veterans, " +
                "requiring many to complete additional schooling to meet " +
                "civilian certification standards. Difficulty finding " +
                "employment can further exacerbate financial strain, " +
                "making it difficult to afford basic needs such as food, " +
                "housing, utilities, healthcare costs.</p>");
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
