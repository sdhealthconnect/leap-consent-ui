package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.adr.model.*;
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
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFPOAMentalHealthHandler;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@PageTitle("Mental Health Care Power of Attorney")
@Route(value = "mentalhealthpowerofattorney", layout = MainLayout.class)
public class MentalHealthPowerOfAttorney extends ViewFrame {
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

    private TextField poaFullNameField;
    private TextField poaAddress1Field;
    private TextField poaAddress2Field;
    private TextField poaHomePhoneField;
    private TextField poaWorkPhoneField;
    private TextField poaCellPhoneField;
    private FlexBoxLayout poaSelectionLayout;

    private TextField altFullNameField;
    private TextField altAddress1Field;
    private TextField altAddress2Field;
    private TextField altHomePhoneField;
    private TextField altWorkPhoneField;
    private TextField altCellPhoneField;
    private FlexBoxLayout altSelectionLayout;

    private FlexBoxLayout authorizationLayout;
    private RadioButtonGroup authorizedDecisions1;
    private RadioButtonGroup authorizedDecisions2;
    private RadioButtonGroup authorizedDecisions3;
    private RadioButtonGroup authorizedDecisions4;
    private TextField authOtherDecisionsField1;
    private TextField authOtherDecisionsField2;
    private TextField authOtherDecisionsField3;

    private TextField authException1Field;
    private TextField authException2Field;
    private FlexBoxLayout authExceptionLayout;

    private FlexBoxLayout revocationLayout;

    private RadioButtonGroup hipaaButton;
    private FlexBoxLayout hipaaLayout;

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

    private PowerOfAttorneyMentalHealth poa;

    private Consent.ConsentState consentState;

    private List<QuestionnaireError> errorList;
    private String advDirectiveFlowType = "Default";
    private Dialog errorDialog;

    @Autowired
    private PDFSigningService pdfSigningService;

    @Autowired
    private FHIRConsent fhirConsentClient;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;

    @PostConstruct
    public void setup() {
        setId("mentalhealthpowerofattorney");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        this.responseList = new ArrayList<>();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html(getTranslation("MentalHealthPOA-intro"));

        createPatientsInitials();
        createPatientGeneralInfo();
        createPOASelection();
        createALTSelection();
        createAuthorizationSelection();
        createAuthExceptionSelection();
        createRevocationStatement();
        createHipaa();
        createPatientSignature();
        createPatientUnableSignature();
        createWitnessSignature();

        createInfoDialog();

        FlexBoxLayout content = new FlexBoxLayout(intro, patientInitialsLayout, patientGeneralInfoLayout, poaSelectionLayout,
                altSelectionLayout, authorizationLayout, authExceptionLayout, revocationLayout, hipaaLayout, patientSignatureLayout, patientUnableSignatureLayout, witnessSignatureLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createPatientsInitials() {
        Html intro2 = new Html(getTranslation("MentalHealthPOA-intro2"));

        patientInitials = new SignaturePad();
        patientInitials.setHeight("100px");
        patientInitials.setWidth("150px");
        patientInitials.setPenColor("#2874A6");

        Button clearPatientInitials = new Button(getTranslation("MentalHealthPOA-clear_initials"));
        clearPatientInitials.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientInitials.addClickListener(event -> {
            patientInitials.clear();
        });
        Button savePatientInitials = new Button(getTranslation("MentalHealthPOA-accept_initials"));
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

        patientInitialsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro2, new BasicDivider(), patientInitials, sigLayout);
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
        Html intro3 = new Html(getTranslation("MentalHealthPOA-intro3"));

        patientFullNameField = new TextField(getTranslation("MentalHealthPOA-name"));
        patientAddress1Field = new TextField(getTranslation("MentalHealthPOA-address"));
        patientAddress2Field = new TextField("");
        patientDateOfBirthField = new TextField(getTranslation("MentalHealthPOA-dob"));
        patientPhoneNumberField = new TextField(getTranslation("MentalHealthPOA-phone"));
        patientEmailAddressField = new TextField(getTranslation("MentalHealthPOA-email"));

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

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro3, new BasicDivider(),
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
    private void createPOASelection() {
        Html intro4 = new Html(getTranslation("MentalHealthPOA-intro4"));

        poaFullNameField = new TextField(getTranslation("MentalHealthPOA-name"));
        poaAddress1Field = new TextField(getTranslation("MentalHealthPOA-address"));
        poaAddress2Field = new TextField("");
        poaHomePhoneField = new TextField(getTranslation("MentalHealthPOA-home_phone"));
        poaWorkPhoneField = new TextField(getTranslation("MentalHealthPOA-work_phone"));
        poaCellPhoneField = new TextField(getTranslation("MentalHealthPOA-cell_phone"));

        poaSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro4, new BasicDivider(),
                poaFullNameField, poaAddress1Field, poaAddress2Field, poaHomePhoneField, poaWorkPhoneField, poaCellPhoneField);
        poaSelectionLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        poaSelectionLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        poaSelectionLayout.setHeightFull();
        poaSelectionLayout.setBackgroundColor("white");
        poaSelectionLayout.setShadow(Shadow.S);
        poaSelectionLayout.setBorderRadius(BorderRadius.S);
        poaSelectionLayout.getStyle().set("margin-bottom", "10px");
        poaSelectionLayout.getStyle().set("margin-right", "10px");
        poaSelectionLayout.getStyle().set("margin-left", "10px");
        poaSelectionLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        poaSelectionLayout.setVisible(false);
    }
    private void createALTSelection() {
        Html intro5 = new Html(getTranslation("MentalHealthPOA-intro5"));

        altFullNameField = new TextField(getTranslation("MentalHealthPOA-name"));
        altAddress1Field = new TextField(getTranslation("MentalHealthPOA-address"));
        altAddress2Field = new TextField("");
        altHomePhoneField = new TextField(getTranslation("MentalHealthPOA-home_phone"));
        altWorkPhoneField = new TextField(getTranslation("MentalHealthPOA-work_phone"));
        altCellPhoneField = new TextField(getTranslation("MentalHealthPOA-cell_phone"));

        altSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro5, new BasicDivider(),
                altFullNameField, altAddress1Field, altAddress2Field, altHomePhoneField, altWorkPhoneField, altCellPhoneField);
        altSelectionLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        altSelectionLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        altSelectionLayout.setHeightFull();
        altSelectionLayout.setBackgroundColor("white");
        altSelectionLayout.setShadow(Shadow.S);
        altSelectionLayout.setBorderRadius(BorderRadius.S);
        altSelectionLayout.getStyle().set("margin-bottom", "10px");
        altSelectionLayout.getStyle().set("margin-right", "10px");
        altSelectionLayout.getStyle().set("margin-left", "10px");
        altSelectionLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        altSelectionLayout.setVisible(false);
    }

    private void createAuthorizationSelection() {
        Html intro6 = new Html(getTranslation("MentalHealthPOA-intro6"));
        Html intro7 = new Html(getTranslation("MentalHealthPOA-intro7"));

        authorizedDecisions1 = new RadioButtonGroup();
        authorizedDecisions1.setLabel("");
        authorizedDecisions1.setItems(getTranslation("MentalHealthPOA-authorized_decisions_1_item"));
        authorizedDecisions2 = new RadioButtonGroup();
        authorizedDecisions2.setLabel("");
        authorizedDecisions2.setItems(getTranslation("MentalHealthPOA-authorized_decisions_2_item"));
        authorizedDecisions3 = new RadioButtonGroup();
        authorizedDecisions3.setLabel("");
        authorizedDecisions3.setItems(getTranslation("MentalHealthPOA-authorized_decisions_3_item"));
        authorizedDecisions4 = new RadioButtonGroup();
        authorizedDecisions4.setLabel("");
        authorizedDecisions4.setItems(getTranslation("MentalHealthPOA-authorized_decisions_4_item"));
        authorizedDecisions4.addValueChangeListener(event -> {
            try {
                String sVal = (String) event.getValue();
                if (sVal.contains("Other:")) {
                    authOtherDecisionsField1.setVisible(true);
                    authOtherDecisionsField2.setVisible(true);
                    authOtherDecisionsField3.setVisible(true);
                } else {
                    authOtherDecisionsField1.setVisible(false);
                    authOtherDecisionsField2.setVisible(false);
                    authOtherDecisionsField3.setVisible(false);
                }
            } catch (Exception ex) {}
            });
        authOtherDecisionsField3 = new TextField();
        authOtherDecisionsField3.setVisible(false);
        authOtherDecisionsField2 = new TextField();
        authOtherDecisionsField2.setVisible(false);
        authOtherDecisionsField1 = new TextField();
        authOtherDecisionsField1.setVisible(false);

        authorizationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro6, intro7, new BasicDivider(),
                authorizedDecisions1, authorizedDecisions2, authorizedDecisions3, authorizedDecisions4, authOtherDecisionsField1, authOtherDecisionsField2, authOtherDecisionsField3);
        authorizationLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        authorizationLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        authorizationLayout.setHeightFull();
        authorizationLayout.setBackgroundColor("white");
        authorizationLayout.setShadow(Shadow.S);
        authorizationLayout.setBorderRadius(BorderRadius.S);
        authorizationLayout.getStyle().set("margin-bottom", "10px");
        authorizationLayout.getStyle().set("margin-right", "10px");
        authorizationLayout.getStyle().set("margin-left", "10px");
        authorizationLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        authorizationLayout.setVisible(false);
    }

    private void createAuthExceptionSelection() {
        Html intro8 = new Html(getTranslation("MentalHealthPOA-intro8"));

        authException1Field = new TextField("");
        authException2Field = new TextField("");

        authExceptionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro8, new BasicDivider(),
                authException1Field, authException2Field);
        authExceptionLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        authExceptionLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        authExceptionLayout.setHeightFull();
        authExceptionLayout.setBackgroundColor("white");
        authExceptionLayout.setShadow(Shadow.S);
        authExceptionLayout.setBorderRadius(BorderRadius.S);
        authExceptionLayout.getStyle().set("margin-bottom", "10px");
        authExceptionLayout.getStyle().set("margin-right", "10px");
        authExceptionLayout.getStyle().set("margin-left", "10px");
        authExceptionLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        authExceptionLayout.setVisible(false);
    }

    private void createRevocationStatement() {
        Html intro9 = new Html(getTranslation("MentalHealthPOA-intro9"));

        revocationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")),intro9, new BasicDivider());
        revocationLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        revocationLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        revocationLayout.setHeightFull();
        revocationLayout.setBackgroundColor("white");
        revocationLayout.setShadow(Shadow.S);
        revocationLayout.setBorderRadius(BorderRadius.S);
        revocationLayout.getStyle().set("margin-bottom", "10px");
        revocationLayout.getStyle().set("margin-right", "10px");
        revocationLayout.getStyle().set("margin-left", "10px");
        revocationLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        revocationLayout.setVisible(false);
    }

    private void createHipaa() {
        Html intro10 = new Html(getTranslation("MentalHealthPOA-intro10"));

        hipaaButton = new RadioButtonGroup();
        hipaaButton.setItems(getTranslation("MentalHealthPOA-hipaa_item1"));
        hipaaButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        hipaaLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")), intro10, new BasicDivider(),
                hipaaButton);
        hipaaLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        hipaaLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        hipaaLayout.setHeightFull();
        hipaaLayout.setBackgroundColor("white");
        hipaaLayout.setShadow(Shadow.S);
        hipaaLayout.setBorderRadius(BorderRadius.S);
        hipaaLayout.getStyle().set("margin-bottom", "10px");
        hipaaLayout.getStyle().set("margin-right", "10px");
        hipaaLayout.getStyle().set("margin-left", "10px");
        hipaaLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        hipaaLayout.setVisible(false);
    }

    private void createPatientSignature() {
        Html intro11 = new Html(getTranslation("MentalHealthPOA-intro11"));
        Html principalLBL = new Html(getTranslation("MentalHealthPOA-principal_LBL"));

        patientSignature = new SignaturePad();
        patientSignature.setHeight("100px");
        patientSignature.setWidth("400px");
        patientSignature.setPenColor("#2874A6");



        Button clearPatientSig = new Button(getTranslation("MentalHealthPOA-clear_signature"));
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            patientSignature.clear();
        });
        Button savePatientSig = new Button(getTranslation("MentalHealthPOA-accept_signature"));
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

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")), intro11, new BasicDivider(), principalLBL,
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
        Html intro12 = new Html(getTranslation("MentalHealthPOA-intro12"));
        Html intro13 = new Html(getTranslation("MentalHealthPOA-intro13"));
        Html witnessNotarySignatureLBL = new Html(getTranslation("MentalHealthPOA-witness_notary_signature_LBL"));

        patientUnableSignatureNameField = new TextField(getTranslation("MentalHealthPOA-name_printed"));

        patientUnableSignature = new SignaturePad();
        patientUnableSignature.setHeight("100px");
        patientUnableSignature.setWidth("400px");
        patientUnableSignature.setPenColor("#2874A6");

        Button clearPatientUnableSig = new Button(getTranslation("MentalHealthPOA-clear_signature"));
        clearPatientUnableSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientUnableSig.addClickListener(event -> {
            patientUnableSignature.clear();
        });
        Button savePatientUnableSig = new Button(getTranslation("MentalHealthPOA-accept_signature"));
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

        patientUnableSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")), intro12, intro13, new BasicDivider(),
                patientUnableSignatureNameField, witnessNotarySignatureLBL, patientUnableSignature, sigLayout);
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
        Html intro14 = new Html(getTranslation("MentalHealthPOA-intro14"));
        Html intro15 = new Html(getTranslation("MentalHealthPOA-intro15"));
        Html witnessSignatureLBL = new Html(getTranslation("MentalHealthPOA-witness_signature_LBL"));
        Html nextSteps = new Html(getTranslation("MentalHealthPOA-next_steps"));

        witnessName = new TextField(getTranslation("MentalHealthPOA-witness_name"));
        witnessAddress = new TextField(getTranslation("MentalHealthPOA-address"));

        witnessSignature = new SignaturePad();
        witnessSignature.setHeight("100px");
        witnessSignature.setWidth("400px");
        witnessSignature.setPenColor("#2874A6");

        Button clearWitnessSig = new Button(getTranslation("MentalHealthPOA-clear_signature"));
        clearWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearWitnessSig.addClickListener(event -> {
            witnessSignature.clear();
        });
        Button saveWitnessSig = new Button(getTranslation("MentalHealthPOA-accept_signature"));
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

        witnessSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("MentalHealthPOA-mental_health_care_power_of_attorney")), intro14, intro15, new BasicDivider(),
                witnessName, witnessAddress, witnessSignatureLBL, witnessSignature, sigLayout, nextSteps);
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
        returnButton = new Button(getTranslation("MentalHealthPOA-back"), new Icon(VaadinIcon.BACKWARDS));
        returnButton.setEnabled(false);
        returnButton.addClickListener(event -> {
            questionPosition--;
            evalNavigation();
        });
        forwardButton = new Button(getTranslation("MentalHealthPOA-next"), new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
            questionPosition++;
            evalNavigation();
        });
        viewStateForm = new Button(getTranslation("MentalHealthPOA-view_your_state_mental_health_care_poa_instructions"));
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
        Span content = new Span(getTranslation("MentalHealthPOA-span_content"));

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
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 1:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(true);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 2:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(true);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 3:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(true);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 4:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(true);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 5:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(true);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 6:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(true);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 7:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(true);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 8:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(true);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 9:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(true);
                witnessSignatureLayout.setVisible(false);
                break;
            case 10:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(false);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                revocationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
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
        StreamResource streamResource = pdfHandler.retrievePDFForm("POAMentalHealth");

        Dialog infoDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button(getTranslation("MentalHealthPOA-close"), e -> infoDialog.close());
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


        Button closeButton = new Button(getTranslation("MentalHealthPOA-cancel"), e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button(getTranslation("MentalHealthPOA-accept_and_submit"));
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            consentState = Consent.ConsentState.ACTIVE;
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

        Button acceptAndPrintButton = new Button(getTranslation("MentalHealthPOA-accept_and_get_notarized"));
        acceptAndPrintButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptAndPrintButton.addClickListener(event -> {
            consentState = Consent.ConsentState.PROPOSED;
            docDialog.close();
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
        poa = new PowerOfAttorneyMentalHealth();
        //Set principle
        Principle principle = new Principle();
        principle.setAddress1(patientAddress1Field.getValue());
        principle.setAddress2(patientAddress2Field.getValue());
        principle.setDateOfBirth(patientDateOfBirthField.getValue());
        principle.setEmailAddress(patientEmailAddressField.getValue());
        principle.setName(patientFullNameField.getValue());
        principle.setPhoneNumber(patientPhoneNumberField.getValue());
        poa.setPrinciple(principle);

        Agent agent = new Agent();
        agent.setAddress1(poaAddress1Field.getValue());
        agent.setAddress2(poaAddress1Field.getValue());
        agent.setCellPhone(poaCellPhoneField.getValue());
        agent.setHomePhone(poaHomePhoneField.getValue());
        agent.setName(poaFullNameField.getValue());
        agent.setWorkPhone(poaWorkPhoneField.getValue());
        poa.setAgent(agent);

        Alternate alternate = new Alternate();
        alternate.setAddress1(altAddress1Field.getValue());
        alternate.setAddress2(altAddress2Field.getValue());
        alternate.setCellPhone(altCellPhoneField.getValue());
        alternate.setHomePhone(altHomePhoneField.getValue());
        alternate.setName(altFullNameField.getValue());
        alternate.setWorkPhone(altWorkPhoneField.getValue());
        poa.setAlternate(alternate);

        String authDecision1 = (String)authorizedDecisions1.getValue();
        String authDecision2 = (String)authorizedDecisions2.getValue();
        String authDecision3 = (String)authorizedDecisions3.getValue();
        String authDecision4 = (String)authorizedDecisions4.getValue();
        if (authDecision1 != null) {
            if (authDecision1.contains(getTranslation("MentalHealthPOA-to_receive_medical_records"))) {
                poa.setAuthorizeReleaseOfRecords(true);
            }
        }
        if (authDecision2 != null) {
            if (authDecision2.contains(getTranslation("MentalHealthPOA-administration_of_any_medications"))) {
                poa.setAuthorizeMedicationAdminstration(true);
            }
        }
        if (authDecision3 != null) {
            if (authDecision3.contains(getTranslation("MentalHealthPOA-hospitalization_program"))) {
                poa.setAuthorizeCommitIfNecessary(true);
            }
        }
        if (authDecision4 != null) {
            if (authDecision4 != null) {
                if (authDecision4.contains(getTranslation("MentalHealthPOA-other"))) {
                    poa.setAuthorizeOtherMentalHealthActions(true);
                    poa.setMentalHealthActionsList1(authOtherDecisionsField1.getValue());
                    poa.setMentalHealthActionsList2(authOtherDecisionsField2.getValue());
                    poa.setMentalHealthActionsList3(authOtherDecisionsField3.getValue());
                }
            }
        }

        poa.setDoNotAuthorizeActionList1(authException1Field.getValue());
        poa.setDoNotAuthorizeActionList2(authException2Field.getValue());

        //Hipaa waiver
        HipaaWaiver hipaa = new HipaaWaiver();
        String hipaaValue = (String)hipaaButton.getValue();
        if (hipaaValue != null) {
            hipaa.setUseDisclosure(hipaaValue.contains(getTranslation("MentalHealthPOA-i_intend")));
            poa.setHipaaWaiver(hipaa);
        }

        PrincipleSignature principleSignature = new PrincipleSignature();
        principleSignature.setBase64EncodeSignature(base64PatientSignature);
        principleSignature.setDateSigned(getDateString(new Date()));
        poa.setPrincipleSignature(principleSignature);

        PrincipleAlternateSignature principleAlternateSignature = new PrincipleAlternateSignature();
        principleAlternateSignature.setBase64EncodedSignature(base64PatientUnableSignature);
        principleAlternateSignature.setNameOfWitnessOrNotary(patientUnableSignatureNameField.getValue());
        principleAlternateSignature.setDateSigned(getDateString(new Date()));
        poa.setPrincipleAlternateSignature(principleAlternateSignature);

        WitnessSignature witnessSignature = new WitnessSignature();
        witnessSignature.setBase64EncodedSignature(base64WitnessSignature);
        witnessSignature.setDateSigned(getDateString(new Date()));
        witnessSignature.setWitnessAddress(witnessAddress.getValue());
        witnessSignature.setWitnessName(witnessName.getValue());
        poa.setWitnessSignature(witnessSignature);

        PDFPOAMentalHealthHandler pdfHandler = new PDFPOAMentalHealthHandler(pdfSigningService);
        StreamResource res = pdfHandler.retrievePDFForm(poa, base64PatientInitials);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;
    }
    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent poaDirective = new Consent();
        poaDirective.setId("POAMentalHealth-"+consentSession.getFhirPatientId());
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
        attachment.setCreation(new Date());
        attachment.setTitle("POAMentalHealth");


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

        poaDirective.setProvision(provision);

        Extension extension = createPowerOfAttorneyMentalHealthQuestionnaireResponse();
        poaDirective.getExtension().add(extension);

        fhirConsentClient.createConsent(poaDirective);
    }

    private Extension createPowerOfAttorneyMentalHealthQuestionnaireResponse() {
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.com/leap/adr/poamentalhealth");
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/leap-poamentalhealth-"+consentSession.getFhirPatientId()));
        return extension;
    }

    private void resetFormAndNavigation() {
        patientInitials.clear();
        poaFullNameField.clear();
        poaAddress2Field.clear();
        poaAddress1Field.clear();
        poaHomePhoneField.clear();
        poaWorkPhoneField.clear();
        poaCellPhoneField.clear();
        altFullNameField.clear();
        altAddress1Field.clear();
        altAddress2Field.clear();
        altHomePhoneField.clear();
        altWorkPhoneField.clear();
        altCellPhoneField.clear();
        authException1Field.clear();
        authException2Field.clear();
        authorizedDecisions1.clear();
        authorizedDecisions2.clear();
        authorizedDecisions3.clear();
        authorizedDecisions4.clear();
        authOtherDecisionsField2.clear();
        authOtherDecisionsField3.clear();
        authOtherDecisionsField1.clear();
        hipaaButton.clear();
        patientSignature.clear();
        patientUnableSignature.clear();
        patientUnableSignatureNameField.clear();
        witnessSignature.clear();
        witnessAddress.clear();
        witnessName.clear();

        questionPosition = 0;
    }

    private void createQuestionnaireResponse() {
        BooleanType booleanTypeTrue = new BooleanType(true);
        BooleanType booleanTypeFalse = new BooleanType(false);
        BooleanType answerBoolean = new BooleanType();
        questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId("leap-poamentalhealth-" + consentSession.getFhirPatientId());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/"+consentSession.getFhirPatientId());
        questionnaireResponse.setAuthor(refpatient);
        questionnaireResponse.setAuthored(new Date());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        questionnaireResponse.setSubject(refpatient);
        questionnaireResponse.setQuestionnaire("Questionnaire/leap-poamentalhealth");

        powerOfAttorneyResponse();
        alternatePowerOfAttorneyResponse();
        powerOfAttorneyAuthorizationResponse();
        hipaaResponse();
        signatureRequirementsResponse();

        questionnaireResponse.setItem(responseList);
        fhirQuestionnaireResponse.createQuestionnaireResponse(questionnaireResponse);

    }

    private void powerOfAttorneyResponse() {
        //poa name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_1 = createItemStringType("1.1.1", getTranslation("MentalHealthPOA-questionnaire_response_item1_1_1"), poa.getAgent().getName());
        responseList.add(item1_1_1);
        //poa address
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_2 = createItemStringType("1.1.2", getTranslation("MentalHealthPOA-questionnaire_response_item1_1_2"), poa.getAgent().getAddress1()+" "+poa.getAgent().getAddress2());
        responseList.add(item1_1_2);
        //poa home phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_3 = createItemStringType("1.1.3", getTranslation("MentalHealthPOA-questionnaire_response_item1_1_3"), poa.getAgent().getHomePhone());
        responseList.add(item1_1_3);
        //poa work phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_4 = createItemStringType("1.1.4", getTranslation("MentalHealthPOA-questionnaire_response_item1_1_4"), poa.getAgent().getWorkPhone());
        responseList.add(item1_1_4);
        //poa cell phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_5 = createItemStringType("1.1.5", getTranslation("MentalHealthPOA-questionnaire_response_item1_1_5"), poa.getAgent().getCellPhone());
        responseList.add(item1_1_5);
    }

    private void alternatePowerOfAttorneyResponse() {
        //alternate name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_1 = createItemStringType("1.2.1", getTranslation("MentalHealthPOA-questionnaire_response_item1_2_1"), poa.getAlternate().getName());
        responseList.add(item1_2_1);
        //alternate address
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_2 = createItemStringType("1.2.2", getTranslation("MentalHealthPOA-questionnaire_response_item1_2_2"), poa.getAlternate().getAddress1()+" "+poa.getAlternate().getAddress2());
        responseList.add(item1_2_2);
        //alternate home phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_3 = createItemStringType("1.2.3", getTranslation("MentalHealthPOA-questionnaire_response_item1_2_3"), poa.getAlternate().getHomePhone());
        responseList.add(item1_2_3);
        //alternate work phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_4 = createItemStringType("1.2.4", getTranslation("MentalHealthPOA-questionnaire_response_item1_2_4"), poa.getAlternate().getWorkPhone());
        responseList.add(item1_2_4);
        //alternate cell phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_5 = createItemStringType("1.2.5", getTranslation("MentalHealthPOA-questionnaire_response_item1_2_5"), poa.getAlternate().getCellPhone());
        responseList.add(item1_2_5);
    }

    private void powerOfAttorneyAuthorizationResponse() {
        //Mental Health Authorizations
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemBooleanType("2.1", getTranslation("MentalHealthPOA-questionnaire_response_item2_1"), poa.isAuthorizeReleaseOfRecords());
        responseList.add(item2_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2", getTranslation("MentalHealthPOA-questionnaire_response_item2_2"), poa.isAuthorizeMedicationAdminstration());
        responseList.add(item2_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_3 = createItemBooleanType("2.3", getTranslation("MentalHealthPOA-questionnaire_response_item2_3"), poa.isAuthorizeCommitIfNecessary());
        responseList.add(item2_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_4 = createItemBooleanType("2.4", getTranslation("MentalHealthPOA-questionnaire_response_item2_4"), poa.isAuthorizeOtherMentalHealthActions());
        responseList.add(item2_4);
        if (poa.isAuthorizeOtherMentalHealthActions()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item2_4_1 = createItemStringType("2.4.1", getTranslation("MentalHealthPOA-questionnaire_response_item2_4_1"), poa.getMentalHealthActionsList1()+" "+poa.getMentalHealthActionsList2()+" "+poa.getMentalHealthActionsList3());
            responseList.add(item2_4_1);
        }
        //NOT AUTHORIZED
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3 = createItemStringType("3", getTranslation("MentalHealthPOA-questionnaire_response_item3"), poa.getDoNotAuthorizeActionList1()+" "+poa.getDoNotAuthorizeActionList2());
        responseList.add(item3);
    }

    private void hipaaResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4 = createItemBooleanType("4", getTranslation("MentalHealthPOA-questionnaire_response_item4"), poa.getHipaaWaiver().isUseDisclosure());
        responseList.add(item4);
    }

    private void signatureRequirementsResponse() {
        boolean patientSignature = false;
        if (poa.getPrincipleSignature().getBase64EncodeSignature() != null && poa.getPrincipleSignature().getBase64EncodeSignature().length > 0) patientSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5 = createItemBooleanType("5", getTranslation("MentalHealthPOA-questionnaire_response_item5"), patientSignature);
        responseList.add(item5);


        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_1 = createItemStringType("6.1", getTranslation("MentalHealthPOA-questionnaire_response_item6_1"), poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary());
        responseList.add(item6_1);

        boolean patientUnableToSign = false;
        if (poa.getPrincipleAlternateSignature().getBase64EncodedSignature() != null && poa.getPrincipleAlternateSignature().getBase64EncodedSignature().length > 0) patientUnableToSign = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_2 = createItemBooleanType("6.2", getTranslation("MentalHealthPOA-questionnaire_response_item6_2"), patientUnableToSign);
        responseList.add(item6_2);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_1 = createItemStringType("7.1", getTranslation("MentalHealthPOA-questionnaire_response_item7_1"), poa.getWitnessSignature().getWitnessName());
        responseList.add(item7_1);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_2 = createItemStringType("7.2", getTranslation("MentalHealthPOA-questionnaire_response_item7_2"), poa.getWitnessSignature().getWitnessAddress());
        responseList.add(item7_2);

        boolean witnessSignature = false;
        if (poa.getWitnessSignature().getBase64EncodedSignature() != null && poa.getWitnessSignature().getBase64EncodedSignature().length > 0) witnessSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7_3 = createItemBooleanType("7.3", getTranslation("MentalHealthPOA-questionnaire_response_item7_3"), witnessSignature);
        responseList.add(item7_3);
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
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-critical_error"), 0));
        }
    }

    private void errorCheckCommon() {
        //user initials
        try {
            if (base64PatientInitials == null || base64PatientInitials.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-user_initial_can_not_be_blank"), 0));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-user_initial_can_not_be_blank"), 0));
        }

        //agent
        if (poa.getAgent().getName() == null || poa.getAgent().getName().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-agent_name_can_not_be_blank"), 2));
        }
        if (poa.getAgent().getAddress1() == null || poa.getAgent().getAddress1().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-agent_address_can_not_be_blank"), 2));
        }
        if ((poa.getAgent().getHomePhone() == null || poa.getAgent().getHomePhone().isEmpty()) &&
                (poa.getAgent().getWorkPhone() == null || poa.getAgent().getWorkPhone().isEmpty()) &&
                (poa.getAgent().getCellPhone() == null || poa.getAgent().getCellPhone().isEmpty())) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-a_minimum_of_1_agent_phone_number_should_be_provided"), 2));
        }

        //alternate
        if (poa.getAlternate().getName() == null || poa.getAlternate().getName().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-alternate_name_can_not_be_blank"), 3));
        }
        if (poa.getAlternate().getAddress1() == null || poa.getAlternate().getAddress1().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-alternate_address_can_not_be_blank"), 3));
        }
        if ((poa.getAlternate().getHomePhone() == null || poa.getAlternate().getHomePhone().isEmpty()) &&
                (poa.getAlternate().getWorkPhone() == null || poa.getAlternate().getWorkPhone().isEmpty()) &&
                (poa.getAlternate().getCellPhone() == null || poa.getAlternate().getCellPhone().isEmpty())) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-a_minimum_of_1_alternate_phone_number_should_be_provided"), 3));
        }

        //authorized actions
        if (!poa.isAuthorizeOtherMentalHealthActions() && !poa.isAuthorizeCommitIfNecessary() && !poa.isAuthorizeMedicationAdminstration() && !poa.isAuthorizeReleaseOfRecords()) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-no_authorization_selection_were_made"), 4));
        }

    }

    private void errorCheckSignature() {
        try {
            if (base64PatientSignature == null || base64PatientSignature.length == 0) {

                if (base64PatientUnableSignature == null || base64PatientUnableSignature.length == 0) {
                    errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-user_signature_or_alternate_signature_required"), 8));
                }
                else {
                    try {
                        if (poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary() == null || poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary().isEmpty()) {
                            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_or_notary_as_alternate_name_required"), 9));
                        }
                    }
                    catch (Exception ex) {
                        errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_or_notary_as_alternate_name_required"), 9));
                    }
                }
            }
        }
        catch(Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-user_signature_or_alternate_signature_required"), 8));
        }

        try {
            if (base64WitnessSignature == null || base64WitnessSignature.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_signature_can_not_be_blank"), 10));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_signature_can_not_be_blank"), 10));
        }
        try {
            if (poa.getWitnessSignature().getWitnessName() == null || poa.getWitnessSignature().getWitnessName().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_name_can_not_be_blank"), 10));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_name_can_not_be_blank"), 10));
        }
        try {
            if (poa.getWitnessSignature().getWitnessAddress() == null || poa.getWitnessSignature().getWitnessAddress().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_address_can_not_be_blank"), 10));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("MentalHealthPOA-witness_address_can_not_be_blank"), 10));
        }
    }

    private void createErrorDialog() {
        Html errorIntro = new Html(getTranslation("MentalHealthPOA-eror_intro"));
        Html flowTypeIntro;
        if (advDirectiveFlowType.equals("Default")) {
            flowTypeIntro = new Html(getTranslation("MentalHealthPOA-flow_type_intro_1"));
        }
        else {
            flowTypeIntro = new Html(getTranslation("MentalHealthPOA-flow_type_intro_2"));
        }

        Button errorBTN = new Button(getTranslation("MentalHealthPOA-correct_errors"));
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
        errorDialog.add(createHeader(VaadinIcon.WARNING, getTranslation("MentalHealthPOA-failed_verification")),errorIntro, flowTypeIntro, verticalLayout, errorBTN);
    }
}
