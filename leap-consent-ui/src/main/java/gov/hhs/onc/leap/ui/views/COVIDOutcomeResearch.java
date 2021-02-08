package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Uniform;

@PageTitle("COVID Outcome Research")
@Route(value = "covidoutcomeresearchview", layout = MainLayout.class)
public class COVIDOutcomeResearch extends ViewFrame {

    public COVIDOutcomeResearch() {
        setId("covidoutcomeresearchview");
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        Html text1 = new Html("<p><b>I would like to participate in the COVID Outcome Studies.\n" +
                "To do so I authorize my Regional Health Information Exchange (HIE)</b> </p>");

        TextField hie = new TextField();
        hie.setReadOnly(true);
        Label hieLbl = new Label("This has been auto-populated based on your identity");
        hieLbl.getStyle().set("font-size", "8px");
        hieLbl.getStyle().set("color", "#00ccff");

        Html text2 = new Html("<p><b>To release my clinical record to the outcome study for next 12 months.</b></p>");
        Html text3 = new Html("<p><b>I have additional privacy concerns and wish to remove any information\n" +
                "that may be sensitive in nature and not applicable to this study \n" +
                "prior to submission.</b></p>");

        Checkbox sensBox = new Checkbox("Checking this box will remove information from your clinical record that is determined to be privacy sensitive");
        sensBox.getStyle().set("color", "#00ccff");



        Button signAndSubmit = new Button("Sign and Submit Authorization");
        signAndSubmit.getStyle().set("background-color", "#fce303");
        signAndSubmit.getStyle().set("border", "3px solid #304391");
        signAndSubmit.getStyle().set("margin-top", "50px");



        FlexBoxLayout content = new FlexBoxLayout(text1, hie, hieLbl, text2, text3, sensBox, signAndSubmit);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;
    }

}
