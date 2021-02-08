package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Uniform;

import javax.swing.*;

@PageTitle("COVID Release My Records")
@Route(value = "covidconsenttoshare", layout = MainLayout.class)
public class COVIDReleaseMyRecords extends ViewFrame {

    public COVIDReleaseMyRecords() {
        setId("covidconsenttoshare");
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        Html text1 = new Html("<p><b>I authorize the healthcare professionals at</b> </p>");

        ComboBox hie = new ComboBox();
        Label hieLbl = new Label("Select provider from list");
        hieLbl.getStyle().set("font-size", "8px");
        hieLbl.getStyle().set("color", "#00ccff");

        Html text2 = new Html("<p><b>to share my immunization data with my regional\n" +
                "Healthcare Information Exchange, listed below,\n" +
                "and its members.\n</b></p>");


        TextField participant = new TextField();
        participant.setReadOnly(true);
        Label participantLbl = new Label("This has been auto-populated based on your identity");
        participantLbl.getStyle().set("font-size", "8px");
        participantLbl.getStyle().set("color", "#00ccff");


        Button signAndSubmit = new Button("Sign and Submit Authorization");
        signAndSubmit.getStyle().set("background-color", "#fce303");
        signAndSubmit.getStyle().set("border", "3px solid #304391");
        signAndSubmit.getStyle().set("margin-top", "50px");



        FlexBoxLayout content = new FlexBoxLayout(text1, hie, hieLbl, text2, participant, participantLbl, signAndSubmit);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;
    }

}
