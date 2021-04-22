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
import gov.hhs.onc.leap.ui.util.pdf.PDFPOAHealthcareHandler;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@PageTitle("Health Care Power Of Attorney")
@Route(value = "healthcarepowerofattorney", layout = MainLayout.class)
public class HealthcarePowerOfAttorney extends ViewFrame {

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

    private TextField authException1Field;
    private TextField authException2Field;
    private TextField authException3Field;
    private FlexBoxLayout authExceptionLayout;

    private RadioButtonGroup autopsyButtonGroup;
    private FlexBoxLayout autopsySelectionLayout;

    private RadioButtonGroup organDonationButtonGroup;
    private TextField institutionAgreementField;
    private RadioButtonGroup whatTissuesButtonGroup;
    private TextField specificOrgansField;
    private RadioButtonGroup pouOrganDonationButtonGroup;
    private TextField otherPurposesField;
    private RadioButtonGroup organizationOrganDonationButtonGroup;
    private TextField patientChoiceOfOrganizations;
    private FlexBoxLayout organDonationSelectionLayout;

    private RadioButtonGroup burialSelectionButtonGroup;
    private TextField buriedInField;
    private TextField ashesDispositionField;
    private FlexBoxLayout burialSelectionLayout;

    private FlexBoxLayout otherAttachmentsLayout;

    private TextField attestationDRName;
    private TextField attestationPatientName;
    private TextField attestationDate;
    private SignaturePad physcianSignature;
    private byte[] base64PhysicianSignature;
    private FlexBoxLayout attestationLayout;

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

    private PowerOfAttorneyHealthCare poa;

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
        setId("healthcarepowerofattorney");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        this.responseList = new ArrayList<>();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html(getTranslation("HCPOA-intro"));

        createPatientsInitials();
        createPatientGeneralInfo();
        createPOASelection();
        createALTSelection();
        createAuthorizationSelection();
        createAuthExceptionSelection();
        createAutopsySelection();
        createOrganDonationSelection();
        createBurialSelection();
        createOtherDirectivesSection();
        createAttestation();
        createHipaa();
        createPatientSignature();
        createPatientUnableSignature();
        createWitnessSignature();

        createInfoDialog();

        FlexBoxLayout content = new FlexBoxLayout(intro, patientInitialsLayout, patientGeneralInfoLayout, poaSelectionLayout,
                altSelectionLayout, authorizationLayout, authExceptionLayout, autopsySelectionLayout, organDonationSelectionLayout, burialSelectionLayout,
                otherAttachmentsLayout, attestationLayout, hipaaLayout, patientSignatureLayout, patientUnableSignatureLayout, witnessSignatureLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createPatientsInitials() {
        Html intro2 = new Html(getTranslation("HCPOA-intro2"));

        patientInitials = new SignaturePad();
        patientInitials.setHeight("100px");
        patientInitials.setWidth("150px");
        patientInitials.setPenColor("#2874A6");

        Button clearPatientInitials = new Button(getTranslation("HCPOA-clear_initials"));
        clearPatientInitials.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientInitials.addClickListener(event -> {
            patientInitials.clear();
        });
        Button savePatientInitials = new Button(getTranslation("HCPOA-accept_initials"));
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

        patientInitialsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro2, new BasicDivider(), patientInitials, sigLayout);
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
        Html intro3 = new Html(getTranslation("HCPOA-intro3"));

        patientFullNameField = new TextField(getTranslation("HCPOA-name"));
        patientAddress1Field = new TextField(getTranslation("HCPOA-address"));
        patientAddress2Field = new TextField("");
        patientDateOfBirthField = new TextField(getTranslation("HCPOA-dob"));
        patientPhoneNumberField = new TextField(getTranslation("HCPOA-phone"));
        patientEmailAddressField = new TextField(getTranslation("HCPOA-email"));

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

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro3, new BasicDivider(),
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
        Html intro4 = new Html(getTranslation("HCPOA-intro4"));

        poaFullNameField = new TextField(getTranslation("HCPOA-name"));
        poaAddress1Field = new TextField(getTranslation("HCPOA-address"));
        poaAddress2Field = new TextField("");
        poaHomePhoneField = new TextField(getTranslation("HCPOA-home_phone"));
        poaWorkPhoneField = new TextField(getTranslation("HCPOA-work_phone"));
        poaCellPhoneField = new TextField(getTranslation("HCPOA-cell_phone"));

        poaSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro4, new BasicDivider(),
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
        Html intro5 = new Html(getTranslation("HCPOA-intro5"));

        altFullNameField = new TextField(getTranslation("HCPOA-name"));
        altAddress1Field = new TextField(getTranslation("HCPOA-address"));
        altAddress2Field = new TextField("");
        altHomePhoneField = new TextField(getTranslation("HCPOA-home_phone"));
        altWorkPhoneField = new TextField(getTranslation("HCPOA-work_phone"));
        altCellPhoneField = new TextField(getTranslation("HCPOA-cell_phone"));

        altSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro5, new BasicDivider(),
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
        Html intro6 = new Html(getTranslation(getTranslation("HCPOA-intro6")));
        authorizationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro6, new BasicDivider());
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
        Html intro7 = new Html(getTranslation("HCPOA-intro7"));

        authException1Field = new TextField("");
        authException2Field = new TextField("");
        authException3Field = new TextField("");

        authExceptionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro7, new BasicDivider(),
                authException1Field, authException2Field, authException3Field);
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

    private void createAutopsySelection() {
        Html intro8 = new Html(getTranslation("HCPOA-intro8"));

        autopsyButtonGroup = new RadioButtonGroup();
        autopsyButtonGroup.setLabel(getTranslation("HCPOA-autopsy_label"));
        autopsyButtonGroup.setItems(getTranslation("HCPOA-autopsy_item1"),
                getTranslation("HCPOA-autopsy_item2"), getTranslation("HCPOA-autopsy_item3"));
        autopsyButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        autopsySelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro8, new BasicDivider(),
                autopsyButtonGroup);
        autopsySelectionLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        autopsySelectionLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        autopsySelectionLayout.setHeightFull();
        autopsySelectionLayout.setBackgroundColor("white");
        autopsySelectionLayout.setShadow(Shadow.S);
        autopsySelectionLayout.setBorderRadius(BorderRadius.S);
        autopsySelectionLayout.getStyle().set("margin-bottom", "10px");
        autopsySelectionLayout.getStyle().set("margin-right", "10px");
        autopsySelectionLayout.getStyle().set("margin-left", "10px");
        autopsySelectionLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        autopsySelectionLayout.setVisible(false);
    }

    private void createOrganDonationSelection() {
        Html intro9 = new Html(getTranslation("HCPOA-intro9"));
        organDonationButtonGroup = new RadioButtonGroup();
        organDonationButtonGroup.setLabel(getTranslation("HCPOA-organ_donation_label"));
        organDonationButtonGroup.setItems(getTranslation("HCPOA-organ_donation_item1"),
                getTranslation("HCPOA-organ_donation_item2"),
                getTranslation("HCPOA-organ_donation_item3"));
        organDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        organDonationButtonGroup.addValueChangeListener(event -> {
            String v = (String)event.getValue();
            if (v == null) {
                institutionAgreementField.setVisible(false);
                whatTissuesButtonGroup.setVisible(false);
                specificOrgansField.setVisible(false);
                pouOrganDonationButtonGroup.setVisible(false);
                otherPurposesField.setVisible(false);
                organizationOrganDonationButtonGroup.setVisible(false);
                patientChoiceOfOrganizations.setVisible(false);
            }
            else if (v.contains(getTranslation("HCPOA-i_have_already_signed"))) {
                institutionAgreementField.setVisible(true);
                whatTissuesButtonGroup.setVisible(false);
                specificOrgansField.setVisible(false);
                pouOrganDonationButtonGroup.setVisible(false);
                otherPurposesField.setVisible(false);
                organizationOrganDonationButtonGroup.setVisible(false);
                patientChoiceOfOrganizations.setVisible(false);
            }
            else if (v.contains(getTranslation("HCPOA-i_do_want"))) {
                institutionAgreementField.setVisible(false);
                whatTissuesButtonGroup.setVisible(true);
                //specificOrgansField.setVisible(true);
                pouOrganDonationButtonGroup.setVisible(true);
                //otherPurposesField.setVisible(true);
                organizationOrganDonationButtonGroup.setVisible(true);
                //patientChoiceOfOrganizations.setVisible(true);
            }
            else {
                institutionAgreementField.setVisible(false);
                whatTissuesButtonGroup.setVisible(false);
                specificOrgansField.setVisible(false);
                pouOrganDonationButtonGroup.setVisible(false);
                otherPurposesField.setVisible(false);
                organizationOrganDonationButtonGroup.setVisible(false);
                patientChoiceOfOrganizations.setVisible(false);
            }
        });

        institutionAgreementField = new TextField(getTranslation("HCPOA-institution"));
        institutionAgreementField.setVisible(false);

        whatTissuesButtonGroup = new RadioButtonGroup();
        whatTissuesButtonGroup.setLabel(getTranslation("HCPOA-what_tissues_label"));
        whatTissuesButtonGroup.setItems(getTranslation("HCPOA-what_tissues_item1"), getTranslation("HCPOA-what_tissues_item2"), getTranslation("HCPOA-what_tissues_item3"));
        whatTissuesButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        whatTissuesButtonGroup.addValueChangeListener(event -> {
           String v = (String)event.getValue();
           if (v.contains(getTranslation("HCPOA-specific_parts"))) {
               specificOrgansField.setVisible(true);
           }
           else {
               specificOrgansField.setVisible(false);
           }
        });
        whatTissuesButtonGroup.setVisible(false);

        specificOrgansField = new TextField(getTranslation("HCPOA-specific_parts_or_organs_only"));
        specificOrgansField.setVisible(false);

        pouOrganDonationButtonGroup = new RadioButtonGroup();
        pouOrganDonationButtonGroup.setLabel(getTranslation("HCPOA-pou_organ_donation_label"));
        pouOrganDonationButtonGroup.setItems(getTranslation("HCPOA-pou_organ_donation_item1"), getTranslation("HCPOA-pou_organ_donation_item2"), getTranslation("HCPOA-pou_organ_donation_item3"), getTranslation("HCPOA-pou_organ_donation_item4"));
        pouOrganDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        pouOrganDonationButtonGroup.addValueChangeListener(event -> {
            String v = (String)event.getValue();
            if (v.equals(getTranslation("HCPOA-pou_organ_donation_item4"))) {
                otherPurposesField.setVisible(true);
            }
            else {
                otherPurposesField.setVisible(false);
            }
        });
        pouOrganDonationButtonGroup.setVisible(false);

        otherPurposesField = new TextField(getTranslation("HCPOA-other_purposes"));
        otherPurposesField.setVisible(false);

        organizationOrganDonationButtonGroup = new RadioButtonGroup();
        organizationOrganDonationButtonGroup.setLabel(getTranslation("HCPOA-organization_organ_donation_label"));
        organizationOrganDonationButtonGroup.setItems(getTranslation("HCPOA-organization_organ_donation_item1"), getTranslation("HCPOA-organization_organ_donation_item2"));
        organizationOrganDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        organizationOrganDonationButtonGroup.addValueChangeListener(event -> {
            String v = (String)event.getValue();
            if (v.equals(getTranslation("HCPOA-my_list"))) {
                patientChoiceOfOrganizations.setVisible(true);
            }
            else {
                patientChoiceOfOrganizations.setVisible(false);
            }
        });
        organizationOrganDonationButtonGroup.setVisible(false);

        patientChoiceOfOrganizations = new TextField(getTranslation("HCPOA-list_organizations"));
        patientChoiceOfOrganizations.setVisible(false);


        organDonationSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")),intro9, new BasicDivider(),
                organDonationButtonGroup, institutionAgreementField, whatTissuesButtonGroup, specificOrgansField, pouOrganDonationButtonGroup, otherPurposesField,
                organizationOrganDonationButtonGroup, patientChoiceOfOrganizations);
        organDonationSelectionLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        organDonationSelectionLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        organDonationSelectionLayout.setHeightFull();
        organDonationSelectionLayout.setBackgroundColor("white");
        organDonationSelectionLayout.setShadow(Shadow.S);
        organDonationSelectionLayout.setBorderRadius(BorderRadius.S);
        organDonationSelectionLayout.getStyle().set("margin-bottom", "10px");
        organDonationSelectionLayout.getStyle().set("margin-right", "10px");
        organDonationSelectionLayout.getStyle().set("margin-left", "10px");
        organDonationSelectionLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        organDonationSelectionLayout.setVisible(false);
    }

    private void createBurialSelection() {
        Html intro10 = new Html(getTranslation("HCPOA-intro10"));

        burialSelectionButtonGroup = new RadioButtonGroup();
        burialSelectionButtonGroup.setItems(getTranslation("HCPOA-burial_selection_item1"),
                getTranslation("HCPOA-burial_selection_item2"),
                getTranslation("HCPOA-burial_selection_item3"),
                getTranslation("HCPOA-burial_selection_item4"),
                getTranslation("HCPOA-burial_selection_item5"));
        burialSelectionButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        burialSelectionButtonGroup.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String v = (String) event.getValue();
                if (v.contains(getTranslation("HCPOA-i_direct_my_body_to_be_buried_in"))) {
                    buriedInField.setVisible(true);
                    ashesDispositionField.setVisible(false);
                } else if (v.contains(getTranslation("HCPOA-cremated_with_my_ashes_to_be"))) {
                    buriedInField.setVisible(false);
                    ashesDispositionField.setVisible(true);
                } else {
                    buriedInField.setVisible(false);
                    ashesDispositionField.setVisible(false);
                }
            }
        });

        buriedInField = new TextField(getTranslation("HCPOA-i_direct_my_body_to_be_buried_in"));
        buriedInField.setVisible(false);

        ashesDispositionField = new TextField(getTranslation("HCPOA-i_direct_the_following_to_be_done_with_my_ashes"));
        ashesDispositionField.setVisible(false);

        burialSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), intro10, new BasicDivider(),
                burialSelectionButtonGroup, buriedInField, ashesDispositionField);
        burialSelectionLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        burialSelectionLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        burialSelectionLayout.setHeightFull();
        burialSelectionLayout.setBackgroundColor("white");
        burialSelectionLayout.setShadow(Shadow.S);
        burialSelectionLayout.setBorderRadius(BorderRadius.S);
        burialSelectionLayout.getStyle().set("margin-bottom", "10px");
        burialSelectionLayout.getStyle().set("margin-right", "10px");
        burialSelectionLayout.getStyle().set("margin-left", "10px");
        burialSelectionLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        burialSelectionLayout.setVisible(false);
    }

    private void createOtherDirectivesSection() {
        Html livingWill1 = new Html(getTranslation("HCPOA-living_will1"));
        Html livingWill2 = new Html(getTranslation("HCPOA-living_will2"));
        RadioButtonGroup rbgLivingWill = new RadioButtonGroup();
        rbgLivingWill.setItems(getTranslation("HCPOA-living_will_item1"),
                getTranslation("HCPOA-living_will_item2"));
        rbgLivingWill.setEnabled(false);

        Html polst1 = new Html(getTranslation("HCPOA-polst1"));
        Html polst2 = new Html(getTranslation("HCPOA-polst2"));
        RadioButtonGroup rbgPolst = new RadioButtonGroup();
        rbgPolst.setItems(getTranslation("HCPOA-polst_item1"),
                getTranslation("HCPOA-polst_item2"));
        rbgPolst.setEnabled(false);

        Html dnr1 = new Html(getTranslation("HCPOA-dnr1"));
        RadioButtonGroup rbgDNR = new RadioButtonGroup();
        rbgDNR.setItems(getTranslation("HCPOA-dnr_item1"),
                getTranslation("HCPOA-dnr_item2"));
        rbgDNR.setEnabled(false);

        Html r4Disclaimer = new Html(getTranslation("HCPOA-r4Disclaimer"));

        otherAttachmentsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), livingWill1, livingWill2, rbgLivingWill,
                polst1, polst2, rbgPolst, dnr1, rbgDNR, new BasicDivider(), r4Disclaimer);
        otherAttachmentsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        otherAttachmentsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        otherAttachmentsLayout.setHeightFull();
        otherAttachmentsLayout.setBackgroundColor("white");
        otherAttachmentsLayout.setShadow(Shadow.S);
        otherAttachmentsLayout.setBorderRadius(BorderRadius.S);
        otherAttachmentsLayout.getStyle().set("margin-bottom", "10px");
        otherAttachmentsLayout.getStyle().set("margin-right", "10px");
        otherAttachmentsLayout.getStyle().set("margin-left", "10px");
        otherAttachmentsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        otherAttachmentsLayout.setVisible(false);

    }

    private void createAttestation() {
        Html intro11 = new Html(getTranslation("HCPOA-intro11"));
        Html intro12 = new Html(getTranslation("HCPOA-intro12"));

        Html para1 = new Html(getTranslation("HCPOA-i_dr"));
        attestationDRName = new TextField(getTranslation("HCPOA-physician_name"));
        Html para2 = new Html(getTranslation("HCPOA-have_reviewed_this_doc"));
        attestationPatientName = new TextField(getTranslation("HCPOA-patients_name"));
        attestationPatientName.setValue(consentUser.getFirstName()+" "+consentUser.getMiddleName()+" "+consentUser.getLastName());
        Html para3 = new Html(getTranslation("HCPOA-any_questions_the_probable_medical_consequences"));
        attestationDate = new TextField(getTranslation("HCPOA-date"));
        attestationDate.setValue(getDateString(new Date()));
        Html para4 = new Html(getTranslation("HCPOA-i_have_agreed_to_comply_with_the_provisions"));

        physcianSignature = new SignaturePad();
        physcianSignature.setHeight("100px");
        physcianSignature.setWidth("400px");
        physcianSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button(getTranslation("HCPOA-clear_signature"));
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            physcianSignature.clear();
        });
        Button savePatientSig = new Button(getTranslation("HCPOA-accept_signature"));
        savePatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientSig.addClickListener(event -> {
            base64PhysicianSignature = physcianSignature.getImageBase64();
            questionPosition++;
            evalNavigation();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(clearPatientSig, savePatientSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        attestationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), intro11, intro12, new BasicDivider(),
                para1, attestationDRName, para2, attestationPatientName, para3, attestationDate, para4, physcianSignature, sigLayout);
        attestationLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        attestationLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        attestationLayout.setHeightFull();
        attestationLayout.setBackgroundColor("white");
        attestationLayout.setShadow(Shadow.S);
        attestationLayout.setBorderRadius(BorderRadius.S);
        attestationLayout.getStyle().set("margin-bottom", "10px");
        attestationLayout.getStyle().set("margin-right", "10px");
        attestationLayout.getStyle().set("margin-left", "10px");
        attestationLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        attestationLayout.setVisible(false);

    }

    private void createHipaa() {
        Html intro13 = new Html(getTranslation("HCPOA-intro13"));

        hipaaButton = new RadioButtonGroup();
        hipaaButton.setItems(getTranslation("HCPOA-hipaa_item1"));
        hipaaButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        hipaaLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), intro13, new BasicDivider(),
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
        Html intro14 = new Html(getTranslation("HCPOA-intro14"));
        Html revocationLbl = new Html(getTranslation("HCPOA-revocation_label"));
        patientSignature = new SignaturePad();
        patientSignature.setHeight("100px");
        patientSignature.setWidth("400px");
        patientSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button(getTranslation("HCPOA-clear_signature"));
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            patientSignature.clear();
        });
        Button savePatientSig = new Button(getTranslation("HCPOA-accept_signature"));
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

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), revocationLbl, intro14, new BasicDivider(),
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
        Html intro15 = new Html(getTranslation("HCPOA-intro15"));
        Html intro16 = new Html(getTranslation("HCPOA-intro16"));

        patientUnableSignatureNameField = new TextField(getTranslation("HCPOA-name"));

        patientUnableSignature = new SignaturePad();
        patientUnableSignature.setHeight("100px");
        patientUnableSignature.setWidth("400px");
        patientUnableSignature.setPenColor("#2874A6");

        Button clearPatientUnableSig = new Button(getTranslation("HCPOA-clear_signature"));
        clearPatientUnableSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientUnableSig.addClickListener(event -> {
            patientUnableSignature.clear();
        });
        Button savePatientUnableSig = new Button(getTranslation("HCPOA-accept_signature"));
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

        patientUnableSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), intro15, intro16, new BasicDivider(),
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
        Html intro17 = new Html(getTranslation("HCPOA-intro17"));
        Html intro18 = new Html(getTranslation("HCPOA-intro18"));
        Html nextSteps = new Html(getTranslation("HCPOA-next_steps"));

        witnessName = new TextField(getTranslation("HCPOA-witness_name"));
        witnessAddress = new TextField(getTranslation("HCPOA-address"));

        witnessSignature = new SignaturePad();
        witnessSignature.setHeight("100px");
        witnessSignature.setWidth("400px");
        witnessSignature.setPenColor("#2874A6");

        Button clearWitnessSig = new Button(getTranslation("HCPOA-clear_signature"));
        clearWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearWitnessSig.addClickListener(event -> {
            witnessSignature.clear();
        });
        Button saveWitnessSig = new Button(getTranslation("HCPOA-accept_signature"));
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

        witnessSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, getTranslation("HCPOA-health_care_power_of_attorney")), intro17, intro18, new BasicDivider(),
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
        returnButton = new Button(getTranslation("HCPOA-back"), new Icon(VaadinIcon.BACKWARDS));
        returnButton.setEnabled(false);
        returnButton.addClickListener(event -> {
            questionPosition--;
            evalNavigation();
        });
        forwardButton = new Button(getTranslation("HCPOA-next"), new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
            questionPosition++;
            evalNavigation();
        });
        viewStateForm = new Button(getTranslation("HCPOA-view_your_states_health_care_power_of"));
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
        Span content = new Span(getTranslation("HCPOA-fhir_advanced_directive_successfully_created"));

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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(true);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(true);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(true);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
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
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(true);
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 10:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(true);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 11:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(true);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 12:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(true);
                patientUnableSignatureLayout.setVisible(false);
                witnessSignatureLayout.setVisible(false);
                break;
            case 13:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(true);
                witnessSignatureLayout.setVisible(false);
                break;
            case 14:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(false);
                patientInitialsLayout.setVisible(false);
                patientGeneralInfoLayout.setVisible(false);
                poaSelectionLayout.setVisible(false);
                altSelectionLayout.setVisible(false);
                authorizationLayout.setVisible(false);
                authExceptionLayout.setVisible(false);
                autopsySelectionLayout.setVisible(false);
                organDonationSelectionLayout.setVisible(false);
                burialSelectionLayout.setVisible(false);
                otherAttachmentsLayout.setVisible(false);
                attestationLayout.setVisible(false);
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
        StreamResource streamResource = pdfHandler.retrievePDFForm("POAHealthcare");

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


    private void getHumanReadable() {
        StreamResource streamResource = setFieldsCreatePDF();
        docDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");


        Button closeButton = new Button(getTranslation("HCPOA-cancel"), e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button(getTranslation("HCPOA-accept_and_submit"));
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
                successNotification();
                //todo test for fhir consent create success
                resetFormAndNavigation();
                evalNavigation();
            }
        });

        Button acceptAndPrintButton = new Button(getTranslation("HCPOA-accept_and_get_notarized"));
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
        poa = new PowerOfAttorneyHealthCare();
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

        poa.setDoNotAuthorize1(authException1Field.getValue());
        poa.setDoNotAuthorize2(authException2Field.getValue());
        poa.setDoNotAuthorize3(authException3Field.getValue());

        //Autopsy
        String autopsy = (String)autopsyButtonGroup.getValue();
        if (autopsy != null) {
            if (autopsy.contains(getTranslation("HCPOA-i_do_not_consent"))) {
                poa.setDenyAutopsy(true);
            } else if (autopsy.contains(getTranslation("HCPOA-i_do_consent"))) {
                poa.setPermitAutopsy(true);
            } else if (autopsy.contains(getTranslation("HCPOA-my_agent"))) {
                poa.setAgentDecidesAutopsy(true);
            } else {
                //nothing to set
            }
        }

        //Organ Donation
        String organdonation = (String)organDonationButtonGroup.getValue();
        if (organdonation != null) {
            if (organdonation.contains(getTranslation("HCPOA-i_do_not_want"))) {
                poa.setDenyOrganTissueDonation(true);
            } else if (organdonation.contains(getTranslation("HCPOA-i_have_already"))) {
                poa.setHaveExistingOrganTissueCardOrAgreement(true);
                poa.setOrganTissueCardOrAgreementInstitution(institutionAgreementField.getValue());
            } else if (organdonation.contains(getTranslation("HCPOA-i_do_want"))) {
                poa.setPermitOrganTissueDonation(true);
                String whatOrgans = (String) whatTissuesButtonGroup.getValue();
                if (whatOrgans.contains(getTranslation("HCPOA-whole_body"))) {
                    poa.setWholeBodyDonation(true);
                } else if (whatOrgans.contains(getTranslation("HCPOA-any_needed"))) {
                    poa.setAnyPartOrOrganNeeded(true);
                } else if (whatOrgans.contains(getTranslation("HCPOA-these_parts"))) {
                    poa.setSpecificPartsOrOrgans(true);
                    poa.setSpecificPartsOrOrgansList(specificOrgansField.getValue());
                } else {
                    //nothing to select in what tissues
                }
                String forWhatPurpose = (String) pouOrganDonationButtonGroup.getValue();
                if (forWhatPurpose.contains(getTranslation("HCPOA-any_legal"))) {
                    poa.setAnyLegalPurpose(true);
                } else if (forWhatPurpose.contains(getTranslation("HCPOA-transplant"))) {
                    poa.setTransplantOrTherapeutic(true);
                } else if (forWhatPurpose.contains(getTranslation("HCPOA-research"))) {
                    poa.setResearchOnly(true);
                } else if (forWhatPurpose.contains(getTranslation("HCPOA-other"))) {
                    poa.setOtherPurposes(true);
                    poa.setOtherPurposesList(otherPurposesField.getValue());
                } else {
                    //nothing to set
                }
                String whatOrganizations = (String) organizationOrganDonationButtonGroup.getValue();
                if (whatOrganizations.contains(getTranslation("HCPOA-any_that_my_agent_chooses"))) {
                    poa.setAgentDecidedOrganTissueDestination(true);
                } else if (whatOrganizations.contains(getTranslation("HCPOA-my_list"))) {
                    poa.setPrincipleDefined(true);
                    poa.setPrincipleDefinedList(patientChoiceOfOrganizations.getValue());
                } else {
                    //no setting
                }
            } else {
                //nothing to set
            }
        }

        //Burial
        String burial = (String)burialSelectionButtonGroup.getValue();
        if (burial != null) {
            if (burial.contains(getTranslation("HCPOA-burried"))) {
                poa.setBodyToBeBuried(true);
            } else if (burial.contains(getTranslation("HCPOA-burried_in"))) {
                poa.setBodyToBeBuriedIn(true);
                poa.setBodyToBeBuriedInInstructions(buriedInField.getValue());
            } else if (burial.contains(getTranslation("HCPOA-cremated"))) {
                poa.setBodyToBeCremated(true);
            } else if (burial.contains(getTranslation("HCPOA-cremated_with_my_asshes"))) {
                poa.setBodyToBeCrematedAshesDisposition(true);
                poa.setBodyToBeCrematedAshesDispositionInstructions(ashesDispositionField.getValue());
            } else if (burial.contains(getTranslation("HCPOA-my_agent"))) {
                poa.setAgentDecidesBurial(true);
            } else {
                //nothing to set
            }
        }

        //todo write logic to evaluate if the have a DNR, Living Will, or POLST for time being set to false

        poa.setSignedDNR(false);
        poa.setNotSignedDNR(true);
        poa.setSignedLivingWill(false);
        poa.setNotSignedLivingWill(true);
        poa.setSignedPOLST(false);
        poa.setNotSignedPOLST(true);

        //physician affidavit
        PhysicansAffidavit affidavit = new PhysicansAffidavit();
        affidavit.setBase64EncodedSignature(base64PhysicianSignature);
        affidavit.setPhysiciansName(attestationDRName.getValue());
        affidavit.setPrinciplesName(patientFullNameField.getValue());
        affidavit.setSignatureDate(getDateString(new Date()));
        poa.setPhysiciansAffidavit(affidavit);

        //Hipaa waiver
        HipaaWaiver hipaa = new HipaaWaiver();
        String hipaaValue = (String)hipaaButton.getValue();
        if (hipaaValue != null) {
            hipaa.setUseDisclosure(hipaaValue.contains(getTranslation("HCPOA-i_intend")));
        }
        poa.setHipaaWaiver(hipaa);

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

        PDFPOAHealthcareHandler pdfHandler = new PDFPOAHealthcareHandler(pdfSigningService);
        StreamResource res = pdfHandler.retrievePDFForm(poa, base64PatientInitials);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;
    }

    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent poaDirective = new Consent();
        poaDirective.setId("POAHealthcare-"+consentSession.getFhirPatientId());
        poaDirective.setStatus(Consent.ConsentState.ACTIVE);
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
        attachment.setTitle("POAHealthcare");


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

        Extension extension = createHealthcarePowerOfAttorneyQuestionnaireResponse();
        poaDirective.getExtension().add(extension);

        fhirConsentClient.createConsent(poaDirective);
    }

    private Extension createHealthcarePowerOfAttorneyQuestionnaireResponse() {
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.com/leap/adr/poahealthcare");
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/leap-poahealthcare-"+consentSession.getFhirPatientId()));
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
        authException3Field.clear();
        autopsyButtonGroup.clear();
        organDonationButtonGroup.clear();
        whatTissuesButtonGroup.clear();
        specificOrgansField.clear();
        pouOrganDonationButtonGroup.clear();
        otherPurposesField.clear();
        organizationOrganDonationButtonGroup.clear();
        patientChoiceOfOrganizations.clear();
        burialSelectionButtonGroup.clear();
        buriedInField.clear();
        ashesDispositionField.clear();
        attestationDate.clear();
        attestationPatientName.clear();
        attestationDRName.clear();
        physcianSignature.clear();
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
        questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId("leap-poahealthcare-" + consentSession.getFhirPatientId());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/"+consentSession.getFhirPatientId());
        questionnaireResponse.setAuthor(refpatient);
        questionnaireResponse.setAuthored(new Date());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        questionnaireResponse.setSubject(refpatient);
        questionnaireResponse.setQuestionnaire("Questionnaire/leap-poahealthcare");


        powerOfAttorneyResponse();
        alternatePowerOfAttorneyResponse();
        powerOfAttorneyAuthorizationResponse();
        autopsyResponse();
        organTissueDonationResponse();
        bodyDispositionResponse();
        physicianAffidavitResponse();
        hipaaResponse();
        signatureRequirementsResponse();

        questionnaireResponse.setItem(responseList);
        fhirQuestionnaireResponse.createQuestionnaireResponse(questionnaireResponse);
    }

    private void powerOfAttorneyResponse() {
        //poa name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_1 = createItemStringType("1.1.1",
                getTranslation("HCPOA-questionnaire_response_item1_1_1"), poa.getAgent().getName());
        responseList.add(item1_1_1);
        //poa address
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_2 = createItemStringType("1.1.2",
                getTranslation("HCPOA-questionnaire_response_item1_1_2"), poa.getAgent().getAddress1() + " " + poa.getAgent().getAddress2());
        responseList.add(item1_1_2);
        //poa home phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_3 = createItemStringType("1.1.3",
                getTranslation("HCPOA-questionnaire_response_item1_1_3"), poa.getAgent().getHomePhone());
        responseList.add(item1_1_3);
        //poa work phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_4 = createItemStringType("1.1.4",
                getTranslation("HCPOA-questionnaire_response_item1_1_4"), poa.getAgent().getWorkPhone());
        responseList.add(item1_1_4);
        //poa cell phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_5 = createItemStringType("1.1.5",
                getTranslation("HCPOA-questionnaire_response_item1_1_5"), poa.getAgent().getCellPhone());
        responseList.add(item1_1_5);
    }

    private void alternatePowerOfAttorneyResponse() {
        //alternate name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_1 = createItemStringType("1.2.1",
                getTranslation("HCPOA-questionnaire_response_item1_2_1"), poa.getAlternate().getName());
        responseList.add(item1_2_1);
        //alternate address
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_2 = createItemStringType("1.2.2",
                getTranslation("HCPOA-questionnaire_response_item1_2_2"), poa.getAlternate().getAddress1() + " " + poa.getAlternate().getAddress2());
        responseList.add(item1_2_2);
        //alternate home phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_3 = createItemStringType("1.2.3",
                getTranslation("HCPOA-questionnaire_response_item1_2_3"), poa.getAlternate().getHomePhone());
        responseList.add(item1_2_3);
        //alternate work phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_4 = createItemStringType("1.2.4",
                getTranslation("HCPOA-questionnaire_response_item1_2_4"), poa.getAlternate().getWorkPhone());
        responseList.add(item1_2_4);
        //alternate cell phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_5 = createItemStringType("1.2.5",
                getTranslation("HCPOA-questionnaire_response_item1_2_5"), poa.getAlternate().getCellPhone());
        responseList.add(item1_2_5);
    }

    private void powerOfAttorneyAuthorizationResponse() {
        //ADD ACTIONABLE QUESTIONS
        // Authorize poa to make decisions
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3 = createItemBooleanType("1.3",
                getTranslation("HCPOA-questionnaire_response_item1_3"), true);//this form when completed makes this statement true
        responseList.add(item1_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_4 = createItemStringType("1.4",
                getTranslation("HCPOA-questionnaire_response_item1_4"), poa.getDoNotAuthorize1()+" "+poa.getDoNotAuthorize2()+" "+poa.getDoNotAuthorize3());
        responseList.add(item1_4);
    }

    private void autopsyResponse() {
        //Autospy
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemBooleanType("2.1",
                getTranslation("HCPOA-questionnaire_response_item2_1"), poa.isDenyAutopsy());
        responseList.add(item2_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2",
                getTranslation("HCPOA-questionnaire_response_item2_2"), poa.isPermitAutopsy());
        responseList.add(item2_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_3 = createItemBooleanType("2.3",
                getTranslation("HCPOA-questionnaire_response_item2_3"), poa.isAgentDecidesAutopsy());
        responseList.add(item2_3);
    }

    private void organTissueDonationResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_1 = createItemBooleanType("3.1",
                getTranslation("HCPOA-questionnaire_response_item3_1"), poa.isDenyOrganTissueDonation());
        responseList.add(item3_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2 = createItemBooleanType("3.2",
                getTranslation("HCPOA-questionnaire_response_item3_2"), poa.isHaveExistingOrganTissueCardOrAgreement());
        responseList.add(item3_2);
        if (poa.isHaveExistingOrganTissueCardOrAgreement()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2_1 = createItemStringType("3.2.1",
                    getTranslation("HCPOA-questionnaire_response_item3_2_1"), poa.getOrganTissueCardOrAgreementInstitution());
            responseList.add(item3_2_1);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3 = createItemBooleanType("3.3",
                getTranslation("HCPOA-questionnaire_response_item3_3"), poa.isPermitOrganTissueDonation());
        responseList.add(item3_3);
        if (poa.isPermitOrganTissueDonation()) {
            //what parts
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_1 = createItemBooleanType("3.3.1.1",
                    getTranslation("HCPOA-questionnaire_response_item3_3_1_1"), poa.isWholeBodyDonation());
            responseList.add(item3_3_1_1);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_2 = createItemBooleanType("3.3.1.2",
                    getTranslation("HCPOA-questionnaire_response_item3_3_1_2"), poa.isAnyPartOrOrganNeeded());
            responseList.add(item3_3_1_2);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_3 = createItemBooleanType("3.3.1.3",
                    getTranslation("HCPOA-questionnaire_response_item3_3_1_3"), poa.isSpecificPartsOrOrgans());
            responseList.add(item3_3_1_3);
            if (poa.isSpecificPartsOrOrgans()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_4 = createItemStringType("3.3.1.4",
                        getTranslation("HCPOA-questionnaire_response_item3_3_1_4"), poa.getSpecificPartsOrOrgansList());
                responseList.add(item3_3_1_4);
            }
            //for what purpose
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_1 = createItemBooleanType("3.3.2.1",
                    getTranslation("HCPOA-questionnaire_response_item3_3_2_1"), poa.isAnyLegalPurpose());
            responseList.add(item3_3_2_1);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_2 = createItemBooleanType("3.3.2.2",
                    getTranslation("HCPOA-questionnaire_response_item3_3_2_2"), poa.isTransplantOrTherapeutic());
            responseList.add(item3_3_2_2);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_3 = createItemBooleanType("3.3.2.3",
                    getTranslation("HCPOA-questionnaire_response_item3_3_2_3"), poa.isResearchOnly());
            responseList.add(item3_3_2_3);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_4 = createItemBooleanType("3.3.2.4",
                    getTranslation("HCPOA-questionnaire_response_item3_3_2_4"), poa.isOtherPurposes());
            responseList.add(item3_3_2_4);
            if (poa.isOtherPurposes()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_5 = createItemStringType("3.3.2.5",
                        getTranslation("HCPOA-questionnaire_response_item3_3_2_5"), poa.getOtherPurposesList());
                responseList.add(item3_3_2_5);
            }
            //destinations of body, organs, or parts
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_3_1 = createItemBooleanType("3.3.3.1",
                    getTranslation("HCPOA-questionnaire_response_item3_3_3_1"), poa.isPrincipleDefined());
            responseList.add(item3_3_3_1);
            if (poa.isPrincipleDefined()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_3_2 = createItemStringType("3.3.3.2",
                        getTranslation("HCPOA-questionnaire_response_item3_3_3_2"), poa.getPrincipleDefinedList());
                responseList.add(item3_3_3_2);
            }
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_3_3 = createItemBooleanType("3.3.3.3",
                    getTranslation("HCPOA-questionnaire_response_item3_3_3_3"), poa.isAgentDecidedOrganTissueDestination());
            responseList.add(item3_3_3_3);
        }
    }

    private void bodyDispositionResponse() {
        //Burial
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1 = createItemBooleanType("4.1",
                getTranslation("HCPOA-questionnaire_response_item4_1"), poa.isBodyToBeBuried());
        responseList.add(item4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2 = createItemBooleanType("4.2",
                getTranslation("HCPOA-questionnaire_response_item4_2"), poa.isBodyToBeBuriedIn());
        responseList.add(item4_2);
        if (poa.isBodyToBeBuriedIn()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2_1 = createItemStringType("4.2.1",
                    getTranslation("HCPOA-questionnaire_response_item4_2_1"), poa.getBodyToBeBuriedInInstructions());
            responseList.add(item4_2_1);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_3 = createItemBooleanType("4.3",
                getTranslation("HCPOA-questionnaire_response_item4_3"), poa.isBodyToBeCremated());
        responseList.add(item4_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_4 = createItemBooleanType("4.4",
                getTranslation("HCPOA-questionnaire_response_item4_4"), poa.isBodyToBeCrematedAshesDisposition());
        responseList.add(item4_4);
        if (poa.isBodyToBeCrematedAshesDisposition()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item4_4_1 = createItemStringType("4.4.1",
                    getTranslation("HCPOA-questionnaire_response_item4_4_1"), poa.getBodyToBeCrematedAshesDispositionInstructions());
            responseList.add(item4_4_1);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_5 = createItemBooleanType("4.5",
                getTranslation("HCPOA-questionnaire_response_item4_5"), poa.isAgentDecidesBurial());
        responseList.add(item4_5);
    }

    private void physicianAffidavitResponse() {
        //Physician Affidavit(Optional)
        boolean physiciansAffidavitSignature = false;
        if (poa.getPhysiciansAffidavit().getBase64EncodedSignature() != null && poa.getPhysiciansAffidavit().getBase64EncodedSignature().length > 0) physiciansAffidavitSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_1 = createItemStringType("5.1",
                getTranslation("HCPOA-questionnaire_response_item5_1"), poa.getPhysiciansAffidavit().getPhysiciansName());
        responseList.add(item5_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_2 = createItemBooleanType("5.2",
                getTranslation("HCPOA-questionnaire_response_item5_2"), physiciansAffidavitSignature);
        responseList.add(item5_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_3 = createItemStringType("5.3",
                getTranslation("HCPOA-questionnaire_response_item5_3"), poa.getPhysiciansAffidavit().getSignatureDate());
        responseList.add(item5_3);
    }

    private void hipaaResponse() {
        //hipaa
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_1 = createItemBooleanType("6.1",
                getTranslation("HCPOA-questionnaire_response_item6_1"), poa.getHipaaWaiver().isUseDisclosure());
        responseList.add(item6_1);
    }

    private void signatureRequirementsResponse() {
        //signature requirements
        boolean patientSignature = false;
        if (poa.getPrincipleSignature().getBase64EncodeSignature() != null && poa.getPrincipleSignature().getBase64EncodeSignature().length > 0) patientSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7 = createItemBooleanType("7",
                getTranslation("HCPOA-questionnaire_response_item7"), patientSignature);
        responseList.add(item7);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item8_1 = createItemStringType("8.1",
                getTranslation("HCPOA-questionnaire_response_item8_1"), poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary());
        responseList.add(item8_1);

        boolean patientUnableToSign = false;
        if (poa.getPrincipleAlternateSignature().getBase64EncodedSignature() != null && poa.getPrincipleAlternateSignature().getBase64EncodedSignature().length > 0) patientUnableToSign = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item8_2 = createItemBooleanType("8.2",
                getTranslation("HCPOA-questionnaire_response_item8_2"), patientUnableToSign);
        responseList.add(item8_2);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_1 = createItemStringType("9.1",
                getTranslation("HCPOA-questionnaire_response_item9_1"), poa.getWitnessSignature().getWitnessName());
        responseList.add(item9_1);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_2 = createItemStringType("9.2",
                getTranslation("HCPOA-questionnaire_response_item9_2"), poa.getWitnessSignature().getWitnessAddress());
        responseList.add(item9_2);

        boolean witnessSignature = false;
        if (poa.getWitnessSignature().getBase64EncodedSignature() != null && poa.getWitnessSignature().getBase64EncodedSignature().length > 0) witnessSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_3 = createItemBooleanType("9.3",
                getTranslation("HCPOA-questionnaire_response_item9_3"), witnessSignature);
        responseList.add(item9_2);
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
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-critical_error"), 0));
        }
    }

    private void errorCheckCommon() {
        //user initials
        try {
            if (base64PatientInitials == null || base64PatientInitials.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("HCPOA-user_initials_can_not_be_blank"), 0));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-user_initials_can_not_be_blank"), 0));
        }

        //agent
        if (poa.getAgent().getName() == null || poa.getAgent().getName().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-agent_name_can_not_be_blank"), 2));
        }
        if (poa.getAgent().getAddress1() == null || poa.getAgent().getAddress1().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-agent_name_can_not_be_blank"), 2));
        }
        if ((poa.getAgent().getHomePhone() == null || poa.getAgent().getHomePhone().isEmpty()) &&
                (poa.getAgent().getWorkPhone() == null || poa.getAgent().getWorkPhone().isEmpty()) &&
                (poa.getAgent().getCellPhone() == null || poa.getAgent().getCellPhone().isEmpty())) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-a_minimum_of_1_agent_phone_number_should_be_provided"), 2));
        }

        //alternate
        if (poa.getAlternate().getName() == null || poa.getAlternate().getName().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-alternate_name_can_not_be_blank"), 3));
        }
        if (poa.getAlternate().getAddress1() == null || poa.getAlternate().getAddress1().isEmpty()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-alternate_address_can_not_be_blank"), 3));
        }
        if ((poa.getAlternate().getHomePhone() == null || poa.getAlternate().getHomePhone().isEmpty()) &&
                (poa.getAlternate().getWorkPhone() == null || poa.getAlternate().getWorkPhone().isEmpty()) &&
                (poa.getAlternate().getCellPhone() == null || poa.getAlternate().getCellPhone().isEmpty())) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-a_minimum_of_1_alternate_phone_number_should_be_provided"), 3));
        }

        //autopsy
        if (!poa.isPermitAutopsy() && !poa.isDenyAutopsy() && !poa.isAgentDecidesAutopsy()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-no_autopsy_selection_made"), 6));
        }

        //organ donation
        if (!poa.isPermitOrganTissueDonation() && !poa.isDenyOrganTissueDonation() && !poa.isHaveExistingOrganTissueCardOrAgreement()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-no_organ_tissue_donation_selection_made"), 7));
        }

        //burial
        if (!poa.isBodyToBeBuriedIn() && !poa.isBodyToBeBuried() && !poa.isAgentDecidesBurial() && !poa.isBodyToBeCremated() && !poa.isBodyToBeCrematedAshesDisposition()) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-no_burial_instructions_selection_made"), 8));
        }

    }

    private void errorCheckSignature() {
        try {
            if (base64PatientSignature == null || base64PatientSignature.length == 0) {

                if (base64PatientUnableSignature == null || base64PatientUnableSignature.length == 0) {
                    errorList.add(new QuestionnaireError(getTranslation("HCPOA-user_signature_or_alternate_signature_required"), 12));
                }
                else {
                    try {
                        if (poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary() == null || poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary().isEmpty()) {
                            errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_or_notary_as_alternate_name_required"), 13));
                        }
                    }
                    catch (Exception ex) {
                        errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_or_notary_as_alternate_name_required"), 13));
                    }
                }
            }
        }
        catch(Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-user_signature_or_alternate_signature_required"), 12));
        }

        try {
            if (base64WitnessSignature == null || base64WitnessSignature.length == 0) {
                errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_signature_can_not_be_blank"), 14));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_signature_can_not_be_blank"), 14));
        }
        try {
            if (poa.getWitnessSignature().getWitnessName() == null || poa.getWitnessSignature().getWitnessName().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_name_can_not_be_blank"), 14));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_name_can_not_be_blank"), 14));
        }
        try {
            if (poa.getWitnessSignature().getWitnessAddress() == null || poa.getWitnessSignature().getWitnessAddress().isEmpty()) {
                errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_address_can_not_be_blank"), 14));
            }
        }
        catch (Exception ex) {
            errorList.add(new QuestionnaireError(getTranslation("HCPOA-witness_address_can_not_be_blank"), 14));
        }
    }

    private void createErrorDialog() {
        Html errorIntro = new Html(getTranslation("HCPOA-error_intro"));
        Html flowTypeIntro;
        if (advDirectiveFlowType.equals("Default")) {
            flowTypeIntro = new Html(getTranslation("HCPOA-flow_type_intro1"));
        }
        else {
            flowTypeIntro = new Html(getTranslation("HCPOA-flow_type_intro2"));
        }

        Button errorBTN = new Button(getTranslation("HCPOA-correct_errors"));
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
        errorDialog.add(createHeader(VaadinIcon.WARNING, getTranslation("HCPOA-failed_verification")),errorIntro, flowTypeIntro, verticalLayout, errorBTN);
    }
}
