package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.adr.model.*;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRProvenance;
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
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFLivingWillHandler;
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


@PageTitle("Living Will")
@Route(value = "livingwillview", layout = MainLayout.class)
public class LivingWill extends ViewFrame {
    private static final Logger log = LoggerFactory.getLogger(LivingWill.class);

    private gov.hhs.onc.leap.signature.PDFSigningService PDFSigningService;
    private ConsentSession consentSession;
    private ConsentUser consentUser;

    private Button returnButton;
    private Button forwardButton;
    private Button viewStateForm;
    private int questionPosition = 0;

    private SignaturePad patientInitials;
    private byte[] base64PatientInitials;
    private FlexBoxLayout patientInitialsLayout;

    private TextField patientFullNameField;
    private TextField patientAddress1Field;
    private TextField patientAddress2Field;
    private TextField patientDateOfBirthField;
    private TextField patientEmailAddressField;
    private TextField patientPhoneNumberField;
    private FlexBoxLayout patientGeneralInfoLayout;

    private Checkbox comfortCareOnly;
    private Checkbox comfortCareOnlyButNo;

    private Checkbox noCardioPulmonaryRecusitation;
    private Checkbox noArtificialFluidsOrFood;
    private Checkbox avoidTakingToHospital;
    private FlexBoxLayout notWantedTreatmentsLayout;

    private Checkbox ifPregnantSaveFetus;
    private Checkbox careUntilDoctorConcludesNoHope;
    private Checkbox prolongLifeToGreatestExtentPossible;

    private FlexBoxLayout instructionsLayout;

    private RadioButtonGroup additionalInstructionsButtonGroup;
    private FlexBoxLayout additionalInstructionsLayout;


    private SignaturePad patientSignature;
    private byte[] base64PatientSignature;
    private TextField patientSignatureDateField;
    private Date patientSignatureDate;
    private FlexBoxLayout patientSignatureLayout;

    private SignaturePad patientUnableSignature;
    private byte[] base64PatientUnableSignature;
    private TextField patientUnableSignatureDateField;
    private Date patientUnableSignatureDate;
    private TextField patientUnableSignatureNameField;
    private FlexBoxLayout patientUnableSignatureLayout;

    private SignaturePad witnessSignature;
    private byte[] base64WitnessSignature;
    private TextField witnessSignatureDateField;
    private Date witnessSignatureDate;
    private TextField witnessName;
    private TextField witnessAddress;
    private FlexBoxLayout witnessSignatureLayout;

    private byte[] consentPDFAsByteArray;

    private Dialog docDialog;

    private QuestionnaireResponse questionnaireResponse;
    private List<QuestionnaireResponse.QuestionnaireResponseItemComponent> responseList;

    private gov.hhs.onc.leap.adr.model.LivingWill livingWill;

    private Consent.ConsentState consentState;

    private List<QuestionnaireError> errorList;
    private String advDirectiveFlowType = "Default";
    private Dialog errorDialog;

    private String consentProvenance;
    private String questionnaireProvenance;
    private Date dateRecordedProvenance;



    @Autowired
    private FHIRConsent fhirConsentClient;

    @Autowired
    private PDFSigningService pdfSigningService;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @Autowired
    private FHIRProvenance fhirProvenanceClient;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;

    @PostConstruct
    public void setup() {
        setId("livingwillview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        this.responseList = new ArrayList<>();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html(getTranslation("livingWill-intro"));

        createPatientsInitials();
        createPatientGeneralInfo();
        createHealthcareChoices();
        createAdditionalInstructions();

        createPatientSignature();
        createPatientUnableSignature();
        createWitnessSignature();

        createInfoDialog();

        FlexBoxLayout content = new FlexBoxLayout(intro, patientInitialsLayout, patientGeneralInfoLayout,
                instructionsLayout, additionalInstructionsLayout, patientSignatureLayout, patientUnableSignatureLayout, witnessSignatureLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createPatientsInitials() {
        Html intro2 = new Html(getTranslation("livingWill-intro2"));

        patientInitials = new SignaturePad();
        patientInitials.setHeight("100px");
        patientInitials.setWidth("150px");
        patientInitials.setPenColor("#2874A6");

        Button clearPatientInitials = new Button(getTranslation("livingWill-clear_initials"));
        clearPatientInitials.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientInitials.addClickListener(event -> {
            patientInitials.clear();
        });
        Button savePatientInitials = new Button(getTranslation("livingWill-accept_initials"));
        savePatientInitials.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientInitials.addClickListener(event -> {
            base64PatientInitials = patientInitials.getImageBase64();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientInitials, savePatientInitials);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        patientInitialsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")),intro2, new BasicDivider(), patientInitials, sigLayout);
        patientInitialsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        patientInitialsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        patientInitialsLayout.setHeightFull();
        patientInitialsLayout.setBackgroundColor("white");
        patientInitialsLayout.setShadow(Shadow.S);
        patientInitialsLayout.setBorderRadius(BorderRadius.S);
        patientInitialsLayout.getStyle().set("margin-bottom", "10px");
        patientInitialsLayout.getStyle().set("margin-right", "10px");
        patientInitialsLayout.getStyle().set("margin-left", "10px");
        patientInitialsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    }

    private void createPatientGeneralInfo() {
        Html intro3 = new Html(getTranslation("livingWill-intro3"));
        patientFullNameField = new TextField(getTranslation("livingWill-name"));
        patientAddress1Field = new TextField(getTranslation("livingWill-address"));
        patientAddress2Field = new TextField("");
        patientDateOfBirthField = new TextField(getTranslation("livingWill-date_of_birth"));
        patientPhoneNumberField = new TextField(getTranslation("livingWill-phone"));
        patientEmailAddressField = new TextField(getTranslation("livingWill-email"));

        //set values
        patientFullNameField.setValue(consentUser.getFirstName()+" "+consentUser.getMiddleName()+" "+consentUser.getLastName());
        String addressHolder = "";
        if (consentUser.getStreetAddress2() != null) {
            addressHolder = consentUser.getStreetAddress1() +" "+consentUser.getStreetAddress2();
        }
        else {
            addressHolder = consentUser.getStreetAddress1();
        }
        patientAddress1Field.setValue(addressHolder);
        patientAddress2Field.setValue(consentUser.getCity()+" "+consentUser.getState()+" "+consentUser.getZipCode());
        patientPhoneNumberField.setValue(consentUser.getPhone());
        patientDateOfBirthField.setValue(getDateString(consentUser.getDateOfBirth()));
        patientEmailAddressField.setValue(consentUser.getEmailAddress());

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")),intro3, new BasicDivider(),
                patientFullNameField, patientAddress1Field, patientAddress2Field, patientDateOfBirthField, patientPhoneNumberField, patientEmailAddressField);
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
        patientGeneralInfoLayout.setVisible(false);
    }

    private void createHealthcareChoices() {
        Html intro4 = new Html(getTranslation("livingWill-intro4"));

        comfortCareOnly = new Checkbox();
        comfortCareOnly.setLabel(getTranslation("livingWill-comfort_care_only"));
        comfortCareOnly.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                prolongLifeToGreatestExtentPossible.clear();
            }
        });
        Html intro5 = new Html(getTranslation("livingWill-intro5"));

        comfortCareOnlyButNo = new Checkbox();
        comfortCareOnlyButNo.setLabel(getTranslation("livingWill-comfort_care_only_but_no"));
        comfortCareOnlyButNo.addValueChangeListener(event -> {
           Boolean s = (Boolean)event.getValue();
           if (s) {
               prolongLifeToGreatestExtentPossible.clear();
               notWantedTreatmentsLayout.setVisible(true);
           }
           else {
               notWantedTreatmentsLayout.setVisible(false);
           }
        });

        noCardioPulmonaryRecusitation = new Checkbox();
        noCardioPulmonaryRecusitation.setLabel(getTranslation("livingWill-no_cardio_pulmonary_recusitation"));


        noArtificialFluidsOrFood = new Checkbox();
        noArtificialFluidsOrFood.setLabel(getTranslation("livingWill-no_artificial_fluids_or_food"));


        avoidTakingToHospital = new Checkbox();
        avoidTakingToHospital.setLabel(getTranslation("livingWill-avoid_taking_to_hospital"));

        notWantedTreatmentsLayout = new FlexBoxLayout(noCardioPulmonaryRecusitation, noArtificialFluidsOrFood, avoidTakingToHospital);
        notWantedTreatmentsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        notWantedTreatmentsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        notWantedTreatmentsLayout.setHeightFull();
        notWantedTreatmentsLayout.setBackgroundColor("white");
        notWantedTreatmentsLayout.setShadow(Shadow.S);
        notWantedTreatmentsLayout.setBorderRadius(BorderRadius.S);
        notWantedTreatmentsLayout.getStyle().set("margin-bottom", "10px");
        notWantedTreatmentsLayout.getStyle().set("margin-right", "10px");
        notWantedTreatmentsLayout.getStyle().set("margin-left", "10px");
        notWantedTreatmentsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        notWantedTreatmentsLayout.setVisible(false);

        ifPregnantSaveFetus = new Checkbox();
        ifPregnantSaveFetus.setLabel(getTranslation("livingWill-if_pregnant_save_fetus"));
        if (consentSession.getConsentUser().getGender().equals(getTranslation("livingWill-male"))) {
            ifPregnantSaveFetus.setEnabled(false);
        }
        ifPregnantSaveFetus.addValueChangeListener(event -> {
           Boolean b = (Boolean)event.getValue();
           if (b) {
               prolongLifeToGreatestExtentPossible.clear();
           }
        });
        careUntilDoctorConcludesNoHope = new Checkbox();
        careUntilDoctorConcludesNoHope.setLabel(getTranslation("livingWill-care_until_doctor_concludes_no_hope"));
        careUntilDoctorConcludesNoHope.addValueChangeListener(event -> {
            Boolean b = (Boolean)event.getValue();
            if (b) {
                prolongLifeToGreatestExtentPossible.clear();
            }
        });
        prolongLifeToGreatestExtentPossible = new Checkbox();
        prolongLifeToGreatestExtentPossible.setLabel(getTranslation("livingWill-prolong_life_to_greatest_extent_possible"));
        prolongLifeToGreatestExtentPossible.addValueChangeListener(event -> {
            Boolean s = (Boolean)event.getValue();
            if (s) {
                //clear all others
                try {
                    comfortCareOnlyButNo.clear();
                    comfortCareOnly.clear();
                    noCardioPulmonaryRecusitation.clear();
                    noArtificialFluidsOrFood.clear();
                    avoidTakingToHospital.clear();
                    notWantedTreatmentsLayout.setVisible(false);
                    ifPregnantSaveFetus.clear();
                    careUntilDoctorConcludesNoHope.clear();
                }
                catch(Exception ex) {
                    log.error("Living Will clear instructions error "+ex.getMessage());
                }
            }
        });
        instructionsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")),intro4, new BasicDivider(),
                comfortCareOnly, intro5, comfortCareOnlyButNo, notWantedTreatmentsLayout,
                ifPregnantSaveFetus, careUntilDoctorConcludesNoHope, prolongLifeToGreatestExtentPossible);
        instructionsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        instructionsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        instructionsLayout.setHeightFull();
        instructionsLayout.setBackgroundColor("white");
        instructionsLayout.setShadow(Shadow.S);
        instructionsLayout.setBorderRadius(BorderRadius.S);
        instructionsLayout.getStyle().set("margin-bottom", "10px");
        instructionsLayout.getStyle().set("margin-right", "10px");
        instructionsLayout.getStyle().set("margin-left", "10px");
        instructionsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        instructionsLayout.setVisible(false);
    }

    private void createAdditionalInstructions() {
        Html intro6 = new Html(getTranslation("livingWill-intro6"));

        additionalInstructionsButtonGroup = new RadioButtonGroup();
        additionalInstructionsButtonGroup.setItems(getTranslation("livingWill-additional_instructions_button_group_item1"),
                getTranslation("livingWill-additional_instructions_button_group_item2"));

        //todo create issue and task to attach additional instructions to the pdf that is generated either during this process or after

        additionalInstructionsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")),intro6, new BasicDivider(),
                additionalInstructionsButtonGroup);
        additionalInstructionsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        additionalInstructionsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        additionalInstructionsLayout.setHeightFull();
        additionalInstructionsLayout.setBackgroundColor("white");
        additionalInstructionsLayout.setShadow(Shadow.S);
        additionalInstructionsLayout.setBorderRadius(BorderRadius.S);
        additionalInstructionsLayout.getStyle().set("margin-bottom", "10px");
        additionalInstructionsLayout.getStyle().set("margin-right", "10px");
        additionalInstructionsLayout.getStyle().set("margin-left", "10px");
        additionalInstructionsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        additionalInstructionsLayout.setVisible(false);
    }

    private void createPatientSignature() {
        Html intro11 = new Html(getTranslation("livingWill-intro11"));
        patientSignature = new SignaturePad();
        patientSignature.setHeight("100px");
        patientSignature.setWidth("400px");
        patientSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button(getTranslation("livingWill-clear_signature"));
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            patientSignature.clear();
        });
        Button savePatientSig = new Button(getTranslation("livingWill-accept_signature"));
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

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")), intro11, new BasicDivider(),
                patientSignature, sigLayout);
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
        patientSignatureLayout.setVisible(false);
    }

    private void createPatientUnableSignature() {
        Html intro12 = new Html(getTranslation("livingWill-intro12"));
        Html intro13 = new Html(getTranslation("livingWill-intro13"));

        patientUnableSignatureNameField = new TextField(getTranslation("livingWill-name"));

        patientUnableSignature = new SignaturePad();
        patientUnableSignature.setHeight("100px");
        patientUnableSignature.setWidth("400px");
        patientUnableSignature.setPenColor("#2874A6");

        Button clearPatientUnableSig = new Button(getTranslation("livingWill-clear_signature"));
        clearPatientUnableSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientUnableSig.addClickListener(event -> {
            patientUnableSignature.clear();
        });
        Button savePatientUnableSig = new Button(getTranslation("livingWill-accept_signature"));
        savePatientUnableSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientUnableSig.addClickListener(event -> {
            base64PatientUnableSignature = patientUnableSignature.getImageBase64();
            patientUnableSignatureDate = new Date();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientUnableSig, savePatientUnableSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        patientUnableSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")), intro12, intro13, new BasicDivider(),
                patientUnableSignatureNameField, patientUnableSignature, sigLayout);
        patientUnableSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        patientUnableSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        patientUnableSignatureLayout.setHeightFull();
        patientUnableSignatureLayout.setBackgroundColor("white");
        patientUnableSignatureLayout.setShadow(Shadow.S);
        patientUnableSignatureLayout.setBorderRadius(BorderRadius.S);
        patientUnableSignatureLayout.getStyle().set("margin-bottom", "10px");
        patientUnableSignatureLayout.getStyle().set("margin-right", "10px");
        patientUnableSignatureLayout.getStyle().set("margin-left", "10px");
        patientUnableSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        patientUnableSignatureLayout.setVisible(false);
    }

    private void createWitnessSignature() {
        Html intro14 = new Html(getTranslation("livingWill-intro14"));
        Html intro15 = new Html(getTranslation("livingWill-intro15"));
        Html nextSteps = new Html(getTranslation("livingWill-next_steps"));

        witnessName = new TextField(getTranslation("livingWill-witness_name"));
        witnessAddress = new TextField(getTranslation("livingWill-address"));

        witnessSignature = new SignaturePad();
        witnessSignature.setHeight("100px");
        witnessSignature.setWidth("400px");
        witnessSignature.setPenColor("#2874A6");

        Button clearWitnessSig = new Button(getTranslation("livingWill-clear_signature"));
        clearWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearWitnessSig.addClickListener(event -> {
            witnessSignature.clear();
        });
        Button saveWitnessSig = new Button(getTranslation("livingWill-accept_signature"));
        saveWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveWitnessSig.addClickListener(event -> {
            base64WitnessSignature = witnessSignature.getImageBase64();
            witnessSignatureDate = new Date();
            getHumanReadable();
            docDialog.open();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearWitnessSig, saveWitnessSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        witnessSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("livingWill-living_will")), intro14, intro15, new BasicDivider(),
                witnessName, witnessAddress, witnessSignature, sigLayout, nextSteps);
        witnessSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        witnessSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        witnessSignatureLayout.setHeightFull();
        witnessSignatureLayout.setBackgroundColor("white");
        witnessSignatureLayout.setShadow(Shadow.S);
        witnessSignatureLayout.setBorderRadius(BorderRadius.S);
        witnessSignatureLayout.getStyle().set("margin-bottom", "10px");
        witnessSignatureLayout.getStyle().set("margin-right", "10px");
        witnessSignatureLayout.getStyle().set("margin-left", "10px");
        witnessSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        witnessSignatureLayout.setVisible(false);
    }
    private Component getFooter() {
        returnButton = new Button(getTranslation("livingWill-back"), new Icon(VaadinIcon.BACKWARDS));
        returnButton.setEnabled(false);
        returnButton.addClickListener(event -> {
            questionPosition--;
            evalNavigation();
        });
        forwardButton = new Button(getTranslation("livingWill-next"), new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
            questionPosition++;
            evalNavigation();
        });
        viewStateForm = new Button(getTranslation("livingWill-view_state"));
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

    private void successNotification() {
        Span content = new Span(getTranslation("livingWill-success_notification"));

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }

    private void evalNavigation() {
        switch(questionPosition) {
            case 0:
                returnButton.setEnabled(false);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(true);
                patientGeneralInfoLayout.setVisible(false);
                instructionsLayout.setVisible(false);
                additionalInstructionsLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 1:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(true);
                instructionsLayout.setVisible(false);
                additionalInstructionsLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 2:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                instructionsLayout.setVisible(true);
                additionalInstructionsLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 3:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                instructionsLayout.setVisible(false);
                additionalInstructionsLayout.setVisible(true);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 4:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                instructionsLayout.setVisible(false);
                additionalInstructionsLayout.setVisible(false);
                patientSignatureLayout.setVisible(true);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 5:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                instructionsLayout.setVisible(false);
                additionalInstructionsLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(true);
                witnessSignatureLayout.setVisible(false);
                break;
            case 6:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(false);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                instructionsLayout.setVisible(false);
                additionalInstructionsLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(true);
                break;
            default:
                break;
        }
    }
    private Dialog createInfoDialog() {
        PDFDocumentHandler pdfHandler = new PDFDocumentHandler();
        StreamResource streamResource = pdfHandler.retrievePDFForm("LivingWill");

        Dialog infoDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button(getTranslation("livingWill-close"), e -> infoDialog.close());
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


        Button closeButton = new Button(getTranslation("livingWill-cancel"), e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button(getTranslation("livingWill-accept_and_submit"));
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
                consentState = Consent.ConsentState.ACTIVE;
                createQuestionnaireResponse();
                createFHIRConsent();
                createFHIRProvenance();
                successNotification();
                //todo test for fhir consent create success
                resetFormAndNavigation();
                evalNavigation();
            }
        });

        Button acceptAndPrintButton = new Button(getTranslation("livingWill-accept_and_get_notarized"));
        acceptAndPrintButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptAndPrintButton.addClickListener(event -> {
            docDialog.close();
            advDirectiveFlowType = "Notary";
            errorCheck();
            if (errorList.size() > 0) {
                createErrorDialog();
                errorDialog.open();
            }
            else {
                consentState = Consent.ConsentState.PROPOSED;
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
        livingWill = new gov.hhs.onc.leap.adr.model.LivingWill();
        //Set principle
        Principle principle = new Principle();
        principle.setAddress1(patientAddress1Field.getValue());
        principle.setAddress2(patientAddress2Field.getValue());
        principle.setDateOfBirth(patientDateOfBirthField.getValue());
        principle.setEmailAddress(patientEmailAddressField.getValue());
        principle.setName(patientFullNameField.getValue());
        principle.setPhoneNumber(patientPhoneNumberField.getValue());
        livingWill.setPrinciple(principle);

        //instructions
        if (prolongLifeToGreatestExtentPossible.getValue()) {
            livingWill.setProlongLifeToGreatestExtentPossible(true);
        }
        else {
            if (comfortCareOnly.getValue()) {
                livingWill.setComfortCareOnly(true);
            }
            if (comfortCareOnlyButNo.getValue()) {
                livingWill.setComfortCareOnlyButNot(true);
                if (noCardioPulmonaryRecusitation.getValue()) {
                    livingWill.setNoCardioPulmonaryRecusitation(true);
                }
                if (noArtificialFluidsOrFood.getValue()) {
                    livingWill.setNoArtificalFluidsFoods(true);
                }
                if (avoidTakingToHospital.getValue()) {
                    livingWill.setAvoidTakingToHospital(true);
                }
            }
            if (ifPregnantSaveFetus.getValue()) {
                livingWill.setPregnantSaveFetus(true);
            }
            if (careUntilDoctorConcludesNoHope.getValue()) {
                livingWill.setCareUntilDoctorsConcludeNoHope(true);
            }
        }

        //additional instructions
        String addlInstructions = (String)additionalInstructionsButtonGroup.getValue();
        if (addlInstructions != null &&  addlInstructions.contains(getTranslation("livingWill-i_have_not_attached"))) {
            livingWill.setNoAdditionalInstructions(true);
        }
        else if (addlInstructions != null && addlInstructions.contains(getTranslation("livingWill-i_have_attached"))) {
            livingWill.setAdditionalInstructions(true);
        }
        else {
            //nothing selected in this section assumes all false
        }


        PrincipleSignature principleSignature = new PrincipleSignature();
        principleSignature.setBase64EncodeSignature(base64PatientSignature);
        principleSignature.setDateSigned(getDateString(new Date()));
        livingWill.setPrincipleSignature(principleSignature);

        PrincipleAlternateSignature principleAlternateSignature = new PrincipleAlternateSignature();
        principleAlternateSignature.setBase64EncodedSignature(base64PatientUnableSignature);
        principleAlternateSignature.setNameOfWitnessOrNotary(patientUnableSignatureNameField.getValue());
        principleAlternateSignature.setDateSigned(getDateString(new Date()));
        livingWill.setPrincipleAlternateSignature(principleAlternateSignature);

        WitnessSignature witnessSignature = new WitnessSignature();
        witnessSignature.setBase64EncodedSignature(base64WitnessSignature);
        witnessSignature.setDateSigned(getDateString(new Date()));
        witnessSignature.setWitnessAddress(witnessAddress.getValue());
        witnessSignature.setWitnessName(witnessName.getValue());
        livingWill.setWitnessSignature(witnessSignature);

        PDFLivingWillHandler pdfHandler = new PDFLivingWillHandler(pdfSigningService);
        StreamResource res = pdfHandler.retrievePDFForm(livingWill, base64PatientInitials);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;
    }
    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent poaDirective = new Consent();
        poaDirective.setId("LivingWill-"+consentSession.getFhirPatientId());
        poaDirective.setStatus(consentState);
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("adr");
        cConcept.addCoding(coding);
        poaDirective.setScope(cConcept);
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        poaDirective.setCategory(cList);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        poaDirective.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        //todo - this is the deployment and custodian organization for advanced directives and should be valid in fhir consent repository
        orgRef.setReference(orgReference);
        orgRef.setDisplay(orgDisplay);
        refList.add(orgRef);
        poaDirective.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        dateRecordedProvenance = new Date();
        attachment.setCreation(dateRecordedProvenance);
        attachment.setTitle("LivingWill");


        String encodedString = Base64.getEncoder().encodeToString(consentPDFAsByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        poaDirective.setSource(attachment);

        Consent.provisionComponent provision = new Consent.provisionComponent();
        Period period = new Period();
        LocalDate sDate = LocalDate.now();
        LocalDate eDate = LocalDate.now().plusYears(10);
        Date startDate = Date.from(sDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(eDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        period.setStart(startDate);
        period.setEnd(endDate);

        provision.setPeriod(period);

        List<Coding> purposeList = new ArrayList<>();
        Coding purposeCoding = new Coding();
        purposeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActReason");
        purposeCoding.setCode("ETREAT");
        purposeList.add(purposeCoding);

        provision.setPurpose(purposeList);

        poaDirective.setProvision(provision);

        Extension extension = createLivingWillQuestionnaireResponse();
        poaDirective.getExtension().add(extension);

        Consent completedConsent = fhirConsentClient.createConsent(poaDirective);
        consentProvenance = "Consent/"+poaDirective.getId();
    }

    private void createFHIRProvenance() {
        try {
            fhirProvenanceClient.createProvenance(consentProvenance, dateRecordedProvenance, questionnaireProvenance);
        }
        catch (Exception ex) {
            log.warn("Error creating provenance resource. "+ex.getMessage());
        }
    }

    private Extension createLivingWillQuestionnaireResponse() {
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.com/leap/adr/livingwill");
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/leap-livingwill-"+consentSession.getFhirPatientId()));
        return extension;
    }

    private void resetFormAndNavigation() {
        patientInitials.clear();
        comfortCareOnlyButNo.clear();
        comfortCareOnly.clear();
        noArtificialFluidsOrFood.clear();
        noCardioPulmonaryRecusitation.clear();
        avoidTakingToHospital.clear();
        ifPregnantSaveFetus.clear();
        careUntilDoctorConcludesNoHope.clear();
        prolongLifeToGreatestExtentPossible.clear();
        additionalInstructionsButtonGroup.clear();
        patientSignature.clear();
        patientUnableSignature.clear();
        patientUnableSignatureNameField.clear();
        witnessSignature.clear();
        witnessAddress.clear();
        witnessName.clear();

        questionPosition = 0;
    }

    private void createQuestionnaireResponse() {
        questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId("leap-livingwill-" + consentSession.getFhirPatientId());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/"+consentSession.getFhirPatientId());
        questionnaireResponse.setAuthor(refpatient);
        questionnaireResponse.setAuthored(new Date());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        questionnaireResponse.setSubject(refpatient);
        questionnaireResponse.setQuestionnaire("Questionnaire/leap-livingwill");

        lifeSustainingDecisionsResponse();
        additionalInstructionsResponse();
        signatureRequirementsResponse();

        questionnaireResponse.setItem(responseList);
        QuestionnaireResponse completedQuestionnaireResponse = fhirQuestionnaireResponse.createQuestionnaireResponse(questionnaireResponse);
        questionnaireProvenance = "QuestionnaireResponse/"+questionnaireResponse.getId();
    }

    private void lifeSustainingDecisionsResponse() {
        //general statements and selections
        if (!livingWill.isProlongLifeToGreatestExtentPossible()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1 = createItemBooleanType("1.1", getTranslation("livingWill-item1_1"), livingWill.isComfortCareOnly());
            responseList.add(item1_1);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2 = createItemBooleanType("1.2", getTranslation("livingWill-item1_2"), livingWill.isComfortCareOnlyButNot());
            responseList.add(item1_2);
            if (livingWill.isComfortCareOnlyButNot()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_1 = createItemBooleanType("1.3.1", getTranslation("livingWill-item1_3_1"), livingWill.isNoCardioPulmonaryRecusitation());
                responseList.add(item1_3_1);
                QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_2 = createItemBooleanType("1.3.2",  getTranslation("livingWill-item1_3_2"), livingWill.isNoArtificalFluidsFoods());
                responseList.add(item1_3_2);
                QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_3 = createItemBooleanType("1.3.3",  getTranslation("livingWill-item1_3_3"), livingWill.isAvoidTakingToHospital());
                responseList.add(item1_3_3);
            }
            if (consentSession.getConsentUser().getGender().equals("F")) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item1_4 = createItemBooleanType("1.4",  getTranslation("livingWill-item1_4"), livingWill.isPregnantSaveFetus());
                responseList.add(item1_4);
            }
            QuestionnaireResponse.QuestionnaireResponseItemComponent item1_5 = createItemBooleanType("1.5",  getTranslation("livingWill-item1_5"), livingWill.isCareUntilDoctorsConcludeNoHope());
            responseList.add(item1_5);
        }
        else {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item1_6 = createItemBooleanType("1.6",  getTranslation("livingWill-item1_6"), livingWill.isProlongLifeToGreatestExtentPossible());
            responseList.add(item1_6);
        }
    }

    private void additionalInstructionsResponse() {
        //attachements additional instructions
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemBooleanType("2.1", getTranslation("livingWill-item2_1"),
                livingWill.isNoAdditionalInstructions());
        responseList.add(item2_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2", getTranslation("livingWill-item2_2"),
                livingWill.isAdditionalInstructions());
        responseList.add(item2_2);
    }

    private void signatureRequirementsResponse() {
        //signature requirements
        boolean patientSignatureBool = false;
        if (livingWill.getPrincipleSignature().getBase64EncodeSignature() != null && livingWill.getPrincipleSignature().getBase64EncodeSignature().length > 0) patientSignatureBool = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_1 = createItemBooleanType("3.1", getTranslation("livingWill-item3_1"),
                patientSignatureBool);
        responseList.add(item3_1);
        boolean patientAlternateSignatureBool = false;
        if (livingWill.getPrincipleAlternateSignature().getBase64EncodedSignature() != null && livingWill.getPrincipleAlternateSignature().getBase64EncodedSignature().length > 0) patientAlternateSignatureBool = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2 = createItemBooleanType("3.2", getTranslation("livingWill-item3_2"),
                patientAlternateSignatureBool);
        responseList.add(item3_2);
        boolean witnessSignatureBool = false;
        if (livingWill.getWitnessSignature().getBase64EncodedSignature() != null && livingWill.getWitnessSignature().getBase64EncodedSignature().length > 0) witnessSignatureBool = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3 = createItemBooleanType("3.3", getTranslation("livingWill-item3_3"),
                witnessSignatureBool);
        responseList.add(item3_3);
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
        if (advDirectiveFlowType.equals("Default")) {
            errorCheckCommon();
            errorCheckSignature();
        }
        else if (advDirectiveFlowType.equals("Notary")) {
            errorCheckCommon();
        }
        else {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-questionnaire_error"), 0));
        }
    }

    private void errorCheckCommon() {
        try {
            if (base64PatientInitials == null || base64PatientInitials.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("livingWill-user_initial_can_not_be_blank"), 0));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-user_initial_can_not_be_blank"), 0));
        }
        if (!livingWill.isProlongLifeToGreatestExtentPossible() && !livingWill.isComfortCareOnlyButNot() && !livingWill.isComfortCareOnly() && !livingWill.isCareUntilDoctorsConcludeNoHope() && !livingWill.isPregnantSaveFetus()) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-no_selections_made_in_health_choices"), 2));
        }
        if (!livingWill.isNoAdditionalInstructions() && !livingWill.isAdditionalInstructions()) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-no_selections_made_in_additional_instructions"), 3));
        }
    }

    private void errorCheckSignature() {
        try {
            if (base64PatientSignature == null || base64PatientSignature.length == 0) {

                if (base64PatientUnableSignature == null || base64PatientUnableSignature.length == 0) {
                    errorList.add(new QuestionnaireError(getTranslation("livingWill-user_signature_or_alternate_signature_required"), 4));
                }
                else {
                    try {
                        if (livingWill.getPrincipleAlternateSignature().getNameOfWitnessOrNotary() == null || livingWill.getPrincipleAlternateSignature().getNameOfWitnessOrNotary().isEmpty()) {
                            errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_or_notary_as_alternate_name_required"), 5));
                        }
                    }
                    catch (Exception ex) {
                        errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_or_notary_as_alternate_name_required"), 5));
                    }
                }
            }
        }
        catch(Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-user_signature_or_alternate_signature_required"), 4));
        }

        try {
            if (base64WitnessSignature == null || base64WitnessSignature.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_signature_can_not_be_blank"), 6));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_signature_can_not_be_blank"), 6));
        }
        try {
            if (livingWill.getWitnessSignature().getWitnessName() == null || livingWill.getWitnessSignature().getWitnessName().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_name_can_not_be_blank"), 6));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_name_can_not_be_blank"), 6));
        }
        try {
            if (livingWill.getWitnessSignature().getWitnessAddress() == null || livingWill.getWitnessSignature().getWitnessAddress().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_address_can_not_be_blank"), 6));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("livingWill-witness_address_can_not_be_blank"), 6));
        }
    }

    private void createErrorDialog() {
        Html errorIntro = new Html(getTranslation("livingWill-errorIntro"));
        Html flowTypeIntro;
        if (advDirectiveFlowType.equals("Default")) {
            flowTypeIntro = new Html(getTranslation("livingWill-low_type_intro1"));
        }
        else {
            flowTypeIntro = new Html(getTranslation("livingWill-low_type_intro2"));
        }

        Button errorBTN = new Button(getTranslation("livingWill-correct_errors"));
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
        errorDialog.add(createHeader(VaadinIcon.WARNING, getTranslation("livingWill-failed_verification")),errorIntro, flowTypeIntro, verticalLayout, errorBTN);
    }
}
