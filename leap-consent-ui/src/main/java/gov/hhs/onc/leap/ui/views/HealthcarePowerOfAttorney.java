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
import gov.hhs.onc.leap.backend.ConsentUser;
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
import gov.hhs.onc.leap.ui.util.pdf.PDFDNRHandler;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@PageTitle("Healthcare Power Of Attorney")
@Route(value = "healthcarepowerofattorney", layout = MainLayout.class)
public class HealthcarePowerOfAttorney extends ViewFrame {

    private PDFSigningService PDFSigningService;
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
        Html intro = new Html("<p><b>GENERAL INFORMATION AND INSTRUCTIONS:</b> Use this questionnaire if you want to select a person, called an <b>agent</b>, "+
                "to make future health care decisions for you so that if you become too ill or cannot make those decisions for yourself the person you choose"+
                " and trust can make medical decisions for you. Be sure you review and understand the importance of the document that is created at the end of this process."+
                " It is a good idea to talk to your doctor and loved ones if you have questions about the type of health care you do or do not want. At anytime click on "+
                "the <b>View your states Healthcare Power of Attorney form and instructions</b> button for additional information." );


        createPatientsInitials();
        createPatientGeneralInfo();
        createPOASelection();
        createALTSelection();
        createAuthorizationSelection();
        createAuthExceptionSelection();
        createAutopsySelection();
        createOrganDonationSelection();
        createBurialSelection();
        createAttestation();
        createHipaa();
        createPatientSignature();
        createPatientUnableSignature();
        createWitnessSignature();

        createInfoDialog();

        FlexBoxLayout content = new FlexBoxLayout(intro, patientInitialsLayout, patientGeneralInfoLayout, poaSelectionLayout,
                altSelectionLayout, authorizationLayout, authExceptionLayout, autopsySelectionLayout, organDonationSelectionLayout, burialSelectionLayout,
                attestationLayout, hipaaLayout, patientSignatureLayout, patientUnableSignatureLayout, witnessSignatureLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createPatientsInitials() {
        Html intro2 = new Html("<p>Before you begin with the <b>Healthcare Power of Attorney</b> questionnaire we need to capture" +
                               " your initials.  Your initials will be applied your state's form based on your responsives.</p>");

        patientInitials = new SignaturePad();
        patientInitials.setHeight("100px");
        patientInitials.setWidth("150px");
        patientInitials.setPenColor("#2874A6");

        Button clearPatientInitials = new Button("Clear Initials");
        clearPatientInitials.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientInitials.addClickListener(event -> {
            patientInitials.clear();
        });
        Button savePatientInitials = new Button("Accept Initials");
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

        patientInitialsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro2, new BasicDivider(), patientInitials, sigLayout);
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
        Html intro3 = new Html("<p><b>My Information(I am the \"Principle\")</b></p>");

        patientFullNameField = new TextField("Name");
        patientAddress1Field = new TextField("Address");
        patientAddress2Field = new TextField("");
        patientDateOfBirthField = new TextField("Date of Birth");
        patientPhoneNumberField = new TextField("Phone");
        patientEmailAddressField = new TextField("Email");

        //set values
        patientFullNameField.setValue(consentUser.getFirstName()+" "+consentUser.getMiddleName()+" "+consentUser.getLastName());
        patientAddress1Field.setValue(consentUser.getStreetAddress1()+" "+consentUser.getStreetAddress2());
        patientAddress2Field.setValue(consentUser.getCity()+" "+consentUser.getState()+" "+consentUser.getZipCode());
        patientPhoneNumberField.setValue(consentUser.getPhone());
        patientDateOfBirthField.setValue(getDateString(consentUser.getDateOfBirth()));
        patientEmailAddressField.setValue(consentUser.getEmailAddress());

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro3, new BasicDivider(),
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
        Html intro4 = new Html("<p><b>Selection of my Healthcare Power of Attorney and Alternate:</b> "+
                "I choose the following person to act as my <b>agent</b> to make health care decisions for me.</p>");

        poaFullNameField = new TextField("Name");
        poaAddress1Field = new TextField("Address");
        poaAddress2Field = new TextField("");
        poaHomePhoneField = new TextField("Home Phone");
        poaWorkPhoneField = new TextField("Work Phone");
        poaCellPhoneField = new TextField("Cell Phone");

        poaSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro4, new BasicDivider(),
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
        Html intro5 = new Html("<p><b>Selection of my Healthcare Power of Attorney and Alternate:</b> "+
                "I choose the following person to act as an <b>alternate</b> to make health care decisions for me if my "+
                "first agent is unavailable, unwilling, or unable to make decisions for me.</p>");

        altFullNameField = new TextField("Name");
        altAddress1Field = new TextField("Address");
        altAddress2Field = new TextField("");
        altHomePhoneField = new TextField("Home Phone");
        altWorkPhoneField = new TextField("Work Phone");
        altCellPhoneField = new TextField("Cell Phone");

        altSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro5, new BasicDivider(),
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
        Html intro6 = new Html("<p><b>I AUTHORIZE</b> my agent to make health care decisions for me when I cannot make "+
                "or communicate my own health care decisions. I want my agent to make all such decisions for me except any decisions "+
                "that I have expressly stated in this form that I do not authorize him/her to make. My agent should explain to me any "+
                "choices he or she made if I am able to understand. I further authorize my agent to have access to my "+
                "<b>personal protected health care information and medical records</b>. This appointment is effective unless it is "+
                "revoked by me or by a court order.</p>");

        authorizationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro6, new BasicDivider());
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
        Html intro7 = new Html("<p><b>Health care decisions that I expressly DO NOT AUTHORIZE if I am unable to make decisions for myself:</b> "+
                "(Explain or write in \"None\") </p>");

        authException1Field = new TextField("");
        authException2Field = new TextField("");
        authException3Field = new TextField("");

        authExceptionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro7, new BasicDivider(),
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
        Html intro8 = new Html("<p><b>My specific wishes regarding autopsy</b></p>");

        autopsyButtonGroup = new RadioButtonGroup();
        autopsyButtonGroup.setLabel("Please note that if not required by law a voluntary autopsy may cost money.");
        autopsyButtonGroup.setItems("Upon my death I DO NOT consent to a voluntary autopsy.",
                "Upon my death I DO consent to a voluntary autopsy.", "My agent may give or refuse consent for an autopsy.");
        autopsyButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        autopsySelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro8, new BasicDivider(),
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
        Html intro9 = new Html("<p><b>My specific wishes regarding organ donation</b></p>");
        organDonationButtonGroup = new RadioButtonGroup();
        organDonationButtonGroup.setLabel("If you do not make a selection your agent may make decisions for you.");
        organDonationButtonGroup.setItems("I DO NOT WANT to make an organ or tissue donation, and I DO NOT want this donation authorized on my behalf by my agent or my family.",
                "I have already signed a written agreement or donor card regarding donation with the following individual or institution.",
                "I DO WANT to make an organ or tissue donation when I die. Here are my directions:");
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
            else if (v.contains("I have already signed")) {
                institutionAgreementField.setVisible(true);
                whatTissuesButtonGroup.setVisible(false);
                specificOrgansField.setVisible(false);
                pouOrganDonationButtonGroup.setVisible(false);
                otherPurposesField.setVisible(false);
                organizationOrganDonationButtonGroup.setVisible(false);
                patientChoiceOfOrganizations.setVisible(false);
            }
            else if (v.contains("I DO WANT")) {
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

        institutionAgreementField = new TextField("Institution");
        institutionAgreementField.setVisible(false);

        whatTissuesButtonGroup = new RadioButtonGroup();
        whatTissuesButtonGroup.setLabel("What organs/tissues I choose to donate");
        whatTissuesButtonGroup.setItems("Whole body", "Any needed parts or organs","Specific parts or organs only");
        whatTissuesButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        whatTissuesButtonGroup.addValueChangeListener(event -> {
           String v = (String)event.getValue();
           if (v.contains("Specific parts")) {
               specificOrgansField.setVisible(true);
           }
           else {
               specificOrgansField.setVisible(false);
           }
        });
        whatTissuesButtonGroup.setVisible(false);

        specificOrgansField = new TextField("Specific parts or organs only");
        specificOrgansField.setVisible(false);

        pouOrganDonationButtonGroup = new RadioButtonGroup();
        pouOrganDonationButtonGroup.setLabel("I am donating organs/tissue for");
        pouOrganDonationButtonGroup.setItems("Any legally authorized purpose","Transplant or therapeutic purposes only",
                "Research only","Other");
        pouOrganDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        pouOrganDonationButtonGroup.addValueChangeListener(event -> {
            String v = (String)event.getValue();
            if (v.equals("Other")) {
                otherPurposesField.setVisible(true);
            }
            else {
                otherPurposesField.setVisible(false);
            }
        });
        pouOrganDonationButtonGroup.setVisible(false);

        otherPurposesField = new TextField("Other Purposes");
        otherPurposesField.setVisible(false);

        organizationOrganDonationButtonGroup = new RadioButtonGroup();
        organizationOrganDonationButtonGroup.setLabel("The organization or person I want my organs/tissue to go to are");
        organizationOrganDonationButtonGroup.setItems("My List", "Any that my agent chooses");
        organizationOrganDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        organizationOrganDonationButtonGroup.addValueChangeListener(event -> {
            String v = (String)event.getValue();
            if (v.equals("My List")) {
                patientChoiceOfOrganizations.setVisible(true);
            }
            else {
                patientChoiceOfOrganizations.setVisible(false);
            }
        });
        organizationOrganDonationButtonGroup.setVisible(false);

        patientChoiceOfOrganizations = new TextField("List Organizations");
        patientChoiceOfOrganizations.setVisible(false);


        organDonationSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro9, new BasicDivider(),
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
        Html intro10 = new Html("<p><b>My specific wishes regarding funeral and burial disposition</b></p>");

        burialSelectionButtonGroup = new RadioButtonGroup();
        burialSelectionButtonGroup.setItems("Upon my death, I direct my body to be buried. (Instead of cremated)",
                "Upon my death, I direct my body to be buried in:", "Upon my death, I direct my body to be cremated.",
                "Upon my death, I direct my body to be cremated with my ashes to be", "My agent will make all funeral and burial decisions.");
        burialSelectionButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        burialSelectionButtonGroup.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                String v = (String) event.getValue();
                if (v.contains("I direct my body to be buried in")) {
                    buriedInField.setVisible(true);
                    ashesDispositionField.setVisible(false);
                } else if (v.contains("cremated with my ashes to be")) {
                    buriedInField.setVisible(false);
                    ashesDispositionField.setVisible(true);
                } else {
                    buriedInField.setVisible(false);
                    ashesDispositionField.setVisible(false);
                }
            }
        });

        buriedInField = new TextField("I direct my body to be buried in following");
        buriedInField.setVisible(false);

        ashesDispositionField = new TextField("I direct the following to be done with my ashes:");
        ashesDispositionField.setVisible(false);

        burialSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"), intro10, new BasicDivider(),
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

    private void createAttestation() {
        Html intro11 = new Html("<p><b>Physician Affidavit(Optional)</b></p>");
        Html intro12 = new Html("<p>You may wish to ask questions of your physician regarding a particular treatment or about the options "+
                "in the form. If you do speak with your physician it is a good idea to ask your physician to complete" +
                " this affidavit and keep a copy for his/her file.</p>");

        Html para1 = new Html("<p>I Dr.</p>");
        attestationDRName = new TextField("Physicians Name");
        Html para2 = new Html("<p>have reviewed this document and have discussed with</p>");
        attestationPatientName = new TextField("Patients Name");
        attestationPatientName.setValue(consentUser.getFirstName()+" "+consentUser.getMiddleName()+" "+consentUser.getLastName());
        Html para3 = new Html("any questions regarding the probable medical consequences of the treatment choices provided above. "+
                "This discussion with the principal occurred on this day</p>");
        attestationDate = new TextField("Date");
        attestationDate.setValue(getDateString(new Date()));
        Html para4 = new Html("<p>I have agreed to comply with the provisions of this directive.</p>");

        physcianSignature = new SignaturePad();
        physcianSignature.setHeight("100px");
        physcianSignature.setWidth("400px");
        physcianSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button("Clear Signature");
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            physcianSignature.clear();
        });
        Button savePatientSig = new Button("Accept Signature");
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

        attestationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"), intro11, intro12, new BasicDivider(),
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
        Html intro13 = new Html("<p><b>HIPAA WAIVER OF CONFIDENTIALITY FOR MY AGENT</b></p>");

        hipaaButton = new RadioButtonGroup();
        hipaaButton.setItems("I intend for my agent to be treated as I would be with respect to my rights regarding "+
                "the use and disclosure of my individually identifiable health information or other medical " +
                "records. This release authority applies to any information governed by the Health Insurance "+
                "Portability and Accountability Act of 1996 (aka HIPAA), 42 USC 1320d and 45 CFR 160-164.");
        hipaaButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        hipaaLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"), intro13, new BasicDivider(),
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
        Html intro14 = new Html("<p><b>MY SIGNATURE VERIFICATION FOR THE HEALTH CARE POWER OF ATTORNEY</b></p>");

        patientSignature = new SignaturePad();
        patientSignature.setHeight("100px");
        patientSignature.setWidth("400px");
        patientSignature.setPenColor("#2874A6");

        Button clearPatientSig = new Button("Clear Signature");
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            patientSignature.clear();
        });
        Button savePatientSig = new Button("Accept Signature");
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

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"), intro14, new BasicDivider(),
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
        Html intro15 = new Html("<p><b>If you are unable to physically sign this document, "+
                "your witness/notary may sign and initial for you. If applicable have your witness/notary sign below.</b></p>");
        Html intro16 = new Html("<p>Witness/Notary Verification: The principal of this document directly indicated to me "+
                "that this Health Care Power of Attorney expresses their wishes and that they intend to adopt it at this time.</p>");

        patientUnableSignatureNameField = new TextField("Name");

        patientUnableSignature = new SignaturePad();
        patientUnableSignature.setHeight("100px");
        patientUnableSignature.setWidth("400px");
        patientUnableSignature.setPenColor("#2874A6");

        Button clearPatientUnableSig = new Button("Clear Signature");
        clearPatientUnableSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientUnableSig.addClickListener(event -> {
            patientUnableSignature.clear();
        });
        Button savePatientUnableSig = new Button("Accept Signature");
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

        patientUnableSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"), intro15, intro16, new BasicDivider(),
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
        Html intro17 = new Html("<p><b>SIGNATURE OF WITNESS</b></p>");
        Html intro18 = new Html("<p>I was present when this form was signed (or marked). The principal appeared to "+
                "be of sound mind and was not forced to sign this form. I affirm that I meet the requirements to be a witness "+
                "as indicated on page one of the health care power of attorney form.</p>");

        witnessName = new TextField("Witness Name");
        witnessAddress = new TextField("Address");

        witnessSignature = new SignaturePad();
        witnessSignature.setHeight("100px");
        witnessSignature.setWidth("400px");
        witnessSignature.setPenColor("#2874A6");

        Button clearWitnessSig = new Button("Clear Signature");
        clearWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearWitnessSig.addClickListener(event -> {
            witnessSignature.clear();
        });
        Button saveWitnessSig = new Button("Accept Signature");
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

        witnessSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"), intro17, intro18, new BasicDivider(),
                witnessName, witnessAddress, witnessSignature, sigLayout);
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
        viewStateForm = new Button("View your states Healthcare Power of Attorney instructions");
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
        Span content = new Span("FHIR advanced directive - POA Healthcare successfully created!");

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
                attestationLayout.setVisible(true);
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
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(true);
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
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(true);
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
                attestationLayout.setVisible(false);
                hipaaLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                patientUnableSignatureLayout.setVisible(true);
                witnessSignatureLayout.setVisible(false);
                break;
            case 13:
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


        Button closeButton = new Button("Cancel", e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button("Accept and Submit");
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            docDialog.close();
            createQuestionnaireResponse();
            createFHIRConsent();
            successNotification();
            //todo test for fhir consent create success
            resetFormAndNavigation();
            evalNavigation();
        });

        Button acceptAndPrintButton = new Button("Accept and Get Notarized");
        acceptAndPrintButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));

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
        if (autopsy.contains("I DO NOT consent")) {
            poa.setDenyAutopsy(true);
        }
        else if (autopsy.contains("I DO consent")) {
            poa.setPermitAutopsy(true);
        }
        else if (autopsy.contains("My agent")) {
            poa.setAgentDecidesAutopsy(true);
        }
        else {
            //nothing to set
        }

        //Organ Donation
        String organdonation = (String)organDonationButtonGroup.getValue();
        if (organdonation.contains("I DO NOT WANT")) {
            poa.setDenyOrganTissueDonation(true);
        }
        else if(organdonation.contains("I have already")) {
            poa.setHaveExistingOrganTissueCardOrAgreement(true);
            poa.setOrganTissueCardOrAgreementInstitution(institutionAgreementField.getValue());
        }
        else if (organdonation.contains("I DO WANT")) {
            poa.setPermitOrganTissueDonation(true);
            String whatOrgans = (String)whatTissuesButtonGroup.getValue();
            if (whatOrgans.contains("Whole body")) {
                poa.setWholeBodyDonation(true);
            }
            else if (whatOrgans.contains("Any needed")) {
                poa.setAnyPartOrOrganNeeded(true);
            }
            else if (whatOrgans.contains("These parts")) {
                poa.setSpecificPartsOrOrgans(true);
                poa.setSpecificPartsOrOrgansList(specificOrgansField.getValue());
            }
            else {
                //nothing to select in what tissues
            }
            String forWhatPurpose = (String)pouOrganDonationButtonGroup.getValue();
            if (forWhatPurpose.contains("Any legal")) {
                poa.setAnyLegalPurpose(true);
            }
            else if (forWhatPurpose.contains("Transplant")) {
                poa.setTransplantOrTherapeutic(true);
            }
            else if (forWhatPurpose.contains("Research")) {
                poa.setResearchOnly(true);
            }
            else if (forWhatPurpose.contains("Other")) {
                poa.setOtherPurposes(true);
                poa.setOtherPurposesList(otherPurposesField.getValue());
            }
            else {
                //nothing to set
            }
            String whatOrganizations = (String)organizationOrganDonationButtonGroup.getValue();
            if (whatOrganizations.contains("Any that my agent chooses")) {
                poa.setAgentDecidedOrganTissueDestination(true);
            }
            else if(whatOrganizations.contains("My List")) {
                poa.setPrincipleDefined(true);
                poa.setPrincipleDefinedList(patientChoiceOfOrganizations.getValue());
            }
            else {
                //no setting
            }
        }
        else {
            //nothing to set
        }

        //Burial
        String burial = (String)burialSelectionButtonGroup.getValue();
        if (burial.contains("buried.")) {
            poa.setBodyToBeBuried(true);
        }
        else if (burial.contains("buried in")) {
            poa.setBodyToBeBuriedIn(true);
            poa.setBodyToBeBuriedInInstructions(buriedInField.getValue());
        }
        else if (burial.contains("cremated.")) {
            poa.setBodyToBeCremated(true);
        }
        else if (burial.contains("cremated with my asshes")) {
            poa.setBodyToBeCrematedAshesDisposition(true);
            poa.setBodyToBeCrematedAshesDispositionInstructions(ashesDispositionField.getValue());
        }
        else if (burial.contains("My agent")) {
            poa.setAgentDecidesBurial(true);
        }
        else {
            //nothing to set
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
        hipaa.setUseDisclosure(hipaaValue.contains("I intend"));
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
        poaDirective.setId("POAHealthcare-"+patient.getId());
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
        patientRef.setReference("Patient/"+patient.getId());
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
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/leap-poahealthcare-"+consentSession.getFhirPatient().getId()));
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
        questionnaireResponse.setId("leap-poahealthcare-" + consentSession.getFhirPatient().getId());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/" + consentSession.getFhirPatient().getId());
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
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_1 = createItemStringType("1.1.1", "POA Name", poa.getAgent().getName());
        responseList.add(item1_1_1);
        //poa address
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_2 = createItemStringType("1.1.2", "POA Address", poa.getAgent().getAddress1() + " " + poa.getAgent().getAddress2());
        responseList.add(item1_1_2);
        //poa home phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_3 = createItemStringType("1.1.3", "POA Home Phone", poa.getAgent().getHomePhone());
        responseList.add(item1_1_3);
        //poa work phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_4 = createItemStringType("1.1.4", "POA Work Phone", poa.getAgent().getWorkPhone());
        responseList.add(item1_1_4);
        //poa cell phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1_5 = createItemStringType("1.1.5", "POA Cell Phone", poa.getAgent().getCellPhone());
        responseList.add(item1_1_5);
    }

    private void alternatePowerOfAttorneyResponse() {
        //alternate name
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_1 = createItemStringType("1.2.1", "Alternate Name", poa.getAlternate().getName());
        responseList.add(item1_2_1);
        //alternate address
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_2 = createItemStringType("1.2.2", "Alternate Address", poa.getAlternate().getAddress1() + " " + poa.getAlternate().getAddress2());
        responseList.add(item1_2_2);
        //alternate home phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_3 = createItemStringType("1.2.3", "Alternate Home Phone", poa.getAlternate().getHomePhone());
        responseList.add(item1_2_3);
        //alternate work phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_4 = createItemStringType("1.2.4", "Alternate Work Phone", poa.getAlternate().getWorkPhone());
        responseList.add(item1_2_4);
        //alternate cell phone
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2_5 = createItemStringType("1.2.5", "Alternate Cell Phone", poa.getAlternate().getCellPhone());
        responseList.add(item1_2_5);
    }

    private void powerOfAttorneyAuthorizationResponse() {
        //ADD ACTIONABLE QUESTIONS
        // Authorize poa to make decisions
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3 = createItemBooleanType("1.3", "I AUTHORIZE my agent to make health care decisions for me when I cannot make or communicate my own health care decisions. I want my agent to make all such decisions for me except any decisions that I have expressly stated in this form that I do not authorize him/her to make. My agent should explain to me any choices he or she made if I am able to understand. I further authorize my agent to have access to my personal protected health care information and medical records. This appointment is effective unless it is revoked by me or by a court order.", true);//this form when completed makes this statement true
        responseList.add(item1_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_4 = createItemStringType("1.4", "Health care decisions that I expressly DO NOT AUTHORIZE if I am unable to make decisions for myself: (Explain or write in \"None\")", poa.getDoNotAuthorize1()+" "+poa.getDoNotAuthorize2()+" "+poa.getDoNotAuthorize3());
        responseList.add(item1_4);
    }

    private void autopsyResponse() {
        //Autospy
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemBooleanType("2.1", "Upon my death, I DO NOT consent to a voluntary autopsy.", poa.isDenyAutopsy());
        responseList.add(item2_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2", "Upon my death, I DO consent to a voluntary autospy.", poa.isPermitAutopsy());
        responseList.add(item2_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_3 = createItemBooleanType("2.3", "My agent will give or refuse consent for an autospy", poa.isAgentDecidesAutopsy());
        responseList.add(item2_3);
    }

    private void organTissueDonationResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_1 = createItemBooleanType("3.1", "I DO NOT WANT to make an organ or tissue donation, and I DO NOT want this donation authorized on my behalf by my agent or my family.", poa.isDenyOrganTissueDonation());
        responseList.add(item3_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2 = createItemBooleanType("3.2", "I have already signed a written agreement or donor card regarding donation with the following individual or institution.", poa.isHaveExistingOrganTissueCardOrAgreement());
        responseList.add(item3_2);
        if (poa.isHaveExistingOrganTissueCardOrAgreement()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2_1 = createItemStringType("3.2.1", "Institution Name", poa.getOrganTissueCardOrAgreementInstitution());
            responseList.add(item3_2_1);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3 = createItemBooleanType("3.3", "I DO WANT to make an organ or tissue donation when I die. Here are my directions", poa.isPermitOrganTissueDonation());
        responseList.add(item3_3);
        if (poa.isPermitOrganTissueDonation()) {
            //what parts
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_1 = createItemBooleanType("3.3.1.1", "Whole body", poa.isWholeBodyDonation());
            responseList.add(item3_3_1_1);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_2 = createItemBooleanType("3.3.1.2", "Any needed parts or organs", poa.isAnyPartOrOrganNeeded());
            responseList.add(item3_3_1_2);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_3 = createItemBooleanType("3.3.1.3", "Specific parts or organs", poa.isSpecificPartsOrOrgans());
            responseList.add(item3_3_1_3);
            if (poa.isSpecificPartsOrOrgans()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_1_4 = createItemStringType("3.3.1.4", "Specific part or organs only", poa.getSpecificPartsOrOrgansList());
                responseList.add(item3_3_1_4);
            }
            //for what purpose
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_1 = createItemBooleanType("3.3.2.1", "Any legally authorized purpose", poa.isAnyLegalPurpose());
            responseList.add(item3_3_2_1);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_2 = createItemBooleanType("3.3.2.2", "For Transplant or Therapeutic treatment purposes", poa.isTransplantOrTherapeutic());
            responseList.add(item3_3_2_2);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_3 = createItemBooleanType("3.3.2.3", "Research Only", poa.isResearchOnly());
            responseList.add(item3_3_2_3);
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_4 = createItemBooleanType("3.3.2.4", "Other", poa.isOtherPurposes());
            responseList.add(item3_3_2_4);
            if (poa.isOtherPurposes()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_2_5 = createItemStringType("3.3.2.5", "Other purposes", poa.getOtherPurposesList());
                responseList.add(item3_3_2_5);
            }
            //destinations of body, organs, or parts
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_3_1 = createItemBooleanType("3.3.3.1", "My List(destinations)", poa.isPrincipleDefined());
            responseList.add(item3_3_3_1);
            if (poa.isPrincipleDefined()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_3_2 = createItemStringType("3.3.3.2", "List of destinations", poa.getPrincipleDefinedList());
                responseList.add(item3_3_3_2);
            }
            QuestionnaireResponse.QuestionnaireResponseItemComponent item3_3_3_3 = createItemBooleanType("3.3.3.3", "Any my agent chooses", poa.isAgentDecidedOrganTissueDestination());
            responseList.add(item3_3_3_3);
        }
    }

    private void bodyDispositionResponse() {
        //Burial
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1 = createItemBooleanType("4.1", "Upon my death, I direct my body to be buried.(instead of cremated)", poa.isBodyToBeBuried());
        responseList.add(item4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2 = createItemBooleanType("4.2", "Upon my death, I direct my body to be buried in:", poa.isBodyToBeBuriedIn());
        responseList.add(item4_2);
        if (poa.isBodyToBeBuriedIn()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2_1 = createItemStringType("4.2.1", "I direct my body to be buried in following:", poa.getBodyToBeBuriedInInstructions());
            responseList.add(item4_2_1);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_3 = createItemBooleanType("4.3", "Upon my death, I direct my body to be cremated.", poa.isBodyToBeCremated());
        responseList.add(item4_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_4 = createItemBooleanType("4.4", "Upon my death, I direct my body to be cremated with my ashes to be,", poa.isBodyToBeCrematedAshesDisposition());
        responseList.add(item4_4);
        if (poa.isBodyToBeCrematedAshesDisposition()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent item4_4_1 = createItemStringType("4.4.1", "Upon my death, I direct my body to be cremated with my ashes to be,", poa.getBodyToBeCrematedAshesDispositionInstructions());
            responseList.add(item4_4_1);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_5 = createItemBooleanType("4.5", "My agent will make all funeral and burial decisions.", poa.isAgentDecidesBurial());
        responseList.add(item4_5);
    }

    private void physicianAffidavitResponse() {
        //Physician Affidavit(Optional)
        boolean physiciansAffidavitSignature = false;
        if (poa.getPhysiciansAffidavit().getBase64EncodedSignature() != null && poa.getPhysiciansAffidavit().getBase64EncodedSignature().length > 0) physiciansAffidavitSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_1 = createItemStringType("5.1", "Physicians Name", poa.getPhysiciansAffidavit().getPhysiciansName());
        responseList.add(item5_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_2 = createItemBooleanType("5.2", "Physicians signature acquired", physiciansAffidavitSignature);
        responseList.add(item5_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_3 = createItemStringType("5.3", "String Date", poa.getPhysiciansAffidavit().getSignatureDate());
        responseList.add(item5_3);
    }

    private void hipaaResponse() {
        //hipaa
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6_1 = createItemBooleanType("6.1", "HIPAA Waiver of confidentiality for my agent", poa.getHipaaWaiver().isUseDisclosure());
        responseList.add(item6_1);
    }

    private void signatureRequirementsResponse() {
        //signature requirements
        boolean patientSignature = false;
        if (poa.getPrincipleSignature().getBase64EncodeSignature() != null && poa.getPrincipleSignature().getBase64EncodeSignature().length > 0) patientSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item7 = createItemBooleanType("7", "MY SIGNATURE VERIFICATION FOR THE HEALTH CARE POWER OF ATTORNEY", patientSignature);
        responseList.add(item7);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item8_1 = createItemStringType("8.1", "Witness or Notary Name", poa.getPrincipleAlternateSignature().getNameOfWitnessOrNotary());
        responseList.add(item8_1);

        boolean patientUnableToSign = false;
        if (poa.getPrincipleAlternateSignature().getBase64EncodedSignature() != null && poa.getPrincipleAlternateSignature().getBase64EncodedSignature().length > 0) patientUnableToSign = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item8_2 = createItemBooleanType("8.2", "If you are unable to physically sign this document, your witness/notary may sign and initial for you", patientUnableToSign);
        responseList.add(item8_2);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_1 = createItemStringType("9.1", "Witness Name", poa.getWitnessSignature().getWitnessName());
        responseList.add(item9_1);

        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_2 = createItemStringType("9.2", "Witness Address", poa.getWitnessSignature().getWitnessAddress());
        responseList.add(item9_2);

        boolean witnessSignature = false;
        if (poa.getWitnessSignature().getBase64EncodedSignature() != null && poa.getWitnessSignature().getBase64EncodedSignature().length > 0) witnessSignature = true;
        QuestionnaireResponse.QuestionnaireResponseItemComponent item9_3 = createItemBooleanType("9.3", "Witness signature acquired", witnessSignature);
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
}
