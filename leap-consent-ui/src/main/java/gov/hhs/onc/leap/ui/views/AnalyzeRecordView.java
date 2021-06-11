package gov.hhs.onc.leap.ui.views;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import elemental.json.Json;
import gov.hhs.onc.leap.ces.sls.client.SLSRequestClient;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Right;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.IconSize;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


@PageTitle("Analyze Your Clinical Record")
@Route(value = "analyzerecordview", layout = MainLayout.class)
public class AnalyzeRecordView extends ViewFrame {

    private Button clearButton;
    private Button processFileButton;
    private Button hieButton;
    private FlexBoxLayout analyzeLayout;
    private MemoryBuffer uploadBuffer;
    private Upload upload;
    private TextField outcomeField;
    private TextArea notesField;

    @Value("${hapi-fhir.url:http://34.94.253.50:8080/hapi-fhir-jpaserver/fhir/}")
    private String baseURL;

    @Value("${sls.url:http://34.94.253.50:9091}")
    private String slsHost;

    private ConsentSession consentSession;

    private String msg;
    private Html optionLabel = new Html("<p><b>-"+getTranslation("analyzeRecordView-or")+"-</b></p>");

    private FhirContext fhirContext = FhirContext.forR4();

    public AnalyzeRecordView() {
        setId("analyzerecordview");
        this.consentSession = consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html(getTranslation("analyzeRecordView-intro"));

        uploadBuffer = new MemoryBuffer();
        upload = new Upload(uploadBuffer);
        upload.setDropAllowed(true);
        Div output = new Div();

        upload.addSucceededListener(event -> {
            processFileButton.setEnabled(true);
            clearButton.setEnabled(true);
        });

        hieButton = new Button(getTranslation("analyzeRecordView-request_record_button_text"), new Icon(VaadinIcon.HOSPITAL));
        hieButton.addClickListener(event -> {
           analyzeInstreamFHIR();
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout(upload, output, optionLabel, hieButton);
        horizontalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        horizontalLayout.setPadding(true);
        horizontalLayout.setSpacing(true);

        outcomeField = new TextField("Privacy Analysis Outcome: ");
        outcomeField.setReadOnly(true);
        notesField = new TextArea("Results Detail");
        notesField.setReadOnly(true);

        analyzeLayout = new FlexBoxLayout(createHeader(VaadinIcon.GLASSES, "Privacy Analysis"), horizontalLayout, outcomeField, notesField);
        analyzeLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        analyzeLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        analyzeLayout.setHeightFull();
        analyzeLayout.setBackgroundColor("white");
        analyzeLayout.setShadow(Shadow.S);
        analyzeLayout.setBorderRadius(BorderRadius.S);
        analyzeLayout.getStyle().set("margin-bottom", "10px");
        analyzeLayout.getStyle().set("margin-right", "10px");
        analyzeLayout.getStyle().set("margin-left", "10px");
        analyzeLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        FlexBoxLayout content = new FlexBoxLayout(intro, analyzeLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;

    }

    private Component getFooter() {
            processFileButton = new Button("Process File", new Icon(VaadinIcon.FILE_PROCESS));
            processFileButton.setEnabled(false);
            processFileButton.addClickListener(event -> {
                analyzeFile();
            });
            clearButton = new Button("Clear", new Icon(VaadinIcon.FILE_REMOVE));
            clearButton.setEnabled(false);
            clearButton.addClickListener(event -> {
                clearForm();
            });

            HorizontalLayout footer = new HorizontalLayout(clearButton, processFileButton);
            footer.setAlignItems(FlexComponent.Alignment.CENTER);
            footer.setPadding(true);
            footer.setSpacing(true);
            return footer;
    }

    private FlexBoxLayout createHeader(VaadinIcon icon, String title) {
        FlexBoxLayout header = new FlexBoxLayout(
                UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, icon),
                UIUtils.createH3Label(title));
        header.getStyle().set("background-color", "#5F9EA0");
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(Right.L);
        return header;
    }

    private void analyzeFile() {
        hieButton.setEnabled(false);
        SLSRequestClient sls = new SLSRequestClient(slsHost);
        //for testing
        String id = UUID.randomUUID().toString();
        String origin = getTranslation("analyzeRecordView-leap_consent_ui");
        String msgSource = "CCDA";
        String msgVersion = "v3";
        try {
            InputStream in = uploadBuffer.getInputStream();
            ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(in));
            msg = IOUtils.toString(bais, StandardCharsets.UTF_8);

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        if (msg.indexOf("MSH") == 0) {
            msgSource = "V2";
            msgVersion = "2.4";
        }
        else if (msg.indexOf("ClinicalDocument") > -1) {
            msgSource = "CCDA";
            msgVersion = "v3";
        }
        else if (msg.indexOf("resourceType") > -1) {
            msgSource = "FHIR";
            msgVersion = "4.0.1";
        }
        else {
            msgSource = "indeterminate";
            msgVersion = "unknown";
        }
        String results = sls.requestLabelingSecured(id, origin, msgSource, msgVersion, msg);
        if (results.contains("NON-RESTRICTED")){
            outcomeField.setValue(getTranslation("analyzeRecordView-normal_msg"));
        }
        else if (results.contains("RESTRICTED")) {
            outcomeField.setValue( getTranslation("analyzeRecordView-restricted_msg"));
        } else {
            outcomeField.setValue(getTranslation("analyzeRecordView-error_msg"));
        }
        notesField.setValue(results);
        clearButton.setEnabled(true);
        processFileButton.setEnabled(false);
    }

    private void clearForm() {
        hieButton.setEnabled(true);
        outcomeField.clear();
        notesField.clear();
        processFileButton.setEnabled(false);
        upload.getElement().setPropertyJson("files", Json.createArray());
    }

    private void analyzeInstreamFHIR() {
        hieButton.setEnabled(false);
        SLSRequestClient sls = new SLSRequestClient(slsHost);
        String id = UUID.randomUUID().toString();
        String orgin = "LEAP Consent UI";
        String msgSource = "FHIR";
        String msgVersion = "4.0.1";
        try {

            String uri = baseURL + "Patient/" + consentSession.getFhirPatientId() + "/$everything";


            IGenericClient client = fhirContext.newRestfulGenericClient(baseURL);

            Bundle bundle = client.search().byUrl(uri).returnBundle(Bundle.class).execute();

            IParser parser = fhirContext.newJsonParser();

            String msg = parser.encodeResourceToString(bundle);


            String results = sls.requestLabelingSecured(id, orgin, msgSource, msgVersion, msg);
            if (results.contains("NON-RESTRICTED")){
                outcomeField.setValue(getTranslation("analyzeRecordView-normal_msg"));
            }
            else if (results.contains("RESTRICTED")) {
                outcomeField.setValue(getTranslation("analyzeRecordView-restricted_msg"));
            }
            else {
                outcomeField.setValue(getTranslation("analyzeRecordView-error_msg"));
            }
            notesField.setValue(results);
            clearButton.setEnabled(true);
            processFileButton.setEnabled(false);
        }
        catch (Exception ex) {
            outcomeField.setValue("ERROR");
            notesField.setValue("Failed processing Instream FHIR bundle: "+ex.getMessage());
            clearButton.setEnabled(true);
            processFileButton.setEnabled(false);
            ex.printStackTrace();
        }
    }
}
