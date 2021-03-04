package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

//import com.vaadin.ui.VerticalLayout;
//import eu.maxschuster.vaadin.signaturefield.SignatureField;


@PageTitle("Living Will")
@Route(value = "livingwillview", layout = MainLayout.class)
public class LivingWill extends ViewFrame {
    private Dialog dialog;
    private PdfBrowserViewer viewer;
    @Autowired
    private FHIRConsent fhirConsentClient;
    private PDFDocumentHandler pdfHandler = new PDFDocumentHandler();


    public LivingWill() {
        setId("livingwillview");
        dialog = createDialog();
    }

    @PostConstruct
    public void setup(){
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        StreamResource streamResource = pdfHandler.retrievePDFForm("LivingWill");
        viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("95%");
        viewer.setWidth("840px");

        Component buttonPanel = createSignaturePanel();

        FlexBoxLayout content = new FlexBoxLayout(viewer, buttonPanel);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private Component createSignaturePanel() {
        Button signature = new Button("Sign & Submit Form");
        signature.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.PENCIL));
        signature.addClickListener(clickEvent ->{
            saveLivingWill();
        });

        Button witnessSignature = new Button("Witness Signature");
        witnessSignature.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.PENCIL));

        Button uploadExisting = new Button("Upload a Living Will");
        uploadExisting.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD));

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth("840px");
        layout.setHeight("5%");
        layout.setMargin(true);
        layout.setPadding(false);

        layout.add(signature, uploadExisting);

        return layout;
    }

    private Dialog createDialog() {
        Dialog dialog = new Dialog();


        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        /**
        VerticalLayout form = new VerticalLayout();
        SignatureField signatureField = new SignatureField();
        signatureField.setWidth("350px");
        signatureField.setHeight("150px");

        form.addComponent(signatureField);
        */




        Span message = new Span();

        Button confirmButton = new Button("Confirm", event -> {
            message.setText("Confirmed!");
            dialog.close();
        });
        Button cancelButton = new Button("Cancel", event -> {
            message.setText("Cancelled...");
            dialog.close();
        });


        dialog.add(confirmButton, cancelButton);
        return dialog;
    }


    private void saveLivingWill() {
        ConsentSession consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        Patient patient = consentSession.getFhirPatient();
        Consent livingWillDirective = new Consent();
        livingWillDirective.setId("LivingWill-"+patient.getId());
        livingWillDirective.setStatus(Consent.ConsentState.ACTIVE);
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("adr");
        cConcept.addCoding(coding);
        livingWillDirective.setScope(cConcept);
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        livingWillDirective.setCategory(cList);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+patient.getId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        livingWillDirective.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        orgRef.setReference("Organization/privacy-consent-scenario-H-healthcurrent");
        orgRef.setDisplay("HealthCurrent");
        refList.add(orgRef);
        livingWillDirective.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        attachment.setCreation(new Date());
        attachment.setTitle("Living Will");


        //PDF Stream reader needed here for test only below
        String fullFormPath =  "/advanced_directives/Arizona/LivingWill/English/LivingWill.pdf";

        ByteArrayInputStream bais = null;
        byte[] bArray = null;
        try {
            bais = new ByteArrayInputStream(IOUtils.toByteArray(getClass().getResourceAsStream(fullFormPath)));
            bArray = IOUtils.toByteArray(bais);//.readAllBytes();
        }
        catch (Exception ex) {
            //blah blah
        }
        String encodedString = Base64.getEncoder().encodeToString(bArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        livingWillDirective.setSource(attachment);

        fhirConsentClient.createConsent(livingWillDirective);
    }
}
