package gov.hhs.onc.leap.ui.views;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.backend.ConsentUser;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    // if required by state
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

    @Autowired
    private FHIRConsent fhirConsentClient;

    @Autowired
    private PDFSigningService pdfSigningService;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;

    @PostConstruct
    public void setup() {
        setId("portablemedicalorderview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html("<p>Health care providers, the patient, or patient representative, should complete this form only after the "+
                "health care provider has had conversation with their patient or the patient’s representative.  "+
                "The POLST decision-making process is for patients who are at risk for a life-threatening clinical event because they have a serious life-limiting medical "+
                "condition, which may include advanced frailty (www.polst.org/guidance-appropriate-patients-pdf).</p>" );

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
        Html intro2 = new Html("<p><b>This is a medical order, not an advance directive. For information about POLST and to understand this document, visit: www.polst.org/form</b></p>");

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
        if (consentUser.getGender().equals("M")) {
            patientGenderM.setValue(true);
        }
        else if (consentUser.getGender().equals("F")) {
            patientGenderF.setValue(true);
        }
        else if (consentUser.getGender().equals("X")) {
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
        Html intro8 = new Html("<p><b>The most recently completed valid POLST form supersedes all" +
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
               supervisingPhysicianSignature.setReadOnly(true);
               supervisingPhysicianLicenseField.setEnabled(false);
           }
           else {
               clearPatientSig.setEnabled(true);
               savePatientSig.setEnabled(true);
               supervisingPhysicianSignature.clear();
               supervisingPhysicianSignature.setReadOnly(false);
               supervisingPhysicianLicenseField.setEnabled(true);
           }
        });
        supervisingPhysicianLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Supervising Physicians Signature"),
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

        primaryProviderName = new TextField("Primary Provider:");
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
        advanceDirectiveReviewLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Form Completion Info - Review(Optional)"),
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
        parentOfMinor = new Checkbox("Parent of minor");
        otherParticipant = new Checkbox("Other:");
        otherParticipantList = new TextField("");
        otherParticipantList.setPlaceholder("List others that participated here");

        participantLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Form Completion Info - Participant Types(Optional)"),
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
        whoAssistedLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Form Completion Info - Participants(Optional)"),
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
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(dt);
        return date;
    }

    private String getDateYear(Date dt) {
        String pattern = "yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(dt);
        return date;
    }
    private String getDateMonth(Date dt) {
        String pattern = "MM";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(dt);
        return date;
    }
    private String getDateDay(Date dt) {
        String pattern = "dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(dt);
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
                forwardButton.setEnabled(false);
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
            default:
                break;
        }
    }
}
