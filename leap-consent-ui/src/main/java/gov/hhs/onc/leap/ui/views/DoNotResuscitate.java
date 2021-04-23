package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import elemental.json.Json;
import gov.hhs.onc.leap.adr.model.QuestionnaireError;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRQuestionnaireResponse;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.signature.PDFSigningService;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.navigation.BasicDivider;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Right;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.IconSize;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import gov.hhs.onc.leap.ui.util.pdf.PDFDNRHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


@PageTitle("Do Not Resuscitate (DNR)")
@Route(value = "dnrview", layout = MainLayout.class)
public class DoNotResuscitate extends ViewFrame {
    private static final Logger log = LoggerFactory.getLogger(DoNotResuscitate.class);
    private Button returnButton;
    private Button forwardButton;
    private Button viewStateForm;
    private int questionPosition = 0;
    private String patientFullName;
    private byte[] base64PatientSignature;
    private FlexBoxLayout patientSignatureLayout;
    private TextField patientFullNameField;

    private ConsentSession consentSession;
    private SignaturePad patientSignature;
    private Date patientSignatureDate;

    private FlexBoxLayout healthcarePowerOfAttorney;
    private TextField healthcarePowerOfAttorneyName;
    private SignaturePad healthcarePOASignature;
    private byte[] base64HealthcarePOASignature;
    private Date healthcarePOASignatureDate;

    private FlexBoxLayout physicalCharacteristics;
    private TextField dateOfBirthField;
    private TextField genderField;
    private TextField raceField;
    private TextField eyecolorField;
    private TextField haircolorField;
    private MemoryBuffer uploadBuffer;
    private Upload upload;
    private Image patientImage;
    private byte[] patientImageBytes;
    private HorizontalLayout hImageLayout;

    private FlexBoxLayout physicianOrHospice;
    private TextField physicianNameField;
    private TextField physicianPhoneField;
    private TextField hospiceField;

    private FlexBoxLayout attestation;
    private SignaturePad attestationSignature;
    private Date attestationDate;
    private byte[] base64AttestationSignature;

    private FlexBoxLayout witness;
    private SignaturePad witnessSignature;
    private Date witnessDate;
    private byte[] base64WitnessSignature;

    private byte[] consentPDFAsByteArray;

    private Dialog docDialog;

    private ConsentUser consentUser;

    private QuestionnaireResponse questionnaireResponse;

    private List<QuestionnaireResponse.QuestionnaireResponseItemComponent> responseList;

    private Consent.ConsentState consentState;

    private List<QuestionnaireError> errorList;
    private String advDirectiveFlowType = "Default";
    private Dialog errorDialog;

    @Autowired
    private FHIRConsent fhirConsentClient;
    @Autowired
    private PDFSigningService pdfSigningService;
    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;


    public DoNotResuscitate() {
        setId("dnrview");
    }

    @PostConstruct
    public void setup(){
        setId("dnrview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        this.responseList = new ArrayList<>();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html(getTranslation("DoNotResuscitate-intro"));

        createPreHospitalMedicalDirective();
        createHealthcarePowerOfAttorney();
        createPatientPhysicalCharacteristics();
        createPhysicianOrHospiceInfo();
        createAttestation();
        createWitnessSignature();
        createInfoDialog();

        FlexBoxLayout content = new FlexBoxLayout(intro, patientSignatureLayout, healthcarePowerOfAttorney, physicalCharacteristics, physicianOrHospice, attestation, witness);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createPreHospitalMedicalDirective() {
        Html intro2 = new Html(getTranslation("DoNotResuscitate-intro2"));
        patientFullNameField = new TextField(getTranslation("DoNotResuscitate-patient_full_name"));
        patientFullName = consentUser.getFirstName() +" "+consentUser.getMiddleName()+" "+consentUser.getLastName();
        patientFullNameField.setValue(patientFullName);

        patientSignature = new SignaturePad();
        patientSignature.setHeight("100px");
        patientSignature.setWidth("400px");
        patientSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button(getTranslation("DoNotResuscitate-clear_signature"));
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            patientSignature.clear();
        });
        Button savePatientSig = new Button(getTranslation("DoNotResuscitate-accept_signature"));
        savePatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientSig.addClickListener(event -> {
            base64PatientSignature = patientSignature.getImageBase64();
            patientSignatureDate = new Date();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientSig, savePatientSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("DoNotResuscitate-prehospital_medical_care_directive")),intro2, new BasicDivider(), patientFullNameField, patientSignature, sigLayout);
        patientSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        patientSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        patientSignatureLayout.setHeightFull();
        patientSignatureLayout.setBackgroundColor("white");
        patientSignatureLayout.setShadow(Shadow.S);
        patientSignatureLayout.setBorderRadius(BorderRadius.S);
        patientSignatureLayout.getStyle().set("margin-bottom", "10px");
        patientSignatureLayout.getStyle().set("margin-right", "10px");
        patientSignatureLayout.getStyle().set("margin-left", "10px");
        patientSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    }

    private void createHealthcarePowerOfAttorney() {
        Html intro3 = new Html(getTranslation("DoNotResuscitate-intro3"));
        healthcarePowerOfAttorneyName = new TextField(getTranslation("DoNotResuscitate-healthcare_POA_name"));

        healthcarePOASignature = new SignaturePad();
        healthcarePOASignature.setHeight("100px");
        healthcarePOASignature.setWidth("400px");
        healthcarePOASignature.setPenColor("#2874A6");

        Button clearPOASign = new Button(getTranslation("DoNotResuscitate-clear_signature"));
        clearPOASign.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPOASign.addClickListener(event -> {
           healthcarePOASignature.clear();
        });

        Button savePOASig = new Button(getTranslation("DoNotResuscitate-accept_signature"));
        savePOASig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePOASig.addClickListener(event -> {
            base64HealthcarePOASignature = healthcarePOASignature.getImageBase64();
            healthcarePOASignatureDate = new Date();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPOASign, savePOASig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        healthcarePowerOfAttorney = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("DoNotResuscitate-prehospital_medical_care_directive")),intro3, new BasicDivider(), healthcarePowerOfAttorneyName, healthcarePOASignature, sigLayout);
        healthcarePowerOfAttorney.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        healthcarePowerOfAttorney.setBoxSizing(BoxSizing.BORDER_BOX);
        healthcarePowerOfAttorney.setHeightFull();
        healthcarePowerOfAttorney.setBackgroundColor("white");
        healthcarePowerOfAttorney.setShadow(Shadow.S);
        healthcarePowerOfAttorney.setBorderRadius(BorderRadius.S);
        healthcarePowerOfAttorney.getStyle().set("margin-bottom", "10px");
        healthcarePowerOfAttorney.getStyle().set("margin-right", "10px");
        healthcarePowerOfAttorney.getStyle().set("margin-left", "10px");
        healthcarePowerOfAttorney.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        healthcarePowerOfAttorney.setVisible(false);
    }

    private void createPatientPhysicalCharacteristics() {
        Html intro4 =  new Html(getTranslation("DoNotResuscitate-intro4"));
        Html fileTypes = new Html(getTranslation("DoNotResuscitate-file_types"));
        dateOfBirthField = new TextField(getTranslation("DoNotResuscitate-dob"));
        dateOfBirthField.setValue(getDateString(consentUser.getDateOfBirth()));
        genderField = new TextField(getTranslation("DoNotResuscitate-gender"));
        genderField.setValue(consentUser.getGender());
        raceField = new TextField(getTranslation("DoNotResuscitate-race"));
        raceField.setValue(consentUser.getEthnicity());
        eyecolorField = new TextField(getTranslation("DoNotResuscitate-eye_color"));
        eyecolorField.setValue(consentUser.getEyeColor());
        haircolorField = new TextField(getTranslation("DoNotResuscitate-hair_color"));
        haircolorField.setValue(consentUser.getHairColor());

        uploadBuffer = new MemoryBuffer();
        upload = new Upload(uploadBuffer);
        upload.setDropAllowed(true);
        upload.setAcceptedFileTypes("image/jpeg","image/jpg", "image/png", "image/gif");
        Div output = new Div();

        upload.addSucceededListener(event -> {
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(uploadBuffer.getInputStream()));
                patientImageBytes = IOUtils.toByteArray(bais);
                patientImage = UIUtils.createImage(patientImageBytes, "patientimage.png", "patientimage");
                patientImage.setHeight("200px");
                patientImage.setWidth("200px");
                hImageLayout.add(patientImage);
            }
            catch (Exception ex) {
                log.error("Error processing patient image: "+ex.getMessage());
            }
        });

        upload.getElement().addEventListener("file-remove", new DomEventListener() {
            @Override
            public void handleEvent(DomEvent domEvent) {
                hImageLayout.remove(patientImage);
            }
        });


        patientImage = new Image();
        patientImage.setHeight("150px");
        patientImage.setWidth("200px");

        hImageLayout = new HorizontalLayout(upload);
        hImageLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        hImageLayout.setPadding(true);
        hImageLayout.setSpacing(true);

        physicalCharacteristics = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("DoNotResuscitate-prehospital_medical_care_directive")),intro4, new BasicDivider(), dateOfBirthField,
                genderField, raceField, eyecolorField, haircolorField, new BasicDivider(), hImageLayout, fileTypes);
        physicalCharacteristics.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        physicalCharacteristics.setBoxSizing(BoxSizing.BORDER_BOX);
        physicalCharacteristics.setHeightFull();
        physicalCharacteristics.setBackgroundColor("white");
        physicalCharacteristics.setShadow(Shadow.S);
        physicalCharacteristics.setBorderRadius(BorderRadius.S);
        physicalCharacteristics.getStyle().set("margin-bottom", "10px");
        physicalCharacteristics.getStyle().set("margin-right", "10px");
        physicalCharacteristics.getStyle().set("margin-left", "10px");
        physicalCharacteristics.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        physicalCharacteristics.setVisible(false);
    }

    private void createPhysicianOrHospiceInfo() {
        Html intro5 = new Html(getTranslation("DoNotResuscitate-intro5"));

        physicianNameField = new TextField(getTranslation("DoNotResuscitate-physician"));
        physicianPhoneField = new TextField(getTranslation("DoNotResuscitate-phone"));
        hospiceField = new TextField(getTranslation("DoNotResuscitate-hospice"));

        physicianOrHospice = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("DoNotResuscitate-prehospital_medical_care_directive")),intro5, new BasicDivider(), physicianNameField, physicianPhoneField, hospiceField);
        physicianOrHospice.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        physicianOrHospice.setBoxSizing(BoxSizing.BORDER_BOX);
        physicianOrHospice.setHeightFull();
        physicianOrHospice.setBackgroundColor("white");
        physicianOrHospice.setShadow(Shadow.S);
        physicianOrHospice.setBorderRadius(BorderRadius.S);
        physicianOrHospice.getStyle().set("margin-bottom", "10px");
        physicianOrHospice.getStyle().set("margin-right", "10px");
        physicianOrHospice.getStyle().set("margin-left", "10px");
        physicianOrHospice.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        physicianOrHospice.setVisible(false);

    }

    private void createAttestation() {
        Html intro6 = new Html(getTranslation("DoNotResuscitate-intro6"));
        Html intro7 = new Html(getTranslation("DoNotResuscitate-intro7"));

        attestationSignature = new SignaturePad();
        attestationSignature.setHeight("100px");
        attestationSignature.setWidth("400px");
        attestationSignature.setPenColor("#2874A6");

        Button clearAttestationSig = new Button(getTranslation("DoNotResuscitate-clear_signature"));
        clearAttestationSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearAttestationSig.addClickListener(event -> {
           attestationSignature.clear();
        });

        Button saveAttestationSig = new Button(getTranslation("DoNotResuscitate-accept_signature"));
        saveAttestationSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveAttestationSig.addClickListener(event -> {
            base64AttestationSignature = attestationSignature.getImageBase64();
            attestationDate = new Date();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearAttestationSig, saveAttestationSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        attestation = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("DoNotResuscitate-prehospital_medical_care_directive")),intro6, intro7, new BasicDivider(), attestationSignature, sigLayout);
        attestation.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        attestation.setBoxSizing(BoxSizing.BORDER_BOX);
        attestation.setHeightFull();
        attestation.setBackgroundColor("white");
        attestation.setShadow(Shadow.S);
        attestation.setBorderRadius(BorderRadius.S);
        attestation.getStyle().set("margin-bottom", "10px");
        attestation.getStyle().set("margin-right", "10px");
        attestation.getStyle().set("margin-left", "10px");
        attestation.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        attestation.setVisible(false);

    }

    private void createWitnessSignature() {
        Html intro8 = new Html(getTranslation("DoNotResuscitate-intro8"));
        Html intro9 = new Html(getTranslation("DoNotResuscitate-intro9"));
        Html nextSteps = new Html(getTranslation("DoNotResuscitate-next_steps"));

        witnessSignature = new SignaturePad();
        witnessSignature.setHeight("100px");
        witnessSignature.setWidth("400px");
        witnessSignature.setPenColor("#2874A6");

        Button clearWitnessSig = new Button(getTranslation("DoNotResuscitate-clear_signature"));
        clearWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearWitnessSig.addClickListener(event -> {
            witnessSignature.clear();
        });

        Button savewitnessSig = new Button(getTranslation("DoNotResuscitate-accept_signature"));
        savewitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savewitnessSig.addClickListener(event -> {
            base64WitnessSignature = witnessSignature.getImageBase64();
            witnessDate = new Date();
            getHumanReadable();
            docDialog.open();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearWitnessSig, savewitnessSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        witness = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("DoNotResuscitate-prehospital_medical_care_directive")),intro8, intro9, new BasicDivider(), witnessSignature, sigLayout, nextSteps);
        witness.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        witness.setBoxSizing(BoxSizing.BORDER_BOX);
        witness.setHeightFull();
        witness.setBackgroundColor("white");
        witness.setShadow(Shadow.S);
        witness.setBorderRadius(BorderRadius.S);
        witness.getStyle().set("margin-bottom", "10px");
        witness.getStyle().set("margin-right", "10px");
        witness.getStyle().set("margin-left", "10px");
        witness.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        witness.setVisible(false);
    }

    private void evalNavigation() {
        switch(questionPosition) {
            case 0:
                returnButton.setEnabled(false);
                forwardButton.setEnabled(true);
                patientSignatureLayout.setVisible(true);
                healthcarePowerOfAttorney.setVisible(false);
                physicalCharacteristics.setVisible(false);
                physicianOrHospice.setVisible(false);
                attestation.setVisible(false);
                witness.setVisible(false);
                break;
            case 1:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientSignatureLayout.setVisible(false);
                healthcarePowerOfAttorney.setVisible(true);
                physicalCharacteristics.setVisible(false);
                physicianOrHospice.setVisible(false);
                attestation.setVisible(false);
                witness.setVisible(false);
                break;
            case 2:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientSignatureLayout.setVisible(false);
                healthcarePowerOfAttorney.setVisible(false);
                physicalCharacteristics.setVisible(true);
                physicianOrHospice.setVisible(false);
                attestation.setVisible(false);
                witness.setVisible(false);
                break;
            case 3:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientSignatureLayout.setVisible(false);
                healthcarePowerOfAttorney.setVisible(false);
                physicalCharacteristics.setVisible(false);
                physicianOrHospice.setVisible(true);
                attestation.setVisible(false);
                witness.setVisible(false);
                break;
            case 4:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientSignatureLayout.setVisible(false);
                healthcarePowerOfAttorney.setVisible(false);
                physicalCharacteristics.setVisible(false);
                physicianOrHospice.setVisible(false);
                attestation.setVisible(true);
                witness.setVisible(false);
                break;
            case 5:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(false);
                patientSignatureLayout.setVisible(false);
                healthcarePowerOfAttorney.setVisible(false);
                physicalCharacteristics.setVisible(false);
                physicianOrHospice.setVisible(false);
                attestation.setVisible(false);
                witness.setVisible(true);
                break;
        }
    }




    private Component getFooter() {
        returnButton = new Button(getTranslation("DoNotResuscitate-back"), new Icon(VaadinIcon.BACKWARDS));
        returnButton.setEnabled(false);
        returnButton.addClickListener(event -> {
            questionPosition--;
            evalNavigation();
        });
        forwardButton = new Button(getTranslation("DoNotResuscitate-next"), new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
            questionPosition++;
            evalNavigation();
        });
        viewStateForm = new Button(getTranslation("DoNotResuscitate-view_your_State_dnr_instructions"));
        viewStateForm.setIconAfterText(true);
        viewStateForm.addClickListener(event -> {
            Dialog d = createInfoDialog();
            d.open();
        });



        HorizontalLayout footer = new HorizontalLayout(returnButton, forwardButton, viewStateForm);
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

    private Dialog createInfoDialog() {
        PDFDocumentHandler pdfHandler = new PDFDocumentHandler();
        StreamResource streamResource = pdfHandler.retrievePDFForm("DNR");

        Dialog infoDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button(getTranslation(getTranslation("DoNotResuscitate-close")), e -> infoDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        FlexBoxLayout content = new FlexBoxLayout(viewer, closeButton);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        infoDialog.add(content);

        infoDialog.setModal(false);
        infoDialog.setResizable(true);
        infoDialog.setDraggable(true);

        return infoDialog;
    }

    private void getHumanReadable() {
        StreamResource streamResource = setFieldsCreatePDF();
        docDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");


        Button closeButton = new Button(getTranslation("DoNotResuscitate-cancel"), e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button(getTranslation("DoNotResuscitate-accept_and_submit"));
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            docDialog.close();
            consentState = Consent.ConsentState.ACTIVE;
            advDirectiveFlowType = "Default";
            errorCheck();
            if (errorList.size() > 0) {
                createErrorDialog();
                errorDialog.open();
            }
            else {
                createQuestionnaireResponse();
                createFHIRConsent();
                successNotification();
                //todo test for fhir consent create success
                resetFormAndNavigation();
                evalNavigation();
            }
        });

        Button acceptAndPrintButton = new Button(getTranslation("DoNotResuscitate-accept_and_get_notarized"));
        acceptAndPrintButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptAndPrintButton.addClickListener(event -> {
            docDialog.close();
            consentState = Consent.ConsentState.PROPOSED;
            advDirectiveFlowType = "Notary";
            errorCheck();
            if (errorList.size() > 0) {
                createErrorDialog();
                errorDialog.open();
            }
            else {
                createQuestionnaireResponse();
                createFHIRConsent();
                successNotification();
                //todo test for fhir consent create success
                resetFormAndNavigation();
                evalNavigation();
            }
        });

        HorizontalLayout hLayout = new HorizontalLayout(closeButton, acceptButton, acceptAndPrintButton);


        FlexBoxLayout content = new FlexBoxLayout(viewer, hLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        docDialog.add(content);

        docDialog.setModal(false);
        docDialog.setResizable(true);
        docDialog.setDraggable(true);
    }

    private StreamResource setFieldsCreatePDF() {
        String patientName = patientFullNameField.getValue();
        String patientsignatureDate = getDateString(patientSignatureDate);
        String poaHealthcare = healthcarePowerOfAttorneyName.getValue();
        String dateOfBirth = dateOfBirthField.getValue();
        String gender = genderField.getValue();
        String ethnicity = raceField.getValue();
        String eyeColor = eyecolorField.getValue();
        String hairColor = haircolorField.getValue();
        String primaryPhysician = physicianNameField.getValue();
        String primaryPhysicianPhoneNumber = physicianPhoneField.getValue();
        String hospiceProgram = hospiceField.getValue();
        String attestationdate = getDateString(attestationDate);
        String witnesssignaturedate = getDateString(witnessDate);

        byte[] imageBytes = null;
        byte[] bytes = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(uploadBuffer.getInputStream()));
            bytes = IOUtils.toByteArray(bais);
            imageBytes = Base64.getEncoder().encode(bytes);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }


        PDFDNRHandler pdfHandler = new PDFDNRHandler(pdfSigningService);
        StreamResource res = pdfHandler.retrievePDFForm(patientName, base64PatientSignature, patientsignatureDate, poaHealthcare, base64HealthcarePOASignature,
                                                        dateOfBirth, gender, ethnicity, eyeColor, hairColor, patientImageBytes,
                                                        primaryPhysician, primaryPhysicianPhoneNumber, hospiceProgram, base64AttestationSignature,
                                                        attestationdate, base64WitnessSignature, witnesssignaturedate);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return  res;
    }

    private String getDateString(Date dt) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        String date = "";
        try {
            date = simpleDateFormat.format(dt);
        }
        catch (Exception ex) {
            log.warn("Date error: "+ex.getMessage());
        }
        return date;
    }

    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent dnrDirective = new Consent();
        dnrDirective.setId("DNR-"+consentSession.getFhirPatientId());
        dnrDirective.setStatus(consentState);
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("adr");
        cConcept.addCoding(coding);
        dnrDirective.setScope(cConcept);
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        dnrDirective.setCategory(cList);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        dnrDirective.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        //todo - this is the deployment and custodian organization for advanced directives and should be valid in fhir consent repository
        orgRef.setReference(orgReference);
        orgRef.setDisplay(orgDisplay);
        refList.add(orgRef);
        dnrDirective.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        attachment.setCreation(new Date());
        attachment.setTitle("DNR");


        String encodedString = Base64.getEncoder().encodeToString(consentPDFAsByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        dnrDirective.setSource(attachment);

        Consent.provisionComponent provision = new Consent.provisionComponent();
        Period period = new Period();
        LocalDate sDate = LocalDate.now();
        LocalDate eDate = LocalDate.now().plusYears(10);
        Date startDate = Date.from(sDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(eDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        period.setStart(startDate);
        period.setEnd(endDate);
        
        provision.setPeriod(period);
        
        dnrDirective.setProvision(provision);

        Extension extension = createDoNotResuscitateQuestionnaireResponse();
        dnrDirective.getExtension().add(extension);


        fhirConsentClient.createConsent(dnrDirective);
    }

    private Extension createDoNotResuscitateQuestionnaireResponse() {
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.com/leap/adr/dnr");
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/leap-dnr-"+consentSession.getFhirPatientId()));
        return extension;
    }

    private void successNotification() {
        Span content = new Span(getTranslation("DoNotResuscitate-fhir_advanced_directive_ndr_successfully_created"));

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }

    private void createQuestionnaireResponse() {
        BooleanType booleanTypeTrue = new BooleanType(true);
        BooleanType booleanTypeFalse = new BooleanType(false);
        BooleanType answerBoolean = new BooleanType();
        questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId("leap-dnr-"+consentSession.getFhirPatientId());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/"+consentSession.getFhirPatientId());
        questionnaireResponse.setAuthor(refpatient);
        questionnaireResponse.setAuthored(new Date());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        questionnaireResponse.setSubject(refpatient);
        questionnaireResponse.setQuestionnaire("Questionnaire/leap-dnr");


        doNotResuscitateResponse();
        powerOfAttorneySignatureResponse();
        physicianInfoResponse();
        hospiceResponse();
        healthcareProviderAttestationAndSignatureResponse();
        witnessSignatureResponse();

        questionnaireResponse.setItem(responseList);
        fhirQuestionnaireResponse.createQuestionnaireResponse(questionnaireResponse);
    }

    private void doNotResuscitateResponse() {
        //patient signature represents response of true
        boolean patientSignatureBool = false;
        if (base64PatientSignature != null && base64PatientSignature.length > 0) patientSignatureBool = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1 = createItemBooleanType("1.1", getTranslation("DoNotResuscitate-questionnaire_response_item1_1"), patientSignatureBool);
        responseList.add(item1_1);
    }

    private void powerOfAttorneySignatureResponse() {
        //name of power of attorney if patient unable to sign
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemStringType("2.1", getTranslation("DoNotResuscitate-questionnaire_response_item2_1"), healthcarePowerOfAttorneyName.getValue());
        responseList.add(item2_1);

        //signature of power of attorney acquired
        boolean poaSignature = false;
        if (base64HealthcarePOASignature != null && base64HealthcarePOASignature.length > 0) poaSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2", getTranslation("DoNotResuscitate-questionnaire_response_item2_2"), poaSignature);
        responseList.add(item2_2);
    }

    private void physicianInfoResponse() {
        //name of physician
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_1 = createItemStringType("3.1", getTranslation("DoNotResuscitate-questionnaire_response_item3_1"), physicianNameField.getValue());
        responseList.add(item3_1);

        //Physician Phone Number
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2 = createItemStringType("3.2", getTranslation("DoNotResuscitate-questionnaire_response_item3_2"), physicianPhoneField.getValue());
        responseList.add(item3_2);
    }

    private void hospiceResponse() {
        //Hospice Name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3 = createItemStringType("3.3", getTranslation("DoNotResuscitate-questionnaire_response_item3_3"), hospiceField.getValue());
        responseList.add(item3_3);
    }

    private void healthcareProviderAttestationAndSignatureResponse() {
        //attestation based on Healthcare Provider signature
        boolean attestation = false;
        if ( base64AttestationSignature != null &&  base64AttestationSignature.length > 0) attestation = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1 = createItemBooleanType("4.1", getTranslation("DoNotResuscitate-questionnaire_response_item4_1"), attestation);
        responseList.add(item4_1);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2 = createItemBooleanType("4.2", getTranslation("DoNotResuscitate-questionnaire_response_item4_2"), attestation);
        responseList.add(item4_2);
    }

    private void witnessSignatureResponse() {
        //signature of witness or notary
        boolean witness = false;
        if (base64WitnessSignature != null && base64WitnessSignature.length > 0) witness = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_1 = createItemBooleanType("5.1", getTranslation("DoNotResuscitate-questionnaire_response_item5_1"), witness);
        responseList.add(item5_1);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createItemBooleanType(String linkId, String definition, boolean bool) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        item.setLinkId(linkId);
        item.getAnswer().add((new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()).setValue(new BooleanType(bool)));
        item.setDefinition(definition);
        return item;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createItemStringType(String linkId, String definition, String string) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        item.setLinkId(linkId);
        item.getAnswer().add((new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()).setValue(new StringType(string)));
        item.setDefinition(definition);
        return item;
    }

    private void resetFormAndNavigation() {
        patientSignature.clear();
        physicianPhoneField.clear();
        physicianNameField.clear();
        healthcarePOASignature.clear();
        healthcarePowerOfAttorneyName.clear();
        hospiceField.clear();
        attestationSignature.clear();
        witnessSignature.clear();
        upload.getElement().setPropertyJson("files", Json.createArray());
        hImageLayout.remove(patientImage);

        questionPosition = 0;
    }
    private void errorCheck() {
        errorList = new ArrayList<>();
        if (advDirectiveFlowType.equals("Default")) {
            errorCheckSignature();
            errorCheckCommon();
        }
        else if (advDirectiveFlowType.equals("Notary")) {
            errorCheckCommon();
        }
        else {
            errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-critical_error_unable_to_determine_signature_path"), 0));
        }
    }

    private void errorCheckCommon() {
        try {
            if (physicianNameField.getValue() == null || physicianNameField.getValue().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-physician_name_can_not_be_blank"), 3));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-physician_name_can_not_be_blank"), 3));
        }

        try {
            if (physicianPhoneField.getValue() == null || physicianPhoneField.getValue().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-physician_phone_number_can_not_be_blank"), 3));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-physician_phone_number_can_not_be_blank"), 3));
        }

    }

    private void errorCheckSignature() {
        //patient signature
        try {
            if (base64PatientSignature == null || base64PatientSignature.length == 0) {
                if (base64HealthcarePOASignature == null || base64HealthcarePOASignature.length == 0) {
                    errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-patient_signature_or_signature_of_POA_must_be_provided"), 0));
                }
                else {
                    //check for poa name
                    if (healthcarePowerOfAttorneyName.getValue() == null || healthcarePowerOfAttorneyName.getValue().isEmpty()) {
                        errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-health_care_power_of_attoney_name_can_not_be_blank"), 1));
                    }
                }
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-patient_signature_or_signature_of_POA_must_be_provided"), 0));
            log.warn("Patient Signature check: "+ex.getMessage());
        }

        try {
            if (base64AttestationSignature == null || base64AttestationSignature.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-physician_attestation_signature_must_be_provided"), 4));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-physician_attestation_signature_must_be_provided"), 4));
            log.warn("Physician attestation error: "+ ex.getMessage());
        }

        try {
            if (base64WitnessSignature == null || base64WitnessSignature.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-witness_signature_must_be_provided"), 5));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("DoNotResuscitate-witness_signature_must_be_provided"), 5));
            log.warn("Witness signature error: "+ ex.getMessage());
        }

    }

    private void createErrorDialog() {
        Html errorIntro = new Html(getTranslation("DoNotResuscitate-error_intro"));
        Html flowTypeIntro;
        if (advDirectiveFlowType.equals("Default")) {
            flowTypeIntro = new Html(getTranslation("DoNotResuscitate-flow_type_intro1"));
        }
        else {
            flowTypeIntro = new Html(getTranslation("DoNotResuscitate-flow_type_intro2"));
        }

        Button errorBTN = new Button(getTranslation("DoNotResuscitate-correct_errors"));
        errorBTN.setWidthFull();
        errorBTN.addClickListener(event -> {
            questionPosition = errorList.get(0).getQuestionnaireIndex();
            errorDialog.close();
            evalNavigation();
        });

        FlexBoxLayout verticalLayout = new FlexBoxLayout();

        verticalLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        verticalLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        if (advDirectiveFlowType.equals("Default")) {
            verticalLayout.setHeight("350px");
        }
        else {
            verticalLayout.setHeight("275px");
        }
        verticalLayout.setBackgroundColor("white");
        verticalLayout.setShadow(Shadow.S);
        verticalLayout.setBorderRadius(BorderRadius.S);
        verticalLayout.getStyle().set("margin-bottom", "10px");
        verticalLayout.getStyle().set("margin-right", "10px");
        verticalLayout.getStyle().set("margin-left", "10px");
        verticalLayout.getStyle().set("overflow", "auto");
        verticalLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        Iterator iter = errorList.iterator();
        while (iter.hasNext()) {
            QuestionnaireError q = (QuestionnaireError)iter.next();
            verticalLayout.add(new Html("<p style=\"color:#259AC9\">"+q.getErrorMessage()+"</p>"));
        }

        errorDialog = new Dialog();
        errorDialog.setHeight("600px");
        errorDialog.setWidth("600px");
        errorDialog.setModal(true);
        errorDialog.setCloseOnOutsideClick(false);
        errorDialog.setCloseOnEsc(false);
        errorDialog.setResizable(true);
        errorDialog.add(createHeader(VaadinIcon.WARNING, getTranslation("DoNotResuscitate-failed_verification")),errorIntro, flowTypeIntro, verticalLayout, errorBTN);
    }
}
