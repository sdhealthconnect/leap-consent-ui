package gov.hhs.onc.leap.ui.views;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.adr.model.QuestionnaireError;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.adr.model.POLSTPortableMedicalOrder;
import gov.hhs.onc.leap.adr.model.PowerOfAttorneyHealthCare;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRQuestionnaireResponse;
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
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFPOAHealthcareHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFPOLSTHandler;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@PageTitle("National POLST Form - A Portable Medical Order")
@Route(value = "portablemedicalorderview", layout = MainLayout.class)
public class PortableMedicalOrder extends ViewFrame {
    private static final Logger log = LoggerFactory.getLogger(PortableMedicalOrder.class);

    private ConsentSession consentSession;
    private ConsentUser consentUser;

    private Button returnButton;
    private Button forwardButton;
    private Button viewStateForm;
    private int questionPosition = 0;

    private TextField patientFirstName;
    private TextField patientMiddleName;
    private TextField patientLastName;
    private TextField patientPreferredName;
    private TextField patientNameSuffix;
    private TextField patientDobYear;
    private TextField patientDobMonth;
    private TextField patientDobDay;
    private HorizontalLayout patientDobLayout;
    private TextField patientState;

    private Checkbox patientGenderM;
    private Checkbox patientGenderF;
    private Checkbox patientGenderX;

    private TextField last4ssn1;
    private TextField last4ssn2;
    private TextField last4ssn3;
    private TextField last4ssn4;
    private HorizontalLayout ssnLayout;
    private FlexBoxLayout patientGeneralInfoLayout;

    private Checkbox yesCPR;
    private Checkbox noCPR;
    private FlexBoxLayout cardiopulmonaryResuscitationOrdersLayout;

    private Checkbox fullTreatments;
    private Checkbox selectiveTreatments;
    private Checkbox comfortTreatments;
    private FlexBoxLayout initialTreatmentOrders;

    private TextArea additionalOrdersInstructions;
    private FlexBoxLayout additionalOrdersLayout;

    private Checkbox provideFeeding;
    private Checkbox trialPeriodFeeding;
    private Checkbox noArtificialMeans;
    private Checkbox discussedNoDecision;
    private FlexBoxLayout medicallyAssistedNutrition;

    private SignaturePad patientOrRepresentativeSignature;
    private byte[] base64PatientOrRepresentativeSignature;
    private Date patientOrRepresentativeSignatureDate;
    private TextField patientOrRepresentativeNameField;
    private Checkbox notPatientSigning;
    private TextField authorityField;
    private FlexBoxLayout patientOrRepresentativeSignatureLayout;

    private FlexBoxLayout notPatientSigningReqLayout;

    private SignaturePad healthcareProviderSignature;
    private byte[] base64HealthcareProviderSignature;
    private Date healthcareProviderSignatureDate;
    private TextField healthcareProviderNameField;
    private TextField healthcareProviderPhoneNumberField;
    private TextField healthcareProviderLicenseField;
    private FlexBoxLayout healthcareProviderSignatureLayout;
    // if required by state or institution
    private SignaturePad supervisingPhysicianSignature;
    private byte[] base64SupervisingPhysicianSignature;
    private TextField supervisingPhysicianLicenseField;
    private Checkbox supervisorSignatureChk;
    private FlexBoxLayout supervisingPhysicianLayout;


    //optional items - Contact Information
    private TextField emergencyContactNameField;
    private Checkbox legalRepresentative;
    private Checkbox otherContactType;
    private TextField dayPhoneNumber;
    private TextField nightPhoneNumber;
    private FlexBoxLayout emergencyContactLayout;

    private TextField primaryProviderName;
    private TextField primaryProviderPhoneNumber;
    private FlexBoxLayout primaryProviderLayout;

    private Checkbox inHospiceCare;
    private TextField hospiceName;
    private TextField hospicePhoneNumber;
    private FlexBoxLayout hospiceLayout;

    private Checkbox livingWillReviewed;
    private TextField reviewedDate;
    private Checkbox livingWillConflict;
    private Checkbox advanceDirectiveNotAvailable;
    private Checkbox noAdvanceDirective;
    private FlexBoxLayout advanceDirectiveReviewLayout;

    private Checkbox patientParticipated;
    private Checkbox legalOrSurrogate;
    private Checkbox courtAppointedGuardian;
    private Checkbox parentOfMinor;
    private Checkbox otherParticipant;
    private TextField otherParticipantList;
    private FlexBoxLayout participantLayout;

    private TextField whoAssistedInFormCompletionName;
    private TextField dateAssisted;
    private TextField whoAssistedPhoneNumber;
    private Checkbox socialWorkerAssist;
    private Checkbox nurseAssisted;
    private Checkbox clergyAssisted;
    private Checkbox otherAssisted;
    private TextField otherAssistedList;
    private FlexBoxLayout whoAssistedLayout;

    private byte[] consentPDFAsByteArray;

    private Dialog docDialog;

    private QuestionnaireResponse questionnaireResponse;

    private List<QuestionnaireResponse.QuestionnaireResponseItemComponent> responseList;

    private POLSTPortableMedicalOrder polst;

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

    @PostConstruct
    public void setup() {
        setId("portablemedicalorderview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        this.responseList = new ArrayList<>();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html("<p>Health care providers, the patient, or patient representative, should complete this form only after the "+
                "health care provider has had a conversation with the patient, or the patient’s representative.  "+
                "The POLST decision-making process is for patients who are at risk for a life-threatening clinical event because they have a serious life-limiting medical "+
                "condition, which may include advanced frailty <a href=\"http://www.polst.org//guidance-appropriate-patients-pdf\">(www.polst.org/guidance-appropriate-patients-pdf)</a>.</p>" );

        createPatientGeneralInfo();
        createCardiopulmonaryResuscitationOrders();
        createInitialTreatmentOrders();
        createAdditionalOrders();
        createMedicallyAssistedNutrition();
        createPatientOrRepresentativeSignature();
        createHealthcareProviderSignature();
        createSupervisingPhysician();
        createEmergencyContact();
        createPrimaryProvider();
        createHospice();
        createAdvancedDirectiveReview();
        createParticipants();
        createWhoAssisted();
        createInfoDialog();


        FlexBoxLayout content = new FlexBoxLayout(intro, patientGeneralInfoLayout, cardiopulmonaryResuscitationOrdersLayout, initialTreatmentOrders, additionalOrdersLayout,
                medicallyAssistedNutrition, patientOrRepresentativeSignatureLayout, healthcareProviderSignatureLayout, supervisingPhysicianLayout, emergencyContactLayout,
                primaryProviderLayout, hospiceLayout, advanceDirectiveReviewLayout, participantLayout, whoAssistedLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createPatientGeneralInfo() {
        Html intro2 = new Html("<p><b>This is a medical order, not an advance directive. For information about POLST and to understand this document, visit:</b> <a href=\"http://www.polst.org/form\">www.polst.org/form</a></p>");

        patientFirstName = new TextField("Patient's First Name");
        patientPreferredName = new TextField("Preferred Name");
        patientMiddleName = new TextField("Middle Name/Initials");
        patientLastName = new TextField("Last Name");
        patientNameSuffix = new TextField("Suffix(Sr,Jr,etc)");

        Label dobLabel = new Label("DOB (mm/dd/yyyy");
        Label dobSeparator1 = new Label("/");
        Label dobSeparator = new Label("/");
        patientDobYear = new TextField("");
        patientDobYear.setWidth("100px");
        patientDobMonth = new TextField("");
        patientDobMonth.setWidth("50px");
        patientDobDay = new TextField("");
        patientDobDay.setWidth("50px");
        Label stateOfCompletion = new Label("State where form was completed: "+consentSession.getPrimaryState());
        patientDobLayout = new HorizontalLayout();
        patientDobLayout.add(dobLabel, patientDobMonth, dobSeparator1, patientDobDay, dobSeparator, patientDobYear, stateOfCompletion);
        patientDobLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        patientGenderM = new Checkbox();
        patientGenderM.setLabel("M");
        patientGenderM.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               patientGenderF.clear();
               patientGenderX.clear();
           }
        });
        patientGenderF = new Checkbox();
        patientGenderF.setLabel("F");
        patientGenderF.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                patientGenderM.clear();
                patientGenderX.clear();
            }
        });
        patientGenderX = new Checkbox();
        patientGenderX.setLabel("X");
        patientGenderX.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                patientGenderF.clear();
                patientGenderM.clear();
            }
        });
        Label genderLabel = new Label("Gender");


        Label ssnLabel = new Label("Social Security Number’s last 4 digits (optional): xxx-xx-");
        last4ssn1 = new TextField();
        last4ssn1.setWidth("50px");
        last4ssn2 = new TextField();
        last4ssn2.setWidth("50px");
        last4ssn3 = new TextField();
        last4ssn3.setWidth("50px");
        last4ssn4 = new TextField();
        last4ssn4.setWidth("50px");

        ssnLayout = new HorizontalLayout();
        ssnLayout.add(genderLabel, patientGenderM, patientGenderF, patientGenderX, ssnLabel, last4ssn1, last4ssn2, last4ssn3, last4ssn4);
        ssnLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        //set values
        patientFirstName.setValue(consentUser.getFirstName());
        patientMiddleName.setValue(consentUser.getMiddleName());
        patientLastName.setValue(consentUser.getLastName());
        patientDobMonth.setValue(getDateMonth(consentUser.getDateOfBirth()));
        patientDobDay.setValue(getDateDay(consentUser.getDateOfBirth()));
        patientDobYear.setValue(getDateYear(consentUser.getDateOfBirth()));
        log.warn("ConsentUser Gender" +consentUser.getGender());
        if (consentUser.getGender().equals("Male")) {
            patientGenderM.setValue(true);
        }
        else if (consentUser.getGender().equals("Female")) {
            patientGenderF.setValue(true);
        }
        else {
            patientGenderX.setValue(true);
        }

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Patient Information"),intro2, new BasicDivider(),
                patientFirstName, patientPreferredName, patientMiddleName, patientLastName, patientDobLayout, ssnLayout);
        patientGeneralInfoLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        patientGeneralInfoLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        patientGeneralInfoLayout.setHeightFull();
        patientGeneralInfoLayout.setBackgroundColor("white");
        patientGeneralInfoLayout.setShadow(Shadow.S);
        patientGeneralInfoLayout.setBorderRadius(BorderRadius.S);
        patientGeneralInfoLayout.getStyle().set("margin-bottom", "10px");
        patientGeneralInfoLayout.getStyle().set("margin-right", "10px");
        patientGeneralInfoLayout.getStyle().set("margin-left", "10px");
        patientGeneralInfoLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        patientGeneralInfoLayout.setVisible(true);
    }

    private void createCardiopulmonaryResuscitationOrders() {
        Html intro3 = new Html("<p><b>Follow these orders if patient has no pulse and is not breathing.</b></p>");

        yesCPR = new Checkbox();
        yesCPR.setLabelAsHtml("<p><b>YES CPR: Attempt Resuscitation, including mechanical ventilation, defibrillation and cardioversion.</b> (Requires choosing Full Treatments in Section B)</p>");
        yesCPR.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) noCPR.clear();
        });
        noCPR = new Checkbox();
        noCPR.setLabelAsHtml("<p><b>NO CPR: Do Not Attempt Resuscitation.</b> (May choose any option in Section B)</p>");
        noCPR.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) yesCPR.clear();
        });

        cardiopulmonaryResuscitationOrdersLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "A. Cardiopulmonary Resuscitation Orders"),intro3, new BasicDivider(),
                yesCPR, noCPR);
        cardiopulmonaryResuscitationOrdersLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        cardiopulmonaryResuscitationOrdersLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        cardiopulmonaryResuscitationOrdersLayout.setHeightFull();
        cardiopulmonaryResuscitationOrdersLayout.setBackgroundColor("white");
        cardiopulmonaryResuscitationOrdersLayout.setShadow(Shadow.S);
        cardiopulmonaryResuscitationOrdersLayout.setBorderRadius(BorderRadius.S);
        cardiopulmonaryResuscitationOrdersLayout.getStyle().set("margin-bottom", "10px");
        cardiopulmonaryResuscitationOrdersLayout.getStyle().set("margin-right", "10px");
        cardiopulmonaryResuscitationOrdersLayout.getStyle().set("margin-left", "10px");
        cardiopulmonaryResuscitationOrdersLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
    }

    private void createInitialTreatmentOrders() {
        Html intro4 = new Html("<p><b>Follow these orders if patient has a pulse and/or is breathing.</b</p>");

        fullTreatments = new Checkbox();
        fullTreatments.setLabelAsHtml("<p><b>Full Treatments (required if choose CPR in Section A).</b> Goal: Attempt to sustain life by all "+
                        "medically effective means. Provide appropriate medical and surgical treatments as indicated to attempt to prolong life, "+
                        "including intensive care.</p>");
        fullTreatments.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               selectiveTreatments.clear();
               comfortTreatments.clear();
           }
        });
        selectiveTreatments = new Checkbox();
        selectiveTreatments.setLabelAsHtml("<p><b>Selective Treatments.</b> Goal: Attempt to restore function while avoiding intensive care and "+
                "resuscitation efforts (ventilator, defibrillation and cardioversion). May use non-invasive positive airway pressure, antibiotics "+
                "and IV fluids as indicated. Avoid intensive care. Transfer to hospital if treatment needs cannot be met in current location.</p>");
        selectiveTreatments.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               fullTreatments.clear();
               comfortTreatments.clear();
           }
        });
        comfortTreatments = new Checkbox();
        comfortTreatments.setLabelAsHtml("<p><b>Comfort-focused Treatments.</b> Goal: Maximize comfort through symptom management; allow natural death. Use oxygen, "+
                "suction and manual treatment of airway obstruction as needed for comfort. Avoid treatments listed in full or select treatments unless consistent with "+
                "comfort goal. Transfer to hospital <b>only</b> if comfort cannot be achieved in current setting. </p>");
        comfortTreatments.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               fullTreatments.clear();
               selectiveTreatments.clear();
           }
        });

        initialTreatmentOrders = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "B. Initial Treatment Orders"),intro4, new BasicDivider(),
                fullTreatments, selectiveTreatments, comfortTreatments);
        initialTreatmentOrders.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        initialTreatmentOrders.setBoxSizing(BoxSizing.BORDER_BOX);
        initialTreatmentOrders.setHeightFull();
        initialTreatmentOrders.setBackgroundColor("white");
        initialTreatmentOrders.setShadow(Shadow.S);
        initialTreatmentOrders.setBorderRadius(BorderRadius.S);
        initialTreatmentOrders.getStyle().set("margin-bottom", "10px");
        initialTreatmentOrders.getStyle().set("margin-right", "10px");
        initialTreatmentOrders.getStyle().set("margin-left", "10px");
        initialTreatmentOrders.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        initialTreatmentOrders.setVisible(false);

    }

    private void createAdditionalOrders() {
        Html intro5 = new Html("<p>These orders are in addition to those in section B (e.g., blood products, dialysis).  [EMS protocols may "+
                "limit emergency responder ability to act on orders in this section.]</p>");

        additionalOrdersInstructions = new TextArea();

        additionalOrdersLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "C. Additional Orders or Instructions"),intro5, new BasicDivider(),
                additionalOrdersInstructions);
        additionalOrdersLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        additionalOrdersLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        additionalOrdersLayout.setHeightFull();
        additionalOrdersLayout.setBackgroundColor("white");
        additionalOrdersLayout.setShadow(Shadow.S);
        additionalOrdersLayout.setBorderRadius(BorderRadius.S);
        additionalOrdersLayout.getStyle().set("margin-bottom", "10px");
        additionalOrdersLayout.getStyle().set("margin-right", "10px");
        additionalOrdersLayout.getStyle().set("margin-left", "10px");
        additionalOrdersLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        additionalOrdersLayout.setVisible(false);
    }

    private void createMedicallyAssistedNutrition() {
        Html intro6 = new Html("<p>(Offer food by mouth if desired by patient, safe and tolerated)</p>");

        provideFeeding = new Checkbox();
        provideFeeding.setLabelAsHtml("<p>Provide feeding through new or existing surgically-placed tubes</p>");
        provideFeeding.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               trialPeriodFeeding.clear();
               noArtificialMeans.clear();
               discussedNoDecision.clear();
           }
        });
        trialPeriodFeeding = new Checkbox();
        trialPeriodFeeding.setLabelAsHtml("<p>Trial period for artificial nutrition but no surgically-placed tubes</p>");
        trialPeriodFeeding.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                provideFeeding.clear();
                noArtificialMeans.clear();
                discussedNoDecision.clear();
            }
        });
        noArtificialMeans = new Checkbox();
        noArtificialMeans.setLabelAsHtml("<p>No artificial means of nutrition desired</p>");
        noArtificialMeans.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                provideFeeding.clear();
                trialPeriodFeeding.clear();
                discussedNoDecision.clear();
            }
        });
        discussedNoDecision = new Checkbox();
        discussedNoDecision.setLabelAsHtml("<p>Discussed but no decision made (standard of care provided)</p>");
        discussedNoDecision.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                provideFeeding.clear();
                trialPeriodFeeding.clear();
                noArtificialMeans.clear();
            }
        });

        medicallyAssistedNutrition = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "D. Medically Assisted Nutrition"),intro6, new BasicDivider(),
                provideFeeding, trialPeriodFeeding, noArtificialMeans, discussedNoDecision);
        medicallyAssistedNutrition.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        medicallyAssistedNutrition.setBoxSizing(BoxSizing.BORDER_BOX);
        medicallyAssistedNutrition.setHeightFull();
        medicallyAssistedNutrition.setBackgroundColor("white");
        medicallyAssistedNutrition.setShadow(Shadow.S);
        medicallyAssistedNutrition.setBorderRadius(BorderRadius.S);
        medicallyAssistedNutrition.getStyle().set("margin-bottom", "10px");
        medicallyAssistedNutrition.getStyle().set("margin-right", "10px");
        medicallyAssistedNutrition.getStyle().set("margin-left", "10px");
        medicallyAssistedNutrition.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        medicallyAssistedNutrition.setVisible(false);
    }

    private void createPatientOrRepresentativeSignature() {
        Html intro7 = new Html("<p>I understand this form is voluntary. I have discussed my treatment options and goals of care with my provider. If signing as the\n" +
                "patient’s representative, the treatments are consistent with the patient’s known wishes and in their best interest.</p");
        Html intro8 = new Html("<p><b>The most recently completed valid POLST form supersedes all " +
                "previously completed POLST forms.</b></p>");

        patientOrRepresentativeSignature = new SignaturePad();
        patientOrRepresentativeSignature.setHeight("100px");
        patientOrRepresentativeSignature.setWidth("400px");
        patientOrRepresentativeSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button("Clear Signature");
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            patientOrRepresentativeSignature.clear();
        });
        Button savePatientSig = new Button("Accept Signature");
        savePatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientSig.addClickListener(event -> {
            base64PatientOrRepresentativeSignature = patientOrRepresentativeSignature.getImageBase64();
            patientOrRepresentativeSignatureDate = new Date();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientSig, savePatientSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        notPatientSigning = new Checkbox();
        notPatientSigning.setLabel("If other than patient, enter full name, and provide authority if any.");
        notPatientSigning.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               notPatientSigningReqLayout.setVisible(true);
           }
           else {
               notPatientSigningReqLayout.setVisible(false);
           }
        });

        patientOrRepresentativeNameField = new TextField("Full Name");
        authorityField = new TextField("Authority");

        notPatientSigningReqLayout = new FlexBoxLayout(patientOrRepresentativeNameField, authorityField);
        notPatientSigningReqLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        notPatientSigningReqLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        //notPatientSigningReqLayout.setHeightFull();
        notPatientSigningReqLayout.setBackgroundColor("white");
        notPatientSigningReqLayout.setShadow(Shadow.S);
        notPatientSigningReqLayout.setBorderRadius(BorderRadius.S);
        notPatientSigningReqLayout.getStyle().set("margin-bottom", "10px");
        notPatientSigningReqLayout.getStyle().set("margin-right", "10px");
        notPatientSigningReqLayout.getStyle().set("margin-left", "10px");
        notPatientSigningReqLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        notPatientSigningReqLayout.setVisible(false);

        patientOrRepresentativeSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "E. Signature: Patient or Patient Representative"),intro7, intro8, new BasicDivider(),
                patientOrRepresentativeSignature, notPatientSigning, notPatientSigningReqLayout, sigLayout);
        patientOrRepresentativeSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        patientOrRepresentativeSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        patientOrRepresentativeSignatureLayout.setHeightFull();
        patientOrRepresentativeSignatureLayout.setBackgroundColor("white");
        patientOrRepresentativeSignatureLayout.setShadow(Shadow.S);
        patientOrRepresentativeSignatureLayout.setBorderRadius(BorderRadius.S);
        patientOrRepresentativeSignatureLayout.getStyle().set("margin-bottom", "10px");
        patientOrRepresentativeSignatureLayout.getStyle().set("margin-right", "10px");
        patientOrRepresentativeSignatureLayout.getStyle().set("margin-left", "10px");
        patientOrRepresentativeSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        patientOrRepresentativeSignatureLayout.setVisible(false);

    }

    private void createHealthcareProviderSignature() {
        Html intro9 = new Html("<p>I have discussed this order with the patient or his/her representative. The orders reflect the patient’s known wishes, to the best of my knowledge.</p>");
        Html intro10 = new Html("<p><b>[Note:</b> Only licensed health care providers authorized by law to sign POLST form in state where completed may sign this order<b>]</b></p>");

        healthcareProviderSignature = new SignaturePad();
        healthcareProviderSignature.setHeight("100px");
        healthcareProviderSignature.setWidth("400px");
        healthcareProviderSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button("Clear Signature");
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            healthcareProviderSignature.clear();
        });
        Button savePatientSig = new Button("Accept Signature");
        savePatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientSig.addClickListener(event -> {
            base64HealthcareProviderSignature = healthcareProviderSignature.getImageBase64();
            healthcareProviderSignatureDate = new Date();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientSig, savePatientSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        healthcareProviderPhoneNumberField = new TextField();
        healthcareProviderPhoneNumberField.setLabel("Phone Number:");
        healthcareProviderPhoneNumberField.setPlaceholder("(###) ###-####");

        healthcareProviderNameField = new TextField();
        healthcareProviderNameField.setLabel("Full Name:");
        healthcareProviderLicenseField = new TextField();
        healthcareProviderLicenseField.setLabel("License/Cert. #:");

        healthcareProviderSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "F. Signature: Health Care Provider - Verbal orders are acceptable with follow up signature."),
                intro9, intro10, new BasicDivider(),
                healthcareProviderSignature, healthcareProviderPhoneNumberField, healthcareProviderNameField, healthcareProviderLicenseField, sigLayout);
        healthcareProviderSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        healthcareProviderSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        healthcareProviderSignatureLayout.setHeightFull();
        healthcareProviderSignatureLayout.setBackgroundColor("white");
        healthcareProviderSignatureLayout.setShadow(Shadow.S);
        healthcareProviderSignatureLayout.setBorderRadius(BorderRadius.S);
        healthcareProviderSignatureLayout.getStyle().set("margin-bottom", "10px");
        healthcareProviderSignatureLayout.getStyle().set("margin-right", "10px");
        healthcareProviderSignatureLayout.getStyle().set("margin-left", "10px");
        healthcareProviderSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        healthcareProviderSignatureLayout.setVisible(false);

    }

    private void createSupervisingPhysician() {
        Html intro11 = new Html("<p>A supervising physicians signature may be required.</p>");

        supervisingPhysicianSignature = new SignaturePad();
        supervisingPhysicianSignature.setHeight("100px");
        supervisingPhysicianSignature.setWidth("400px");
        supervisingPhysicianSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button("Clear Signature");
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            supervisingPhysicianSignature.clear();
            supervisingPhysicianLicenseField.clear();
        });
        Button savePatientSig = new Button("Accept Signature");
        savePatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientSig.addClickListener(event -> {
            base64SupervisingPhysicianSignature = supervisingPhysicianSignature.getImageBase64();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientSig, savePatientSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        supervisingPhysicianLicenseField = new TextField("License #:");

        supervisorSignatureChk = new Checkbox();
        supervisorSignatureChk.setLabel("Not Applicable");
        supervisorSignatureChk.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               clearPatientSig.setEnabled(false);
               savePatientSig.setEnabled(false);
               supervisingPhysicianSignature.clear();
               supervisingPhysicianLicenseField.clear();
               supervisingPhysicianSignature.setReadOnly(true);
               supervisingPhysicianLicenseField.setEnabled(false);
           }
           else {
               clearPatientSig.setEnabled(true);
               savePatientSig.setEnabled(true);
               supervisingPhysicianSignature.clear();
               supervisingPhysicianLicenseField.clear();
               supervisingPhysicianSignature.setReadOnly(false);
               supervisingPhysicianLicenseField.setEnabled(true);
           }
        });
        supervisingPhysicianLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Supervising Physician Signature"),
                intro11, new BasicDivider(),
                supervisorSignatureChk, supervisingPhysicianSignature, supervisingPhysicianLicenseField, sigLayout);
        supervisingPhysicianLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        supervisingPhysicianLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        supervisingPhysicianLayout.setHeightFull();
        supervisingPhysicianLayout.setBackgroundColor("white");
        supervisingPhysicianLayout.setShadow(Shadow.S);
        supervisingPhysicianLayout.setBorderRadius(BorderRadius.S);
        supervisingPhysicianLayout.getStyle().set("margin-bottom", "10px");
        supervisingPhysicianLayout.getStyle().set("margin-right", "10px");
        supervisingPhysicianLayout.getStyle().set("margin-left", "10px");
        supervisingPhysicianLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        supervisingPhysicianLayout.setVisible(false);
    }

    private void createEmergencyContact() {
        Html intro12 = new Html("<p>Patient’s Emergency Contact. (Note: Listing a person here does <b>NOT</b> grant them authority to be a legal representative. Only an " +
                "advance directive or state law can grant that authority.)</p>");

        emergencyContactNameField = new TextField("Full Name:");
        legalRepresentative = new Checkbox("Legal Representative");
        otherContactType = new Checkbox("Other Contact Type");
        dayPhoneNumber = new TextField("Day Phone #:");
        dayPhoneNumber.setPlaceholder("(###) ###-####");
        nightPhoneNumber = new TextField("Night Phone #:");
        nightPhoneNumber.setPlaceholder("(###) ###-####");
        legalRepresentative.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                otherContactType.setValue(false);
            }
        });
        otherContactType.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                legalRepresentative.setValue(false);
            }
        });

        emergencyContactLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Contact Information - Emergency Contact (Optional)"),
                intro12, new BasicDivider(),
                emergencyContactNameField, legalRepresentative, otherContactType, dayPhoneNumber, nightPhoneNumber);
        emergencyContactLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        emergencyContactLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        emergencyContactLayout.setHeightFull();
        emergencyContactLayout.setBackgroundColor("white");
        emergencyContactLayout.setShadow(Shadow.S);
        emergencyContactLayout.setBorderRadius(BorderRadius.S);
        emergencyContactLayout.getStyle().set("margin-bottom", "10px");
        emergencyContactLayout.getStyle().set("margin-right", "10px");
        emergencyContactLayout.getStyle().set("margin-left", "10px");
        emergencyContactLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        emergencyContactLayout.setVisible(false);

    }

    private void createPrimaryProvider() {
        Html intro13 = new Html("<p>Patient’s Emergency Contact. (Note: Listing a person here does <b>NOT</b> grant them authority to be a legal representative. Only an " +
                              "advance directive or state law can grant that authority.)</p>");

        primaryProviderName = new TextField("Primary Care Provider Name:");
        primaryProviderPhoneNumber = new TextField("Phone #:");
        primaryProviderPhoneNumber.setPlaceholder("(###) ###-####");

       primaryProviderLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Contact Information - Primary Provider (Optional)"),
                intro13, new BasicDivider(), primaryProviderName, primaryProviderPhoneNumber);
        primaryProviderLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        primaryProviderLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        primaryProviderLayout.setHeightFull();
        primaryProviderLayout.setBackgroundColor("white");
        primaryProviderLayout.setShadow(Shadow.S);
        primaryProviderLayout.setBorderRadius(BorderRadius.S);
        primaryProviderLayout.getStyle().set("margin-bottom", "10px");
        primaryProviderLayout.getStyle().set("margin-right", "10px");
        primaryProviderLayout.getStyle().set("margin-left", "10px");
        primaryProviderLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        primaryProviderLayout.setVisible(false);

    }

    private void createHospice() {
        Html intro14 = new Html("<p>Patient’s Emergency Contact. (Note: Listing a person here does <b>NOT</b> grant them authority to be a legal representative. Only an " +
                "advance directive or state law can grant that authority.)</p>");

        inHospiceCare = new Checkbox("Patient is enrolled in Hospice");
        inHospiceCare.setValue(false);
        hospiceName = new TextField("Name of Agency:");
        hospiceName.setEnabled(false);
        hospicePhoneNumber = new TextField("Agency Phone Number:");
        hospicePhoneNumber.setPlaceholder("(###) ###-####");
        hospicePhoneNumber.setEnabled(false);

        inHospiceCare.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               hospiceName.setEnabled(true);
               hospiceName.clear();
               hospicePhoneNumber.setEnabled(true);
               hospicePhoneNumber.clear();
           }
           else {
               hospiceName.setEnabled(false);
               hospiceName.clear();
               hospicePhoneNumber.setEnabled(false);
               hospicePhoneNumber.clear();
           }
        });

        hospiceLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Contact Information - Hospice Care (Optional)"),
                intro14, new BasicDivider(), inHospiceCare, hospiceName, hospicePhoneNumber);
        hospiceLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        hospiceLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        hospiceLayout.setHeightFull();
        hospiceLayout.setBackgroundColor("white");
        hospiceLayout.setShadow(Shadow.S);
        hospiceLayout.setBorderRadius(BorderRadius.S);
        hospiceLayout.getStyle().set("margin-bottom", "10px");
        hospiceLayout.getStyle().set("margin-right", "10px");
        hospiceLayout.getStyle().set("margin-left", "10px");
        hospiceLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        hospiceLayout.setVisible(false);
    }

    private void createAdvancedDirectiveReview() {
        Html intro15 = new Html("<p>Reviewed patient’s advance directive to confirm no conflict with POLST orders: " +
                "(A POLST form does not replace an advance directive or living will)</p>");
        livingWillReviewed = new Checkbox("Yes");
        reviewedDate = new TextField("Date document reviewed:");
        reviewedDate.setEnabled(false);
        livingWillConflict = new Checkbox("Conflict exists, notified patient (if patient lacks capacity, noted in chart)");
        advanceDirectiveNotAvailable = new Checkbox("Advance directive not available");
        noAdvanceDirective = new Checkbox("No advance directive exists");
        livingWillReviewed.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               reviewedDate.setEnabled(true);
               reviewedDate.setValue(getDateString(new Date()));
               advanceDirectiveNotAvailable.clear();
               noAdvanceDirective.clear();
           }
           else {
               reviewedDate.clear();
               reviewedDate.setEnabled(false);
               livingWillConflict.clear();
           }
        });
        livingWillConflict.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                livingWillReviewed.setValue(true);
                reviewedDate.setEnabled(true);
                advanceDirectiveNotAvailable.clear();
                noAdvanceDirective.clear();
            }
        });
        advanceDirectiveNotAvailable.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               livingWillReviewed.clear();
               reviewedDate.clear();
               reviewedDate.setEnabled(false);
               livingWillConflict.clear();
               noAdvanceDirective.clear();
           }
        });
        noAdvanceDirective.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                livingWillReviewed.clear();
                reviewedDate.clear();
                reviewedDate.setEnabled(false);
                livingWillConflict.clear();
                advanceDirectiveNotAvailable.clear();
            }
        });
        advanceDirectiveReviewLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Form Completion Info - Review (Optional)"),
                intro15, new BasicDivider(), livingWillReviewed, reviewedDate, livingWillConflict, advanceDirectiveNotAvailable, noAdvanceDirective);
        advanceDirectiveReviewLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        advanceDirectiveReviewLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        advanceDirectiveReviewLayout.setHeightFull();
        advanceDirectiveReviewLayout.setBackgroundColor("white");
        advanceDirectiveReviewLayout.setShadow(Shadow.S);
        advanceDirectiveReviewLayout.setBorderRadius(BorderRadius.S);
        advanceDirectiveReviewLayout.getStyle().set("margin-bottom", "10px");
        advanceDirectiveReviewLayout.getStyle().set("margin-right", "10px");
        advanceDirectiveReviewLayout.getStyle().set("margin-left", "10px");
        advanceDirectiveReviewLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        advanceDirectiveReviewLayout.setVisible(false);
    }

    private void createParticipants() {
        Html intro16 = new Html("<p>Reviewed patient’s advance directive to confirm no conflict with POLST orders: " +
                "(A POLST form does not replace an advance directive or living will)</p>");
        patientParticipated = new Checkbox("Patient with decision-making capacity");
        legalOrSurrogate = new Checkbox("Legal Surrogate / Health Care Agent");
        courtAppointedGuardian = new Checkbox("Court Appointed Guardian");
        parentOfMinor = new Checkbox("Parent of Minor");
        otherParticipant = new Checkbox("Other:");
        otherParticipantList = new TextField("");
        otherParticipantList.setPlaceholder("List others that participated here");

        participantLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Form Completion Info - Participant Types (Optional)"),
                intro16, new BasicDivider(), patientParticipated, legalOrSurrogate, courtAppointedGuardian, parentOfMinor, otherParticipant, otherParticipantList);
        participantLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        participantLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        participantLayout.setHeightFull();
        participantLayout.setBackgroundColor("white");
        participantLayout.setShadow(Shadow.S);
        participantLayout.setBorderRadius(BorderRadius.S);
        participantLayout.getStyle().set("margin-bottom", "10px");
        participantLayout.getStyle().set("margin-right", "10px");
        participantLayout.getStyle().set("margin-left", "10px");
        participantLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        participantLayout.setVisible(false);
    }

    private void createWhoAssisted() {
        Html intro17 = new Html("<p>Reviewed patient’s advance directive to confirm no conflict with POLST orders: " +
                                "(A POLST form does not replace an advance directive or living will)</p>");
        Label professional = new Label("Professional Assisting Health Care Provider w/ Form Completion (if applicable):");
        whoAssistedInFormCompletionName = new TextField("Full Name:");
        whoAssistedPhoneNumber = new TextField("Phone #:");
        whoAssistedPhoneNumber.setPlaceholder("(###) ###-####");
        dateAssisted = new TextField("Date Assisted:");
        dateAssisted.setValue(getDateString(new Date()));
        Label individualType = new Label("This individual is the patient’s:");
        socialWorkerAssist = new Checkbox("Social Worker");
        nurseAssisted = new Checkbox("Nurse");
        clergyAssisted = new Checkbox("Clergy");
        otherAssisted = new Checkbox("Other:");
        otherAssistedList = new TextField("");
        otherAssistedList.setPlaceholder("Enter Types");
        otherAssistedList.setEnabled(false);

        socialWorkerAssist.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               nurseAssisted.clear();
               clergyAssisted.clear();
               otherAssisted.clear();
               otherAssistedList.clear();
               otherAssistedList.setEnabled(false);
           }
        });
        nurseAssisted.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               socialWorkerAssist.clear();
               clergyAssisted.clear();
               otherAssisted.clear();
               otherAssistedList.clear();
               otherAssistedList.setEnabled(false);
           }
        });
        clergyAssisted.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               socialWorkerAssist.clear();
               nurseAssisted.clear();
               otherAssisted.clear();
               otherAssistedList.clear();
               otherAssistedList.setEnabled(false);
           }
        });
        otherAssisted.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               socialWorkerAssist.clear();
               nurseAssisted.clear();
               clergyAssisted.clear();
               otherAssistedList.setEnabled(true);
           }
        });
        whoAssistedLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Form Completion Info - Participants (Optional)"),
                intro17, new BasicDivider(), professional, whoAssistedInFormCompletionName, dateAssisted, whoAssistedPhoneNumber, new BasicDivider(), individualType, socialWorkerAssist, nurseAssisted, clergyAssisted, otherAssisted, otherAssistedList);
        whoAssistedLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        whoAssistedLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        whoAssistedLayout.setHeightFull();
        whoAssistedLayout.setBackgroundColor("white");
        whoAssistedLayout.setShadow(Shadow.S);
        whoAssistedLayout.setBorderRadius(BorderRadius.S);
        whoAssistedLayout.getStyle().set("margin-bottom", "10px");
        whoAssistedLayout.getStyle().set("margin-right", "10px");
        whoAssistedLayout.getStyle().set("margin-left", "10px");
        whoAssistedLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        whoAssistedLayout.setVisible(false);
    }
    private Component getFooter() {
        returnButton = new Button("Back", new Icon(VaadinIcon.BACKWARDS));
        returnButton.setEnabled(false);
        returnButton.addClickListener(event -> {
            questionPosition--;
            evalNavigation();
        });
        forwardButton = new Button("Next", new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
            questionPosition++;
            evalNavigation();
        });
        viewStateForm = new Button("View "+consentSession.getPrimaryState()+" instructions");
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
    private String getDateString(Date dt) {
        String pattern = "MM/dd/yyyy";
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

    private String getDateYear(Date dt) {
        String pattern = "yyyy";
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
    private String getDateMonth(Date dt) {
        String pattern = "MM";
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
    private String getDateDay(Date dt) {
        String pattern = "dd";
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
    private void successNotification() {
        Span content = new Span("FHIR Treatment - Portable Medical Order successfully created!");

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }

    private Dialog createInfoDialog() {
        PDFDocumentHandler pdfHandler = new PDFDocumentHandler();
        StreamResource streamResource = pdfHandler.retrievePDFForm("POLST");

        Dialog infoDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button("Close", e -> infoDialog.close());
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

    private void evalNavigation() {
        switch(questionPosition) {
            case 0:
                returnButton.setEnabled(false);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(true);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 1:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(true);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 2:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(true);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 3:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(true);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 4:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(true);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 5:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(true);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 6:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(true);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 7:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(true);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 8:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(true);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 9:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(true);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 10:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(true);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 11:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(true);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(false);
                break;
            case 12:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(true);
                whoAssistedLayout.setVisible(false);
                break;
            case 13:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientGeneralInfoLayout.setVisible(false);
                cardiopulmonaryResuscitationOrdersLayout.setVisible(false);
                initialTreatmentOrders.setVisible(false);
                additionalOrdersLayout.setVisible(false);
                medicallyAssistedNutrition.setVisible(false);
                patientOrRepresentativeSignatureLayout.setVisible(false);
                healthcareProviderSignatureLayout.setVisible(false);
                supervisingPhysicianLayout.setVisible(false);
                emergencyContactLayout.setVisible(false);
                primaryProviderLayout.setVisible(false);
                hospiceLayout.setVisible(false);
                advanceDirectiveReviewLayout.setVisible(false);
                participantLayout.setVisible(false);
                whoAssistedLayout.setVisible(true);
                break;
            case 14:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(false);
                getHumanReadable();
                docDialog.open();
                break;
            default:
                break;
        }
    }

    private void getHumanReadable() {
        StreamResource streamResource = setFieldsCreatePDF();
        docDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");


        Button closeButton = new Button("Cancel", e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));
        closeButton.addClickListener(event -> {
            docDialog.close();
            questionPosition--;
            evalNavigation();
        });

        Button acceptButton = new Button("Accept and Submit");
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            docDialog.close();
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

        HorizontalLayout hLayout = new HorizontalLayout(closeButton, acceptButton);

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
        polst = new POLSTPortableMedicalOrder();
        //demographics
        polst.setPatientFirstName(patientFirstName.getValue());
        polst.setPatientMiddleName(patientMiddleName.getValue());
        polst.setPatientLastName(patientLastName.getValue());
        polst.setPatientPreferredName(patientPreferredName.getValue());
        polst.setPatientSuffix(patientNameSuffix.getValue());
        polst.setGenderF(patientGenderF.getValue());
        polst.setGenderM(patientGenderM.getValue());
        polst.setGenderX(patientGenderX.getValue());
        polst.setPatientDateOfBirth(patientDobMonth.getValue()+"/"+patientDobDay.getValue()+"/"+patientDobYear.getValue());
        polst.setLast4SSN(last4ssn1.getValue()+last4ssn2.getValue()+last4ssn3.getValue()+last4ssn4.getValue());
        polst.setPatientHomeState(consentSession.getPrimaryState());
        //cardiopulmonaryresuscitation
        polst.setYesCPR(yesCPR.getValue());
        polst.setNoCPR(noCPR.getValue());
        //initial treatment
        polst.setFullTreatments(fullTreatments.getValue());
        polst.setSelectiveTreatments(selectiveTreatments.getValue());
        polst.setComfortFocusedTreament(comfortTreatments.getValue());
        //additional orders
        polst.setAdditionalTreatments(additionalOrdersInstructions.getValue());
        //nutrition
        polst.setNutritionByArtificialMeans(provideFeeding.getValue());
        polst.setTrialNutritionByArtificialMeans(trialPeriodFeeding.getValue());
        polst.setNoArtificialMeans(noArtificialMeans.getValue());
        polst.setNoNutritionDecisionMade(discussedNoDecision.getValue());

        //patient or representative signature
        polst.setBase64EncodedSignature(base64PatientOrRepresentativeSignature);
        polst.setRepresentativeSigning(notPatientSigning.getValue());
        polst.setRepresentativeName(patientOrRepresentativeNameField.getValue());
        polst.setRepresentativeAuthority(authorityField.getValue());

        //healthcare provider signature
        polst.setHealthcareProviderFullName(healthcareProviderNameField.getValue());
        polst.setHealthcareProviderLicenseOrCert(healthcareProviderLicenseField.getValue());
        polst.setSignatureDate(getDateString(healthcareProviderSignatureDate));
        polst.setHealthcareProviderPhoneNumber(healthcareProviderPhoneNumberField.getValue());
        polst.setBase64EncodedSignatureHealthcareProvider(base64HealthcareProviderSignature);

        //supervisor
        polst.setSupervisingPhysicianLicense(supervisingPhysicianLicenseField.getValue());
        polst.setRequiredSupervisingPhysicianSignature(supervisorSignatureChk.getValue());
        polst.setBase64EncodedSupervisingPhysicianSignature(base64SupervisingPhysicianSignature);

        //emergency contact
        polst.setEmergencyContactFullName(emergencyContactNameField.getValue());
        polst.setOtherEmergencyType(otherContactType.getValue());
        polst.setLegalSurrogateOrHealthcareAgent(legalOrSurrogate.getValue());
        polst.setEmergencyContactPhoneNumberDay(dayPhoneNumber.getValue());
        polst.setEmergencyContactPhoneNumberNight(nightPhoneNumber.getValue());
        //primary care provider
        polst.setPrimaryPhysicianFullName(primaryProviderName.getValue());
        polst.setPrimaryPhysicianPhoneNumber(primaryProviderPhoneNumber.getValue());
        //hospice
        polst.setInHospice(inHospiceCare.getValue());
        polst.setHospiceAgencyName(hospiceName.getValue());
        polst.setHospiceAgencyPhoneNumber(hospicePhoneNumber.getValue());


        //advance directive review
        polst.setAdvancedDirectiveReviewed(livingWillReviewed.getValue());
        polst.setDateAdvancedDirectiveReviewed(reviewedDate.getValue());
        polst.setAdvanceDirectiveConflictExists(livingWillConflict.getValue());
        polst.setAdvanceDirectiveNotAvailable(advanceDirectiveNotAvailable.getValue());
        polst.setNoAdvanceDirectiveExists(noAdvanceDirective.getValue());

        //who particpated
        polst.setPatientWithDecisionMakingCapacity(patientParticipated.getValue());
        polst.setLegalSurrogateOrHealthcareAgent(legalOrSurrogate.getValue());
        polst.setCourtAppointedGuardian(courtAppointedGuardian.getValue());
        polst.setParentOfMinor(parentOfMinor.getValue());
        polst.setOtherParticipants(otherParticipant.getValue());
        polst.setOtherParticipantsList(otherParticipantList.getValue());

        polst.setAssistingHealthcareProviderFullName(whoAssistedInFormCompletionName.getValue());
        polst.setDateAssistedByHealthcareProvider(dateAssisted.getValue());
        polst.setAssistingHealthcareProviderPhoneNumber(whoAssistedPhoneNumber.getValue());

        polst.setSocialWorker(socialWorkerAssist.getValue());
        polst.setNurse(nurseAssisted.getValue());
        polst.setClergy(clergyAssisted.getValue());
        polst.setAssistingOther(otherAssisted.getValue());
        polst.setAssistingOtherList(otherAssistedList.getValue());

        PDFPOLSTHandler pdfHandler = new PDFPOLSTHandler(pdfSigningService);
        StreamResource res = pdfHandler.retrievePDFForm(polst);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;
    }

    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent polstDirective = new Consent();
        polstDirective.setId("POLST-"+consentSession.getFhirPatientId());
        polstDirective.setStatus(Consent.ConsentState.ACTIVE);
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("adr");
        cConcept.addCoding(coding);
        polstDirective.setScope(cConcept);
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        polstDirective.setCategory(cList);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        polstDirective.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        //todo - this is the deployment and custodian organization for advanced directives and should be valid in fhir consent repository
        orgRef.setReference(orgReference);
        orgRef.setDisplay(orgDisplay);
        refList.add(orgRef);
        polstDirective.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        attachment.setCreation(new Date());
        attachment.setTitle("POLST");


        String encodedString = Base64.getEncoder().encodeToString(consentPDFAsByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        polstDirective.setSource(attachment);

        Consent.provisionComponent provision = new Consent.provisionComponent();
        Period period = new Period();
        LocalDate sDate = LocalDate.now();
        LocalDate eDate = LocalDate.now().plusYears(10);
        Date startDate = Date.from(sDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(eDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        period.setStart(startDate);
        period.setEnd(endDate);

        provision.setPeriod(period);

        polstDirective.setProvision(provision);

        Extension extension = createPortableMedicalOrderQuestionnaireResponse();
        polstDirective.getExtension().add(extension);

        fhirConsentClient.createConsent(polstDirective);
    }

    private Extension createPortableMedicalOrderQuestionnaireResponse() {
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.com/leap/adr/polst");
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/leap-polst-"+consentSession.getFhirPatientId()));
        return extension;
    }

    private void resetFormAndNavigation() {
        last4ssn4.clear();
        last4ssn3.clear();
        last4ssn2.clear();
        last4ssn1.clear();
        yesCPR.clear();
        noCPR.clear();
        fullTreatments.clear();
        selectiveTreatments.clear();
        comfortTreatments.clear();
        additionalOrdersInstructions.clear();
        provideFeeding.clear();
        trialPeriodFeeding.clear();
        noArtificialMeans.clear();
        discussedNoDecision.clear();
        patientOrRepresentativeSignature.clear();
        notPatientSigning.clear();
        patientOrRepresentativeNameField.clear();
        authorityField.clear();
        healthcareProviderSignature.clear();
        healthcareProviderLicenseField.clear();
        healthcareProviderNameField.clear();
        healthcareProviderPhoneNumberField.clear();
        supervisingPhysicianSignature.clear();
        supervisingPhysicianLicenseField.clear();
        supervisorSignatureChk.clear();
        emergencyContactNameField.clear();
        nightPhoneNumber.clear();
        dayPhoneNumber.clear();
        legalOrSurrogate.clear();
        otherContactType.clear();
        primaryProviderPhoneNumber.clear();
        primaryProviderName.clear();
        inHospiceCare.clear();
        hospicePhoneNumber.clear();
        hospiceName.clear();
        livingWillReviewed.clear();
        reviewedDate.clear();
        livingWillConflict.clear();
        advanceDirectiveNotAvailable.clear();
        noAdvanceDirective.clear();
        patientParticipated.clear();
        legalOrSurrogate.clear();
        courtAppointedGuardian.clear();
        parentOfMinor.clear();
        otherParticipant.clear();
        otherParticipantList.clear();
        whoAssistedInFormCompletionName.clear();
        whoAssistedPhoneNumber.clear();
        dateAssisted.clear();
        socialWorkerAssist.clear();
        nurseAssisted.clear();
        clergyAssisted.clear();
        otherAssisted.clear();
        otherAssistedList.clear();

        questionPosition = 0;
    }

    private void createQuestionnaireResponse() {
        questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId("leap-polst-" + consentSession.getFhirPatientId());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/"+consentSession.getFhirPatientId());
        questionnaireResponse.setAuthor(refpatient);
        questionnaireResponse.setAuthored(new Date());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        questionnaireResponse.setSubject(refpatient);
        questionnaireResponse.setQuestionnaire("Questionnaire/leap-polst");


        doNotResuscitateResponse();
        treatmentsResponse();
        additionalOrdersResponse();
        nutritionResponse();
        patientOrAlternateSignatureResponse();
        healthcareProviderSignatureResponse();
        emergencyContactResponse();
        primaryProviderResponse();
        hospiceResponse();
        advanceDirectiveReviewResponse();
        participantsResponse();
        assistingIndividualResponse();

        questionnaireResponse.setItem(responseList);
        fhirQuestionnaireResponse.createQuestionnaireResponse(questionnaireResponse);
    }

    private void doNotResuscitateResponse() {
        //DNR
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1 = createItemBooleanType("1.1", "YES CPR: Attempt Resuscitation, including mechanical ventilation, " +
                "defibrillation and cardioversion. (Requires choosing Full Treatments in Section B)", polst.isYesCPR());
        responseList.add(item1_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2 = createItemBooleanType("1.2", "NO CPR: Do Not Attempt Resuscitation.</b> (May choose any " +
                "option in Section B)", polst.isNoCPR());
        responseList.add(item1_2);
    }

    private void treatmentsResponse() {
        //TREATMENTS
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemBooleanType("2.1", "Full Treatments (required if choose CPR in Section A). Goal: Attempt to " +
                "sustain life by all medically effective means. Provide appropriate medical and surgical treatments as indicated to attempt to prolong life, including intensive care.", polst.isFullTreatments());
        responseList.add(item2_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2", "Selective Treatments. Goal: Attempt to restore function while avoiding intensive " +
                "care and resuscitation efforts (ventilator, defibrillation and cardioversion). May use non-invasive positive airway pressure, antibiotics and IV fluids as indicated. Avoid intensive " +
                "care. Transfer to hospital if treatment needs cannot be met in current location.)", polst.isSelectiveTreatments());
        responseList.add(item2_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_3 = createItemBooleanType("2.3", "Comfort-focused Treatments. Goal: Maximize comfort through symptom management; allow " +
                "natural death. Use oxygen, suction and manual treatment of airway obstruction as needed for comfort. Avoid treatments listed in full or select treatments unless consistent with comfort goal. " +
                "Transfer to hospital <b>only</b> if comfort cannot be achieved in current setting.", polst.isComfortFocusedTreament());
        responseList.add(item2_3);
    }

    private void additionalOrdersResponse() {
        //Additional Orders
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3 = createItemStringType("3", "C. Additional Orders or Instructions - These orders are in addition to those in section B +" +
                "(e.g., blood products, dialysis). [EMS protocols may limit emergency responder ability to act on orders in this section.]", polst.getAdditionalTreatments());
        responseList.add(item3);

        //Nutrition
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1 = createItemBooleanType("4.1", "Provide feeding through new or existing surgically-placed tubes", polst.isNutritionByArtificialMeans());
        responseList.add(item4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2 = createItemBooleanType("4.2", "Trial period for artificial nutrition but no surgically-placed tubes", polst.isTrialNutritionByArtificialMeans());
        responseList.add(item4_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_3 = createItemBooleanType("4.3", "No artificial means of nutrition desired", polst.isNoArtificialMeans());
        responseList.add(item4_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_4 = createItemBooleanType("4.4", "Discussed but no decision made (standard of care provided)", polst.isNoNutritionDecisionMade());
        responseList.add(item4_4);
    }

    private void nutritionResponse() {
        //Nutrition
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1 = createItemBooleanType("4.1", "Provide feeding through new or existing surgically-placed tubes", polst.isNutritionByArtificialMeans());
        responseList.add(item4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2 = createItemBooleanType("4.2", "Trial period for artificial nutrition but no surgically-placed tubes", polst.isTrialNutritionByArtificialMeans());
        responseList.add(item4_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_3 = createItemBooleanType("4.3", "No artificial means of nutrition desired", polst.isNoArtificialMeans());
        responseList.add(item4_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_4 = createItemBooleanType("4.4", "Discussed but no decision made (standard of care provided)", polst.isNoNutritionDecisionMade());
        responseList.add(item4_4);
    }

    private void patientOrAlternateSignatureResponse() {
        //Patient and Alternate Signature
        boolean patientSignatureBool = false;
        if (polst.getBase64EncodedSignature() != null && polst.getBase64EncodedSignature().length > 0) patientSignatureBool = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_1 = createItemBooleanType("5.1", "Patient signature acquired)", patientSignatureBool);
        responseList.add(item5_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_2 = createItemBooleanType("5.2", "If other than patient, enter full name, and provide authority if any.)", polst.isRepresentativeSigning());
        responseList.add(item5_2);
        if (polst.isRepresentativeSigning()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item5_2_1 = createItemStringType("5.2.1", "Full Name", polst.getRepresentativeName());
            responseList.add(item5_2_1);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item5_2_2 = createItemStringType("5.2.2", "Authority", polst.getRepresentativeName());
            responseList.add(item5_2_2);
        }
    }

    private void healthcareProviderSignatureResponse() {
        //Healthcare Provider Signature and Info
        boolean healthcareProviderSignatureBool = false;
        if (polst.getBase64EncodedSignatureHealthcareProvider() != null & polst.getBase64EncodedSignatureHealthcareProvider().length > 0) healthcareProviderSignatureBool = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_1 = createItemBooleanType("6.1", "Healthcare provider signature acquired", healthcareProviderSignatureBool);
        responseList.add(item6_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_2 = createItemStringType("6.2", "Healthcare provider Full Name", polst.getHealthcareProviderFullName());
        responseList.add(item6_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_3 = createItemStringType("6.3", "License/Cert#", polst.getHealthcareProviderLicenseOrCert());
        responseList.add(item6_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_4_1 = createItemBooleanType("6.4.1", "Supervisor Signature Not Applicable", polst.isRequiredSupervisingPhysicianSignature());
        responseList.add(item6_4_1);
        if (!polst.isRequiredSupervisingPhysicianSignature()) {
            boolean supervisingPhysicianSignatureBool = false;
            if (polst.getBase64EncodedSupervisingPhysicianSignature() != null && polst.getBase64EncodedSupervisingPhysicianSignature().length > 0) supervisingPhysicianSignatureBool = true;
            QuestionnaireResponse.QuestionnaireResponseItemComponent item6_4_2 = createItemBooleanType("6.4.2", "Supervisor Signature Not Applicable", supervisingPhysicianSignatureBool);
            responseList.add(item6_4_2);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item6_4_3 = createItemStringType("6.4.3", "License #", polst.getSupervisingPhysicianLicense());
            responseList.add(item6_4_3);
        }
    }

    private void emergencyContactResponse() {
        //emergency contact
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_1 = createItemStringType("7.1", "Full Name", polst.getEmergencyContactFullName());
        responseList.add(item7_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_3_1 = createItemBooleanType("7.3.1", "Legal Representative", legalRepresentative.getValue());
        responseList.add(item7_3_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_3_2 = createItemBooleanType("7.3.2", "Other Contact Type", polst.isOtherEmergencyType());
        responseList.add(item7_3_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_4 = createItemStringType("7.4", "Day Phone Number", polst.getEmergencyContactPhoneNumberDay());
        responseList.add(item7_4);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_5 = createItemStringType("7.5", "Night Phone Number", polst.getEmergencyContactPhoneNumberNight());
        responseList.add(item7_5);
    }

    private void primaryProviderResponse() {
        //primary provider
        QuestionnaireResponse.QuestionnaireResponseItemComponent item8_1 = createItemStringType("8.1", "Primary Provider Name", polst.getPrimaryPhysicianFullName());
        responseList.add(item8_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item8_2 = createItemStringType("8.2", "Phone Number", polst.getPrimaryPhysicianPhoneNumber());
        responseList.add(item8_2);
    }

    private void hospiceResponse() {
        //Hospice
        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_1 = createItemBooleanType("9.1", "Patient is enrolled in Hospice", polst.isInHospice());
        responseList.add(item9_1);
        if (polst.isInHospice()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item9_2 = createItemStringType("9.2", "Name of Agency", polst.getHospiceAgencyName());
            responseList.add(item9_2);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item9_3 = createItemStringType("9.3", "Agency Phone Number", polst.getHospiceAgencyPhoneNumber());
            responseList.add(item9_3);
        }
    }

    private void advanceDirectiveReviewResponse() {
        //Advanced Directive review (optional)
        QuestionnaireResponse.QuestionnaireResponseItemComponent item10_1 = createItemBooleanType("10.1", "Yes - Reviewed patient’s advance directive to confirm " +
                "no conflict with POLST orders: (A POLST form does not replace an advance directive or living will)", polst.isAdvancedDirectiveReviewed());
        responseList.add(item10_1);
        if (polst.isAdvancedDirectiveReviewed()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item10_2 = createItemStringType("10.2", "Date Reviewed", polst.getDateAdvancedDirectiveReviewed());
            responseList.add(item10_2);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item10_3 = createItemBooleanType("10.3", "Conflict exists, patient notified(if patient lacks capacity, noted in chart)", polst.isAdvanceDirectiveConflictExists());
        responseList.add(item10_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item10_4 = createItemBooleanType("10.4", "Advanced Directive not available", polst.isAdvanceDirectiveNotAvailable());
        responseList.add(item10_4);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item10_5 = createItemBooleanType("10.5", "Advanced Directive does not exist", polst.isNoAdvanceDirectiveExists());
        responseList.add(item10_5);
    }

    private void participantsResponse() {
        //Participants
        QuestionnaireResponse.QuestionnaireResponseItemComponent item11_1 = createItemBooleanType("11.1", "Patient with decision making capacity", polst.isPatientWithDecisionMakingCapacity());
        responseList.add(item11_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item11_2 = createItemBooleanType("11.2", "Legal Surrogate/Health care agent", polst.isLegalSurrogateOrHealthcareAgent());
        responseList.add(item11_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item11_3 = createItemBooleanType("11.3", "Court appointed guardian", polst.isCourtAppointedGuardian());
        responseList.add(item11_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item11_4 = createItemBooleanType("11.4", "Parent of minor", polst.isParentOfMinor());
        responseList.add(item11_4);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item11_5 = createItemBooleanType("11.5", "Other", polst.isOtherParticipants());
        responseList.add(item11_5);
        if (polst.isOtherParticipants()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item11_6 = createItemStringType("11.6", "List of other participants", polst.getOtherParticipantsList());
            responseList.add(item11_6);
        }
    }

    private void assistingIndividualResponse() {
        //Professional Assisting Health Care Provider w/ Form Completion (if applicable):
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_1 = createItemStringType("12.1", "Full Name", polst.getAssistingHealthcareProviderFullName());
        responseList.add(item12_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_2 = createItemStringType("12.2", "Date Assisted", polst.getDateAssistedByHealthcareProvider());
        responseList.add(item12_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_3 = createItemStringType("12.3", "Phone Number", polst.getAssistingHealthcareProviderPhoneNumber());
        responseList.add(item12_3);
        //relationship
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_4_1 = createItemBooleanType("12.4.1", "Social Worker", polst.isSocialWorker());
        responseList.add(item12_4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_4_2 = createItemBooleanType("12.4.2", "Nurse", polst.isNurse());
        responseList.add(item12_4_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_4_3 = createItemBooleanType("12.4.3", "Clergy", polst.isClergy());
        responseList.add(item12_4_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item12_4_4 = createItemBooleanType("12.4.4", "Other", polst.isAssistingOther());
        responseList.add(item12_4_4);
        if (polst.isAssistingOther()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item12_4_5 = createItemStringType("12.4.5", "Enter Type", polst.getAssistingOtherList());
            responseList.add(item12_4_5);
        }
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

    private void errorCheck() {
        errorList = new ArrayList<>();
        errorCheckCommon();
        errorCheckSignature();
    }

    private void errorCheckCommon() {
        if (!polst.isYesCPR() && !polst.isNoCPR()) {
            errorList.add(new QuestionnaireError("Cardiopulmonary resusciation orders no selection made.", 1));
        }
        if (!polst.isFullTreatments() && !polst.isSelectiveTreatments() && !polst.isComfortFocusedTreament()) {
            errorList.add(new QuestionnaireError("Initial treatment orders no selection made.", 2));
        }
        if (!polst.isNutritionByArtificialMeans() && !polst.isTrialNutritionByArtificialMeans() && !polst.isNoArtificialMeans() && !polst.isNoNutritionDecisionMade()) {
            errorList.add(new QuestionnaireError("Medically assisted nutrition no selection made.", 4));
        }
    }

    private void errorCheckSignature() {
        //patient or patient representative signature check
        if (base64PatientOrRepresentativeSignature == null || base64PatientOrRepresentativeSignature.length == 0) {
            errorList.add(new QuestionnaireError("Patient or patient's representative signature can not be blank.", 5));
        }
        if (polst.isRepresentativeSigning()) {
            if (polst.getRepresentativeName() == null || polst.getRepresentativeName().isEmpty()) {
                errorList.add(new QuestionnaireError("Patient representative name can not be blank.", 5));
            }
            if (polst.getRepresentativeAuthority() == null || polst.getRepresentativeAuthority().isEmpty()) {
                errorList.add(new QuestionnaireError("Patient representative authority can not be blank.", 5));
            }
        }
        //health care provider signature
        if (base64HealthcareProviderSignature == null || base64HealthcareProviderSignature.length == 0) {
            errorList.add(new QuestionnaireError("Health care provider signature can not be blank.", 6));
        }
        if (polst.getHealthcareProviderFullName() == null || polst.getHealthcareProviderFullName().isEmpty()) {
            errorList.add(new QuestionnaireError("Health care provider name can not be blank.", 6));
        }
        if (polst.getHealthcareProviderPhoneNumber() == null || polst.getHealthcareProviderPhoneNumber().isEmpty()) {
            errorList.add(new QuestionnaireError("Health care provider phone number can not be blank.", 6));
        }
        if (polst.getHealthcareProviderLicenseOrCert() == null || polst.getHealthcareProviderLicenseOrCert().isEmpty()) {
            errorList.add(new QuestionnaireError("Health care provider license or Certificate number can not be blank.", 6));
        }
        if (polst.isRequiredSupervisingPhysicianSignature()) {
            if (base64SupervisingPhysicianSignature == null || base64SupervisingPhysicianSignature.length == 0) {
                errorList.add(new QuestionnaireError("Supervising physician signature is required.", 6));
            }
            if (polst.getSupervisingPhysicianLicense() == null || polst.getSupervisingPhysicianLicense().isEmpty()) {
                errorList.add(new QuestionnaireError("Supervising physician license number required.", 6));
            }
        }
    }

    private void createErrorDialog() {
        Html errorIntro = new Html("<p><b>The following errors were identified. You will need to correct them before saving this consent document.</b></p>");
        Html flowTypeIntro;
        if (advDirectiveFlowType.equals("Default")) {
            flowTypeIntro = new Html("<p>Based on you selection of \"Accept and Submit\" responses to all non-optional questions, signatures, and signature information is required.</p>");
        }
        else {
            flowTypeIntro = new Html("<p>Based on you selection of \"Accept and Get Notarized\" responses to all questions are required. You are expected to print a copy of this " +
                    "consent document and acquire signatures for it in the presence of a notary.  You are then required to scan and upload this document to activate enforcement of it.</p>");
        }

        Button errorBTN = new Button("Correct Errors");
        errorBTN.setWidthFull();
        errorBTN.addClickListener(event -> {
            questionPosition = errorList.get(0).getQuestionnaireIndex();
            errorDialog.close();
            evalNavigation();
        });


        FlexBoxLayout verticalLayout = new FlexBoxLayout();

        verticalLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        verticalLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        verticalLayout.setHeight("350px");
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
        errorDialog.add(createHeader(VaadinIcon.WARNING, "Failed Verification"),errorIntro, flowTypeIntro, verticalLayout, errorBTN);
    }
}
