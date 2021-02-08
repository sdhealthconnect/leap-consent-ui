package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Uniform;

import java.awt.*;

@PageTitle("COVID-19 Authorize Treatment")
@Route(value = "covidauthtreatmentview", layout = MainLayout.class)
public class COVIDAuthorizeTreatment extends ViewFrame {

    public COVIDAuthorizeTreatment() {
        setId("covidauthtreatmentview");
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        Html auth1 = new Html("<p><b>I have been counseled about potential side effects after vaccination, "+
                "when they may occur, and when and where I should seek treatment. I am responsible for following up " +
                "with my physician at my expense if I experience any side effects.</b> </p>");
        Html auth2 = new Html("<p><b>I have read, or have had read to me, the Vaccine Information Statement(s) (“VIS”) " +
                "provided for the COVID vaccine(S) to be administered. I have had the opportunity to ask questions, and all " +
                "my questions have been answered to my satisfaction. I understand the benefits and risks of the vaccine(s). </p>");
        Html auth3 = new Html("<p><b>Through my authorization below, I consent to the administration of the COVID vaccine(s) by " +
                "a pharmacist or a supervised student pharmacist or technician, where permitted by law, and employed by</b></p>");

        TextField participant = new TextField("Authorized Practictioner or Organization");
        participant.setReadOnly(true);
        Label participantLbl = new Label("This has been auto-populated based on your provider selection");
        participantLbl.getStyle().set("font-size", "8px");
        participantLbl.getStyle().set("color", "#00ccff");


        Button signAndSubmit = new Button("Sign and Submit Authorization");
        signAndSubmit.getStyle().set("background-color", "#fce303");
        signAndSubmit.getStyle().set("border", "3px solid #304391");
        signAndSubmit.getStyle().set("margin-top", "50px");



        FlexBoxLayout content = new FlexBoxLayout(auth1, auth2, auth3, participant, participantLbl, signAndSubmit);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;

    }
}
