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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@PageTitle("Mental Health Power of Attorney")
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

    @Autowired
    private PDFSigningService pdfSigningService;

    @Autowired
    private FHIRConsent fhirConsentClient;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;

    @PostConstruct
    public void setup() {
        setId("mentalhealthpowerofattorney");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html("<p><b>GENERAL INFORMATION AND INSTRUCTIONS:</b> Use this questionnaire if you want to select a person, called an <b>agent</b>, "+
                "to make future health care decisions for you so that if you become too ill or cannot make those decisions for yourself the person you choose"+
                " and trust can make medical decisions for you. Be sure you review and understand the importance of the document that is created at the end of this process."+
                " It is a good idea to talk to your doctor and loved ones if you have questions about the type of health care you do or do not want. At anytime click on "+
                "the <b>View your states Healthcare Power of Attorney form and instructions</b> button for additional information.</p>" );


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
        Html intro2 = new Html("<p>Before you begin with the <b>Mental Health Power of Attorney</b> questionnaire we need to capture" +
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

        patientInitialsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"),intro2, new BasicDivider(), patientInitials, sigLayout);
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

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"),intro3, new BasicDivider(),
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
        Html intro4 = new Html("<p><b>Selection of my Mental Healthcare Power of Attorney and Alternate:</b> "+
                "I choose the following person to act as my <b>agent</b> to make mental health care decisions for me.</p>");

        poaFullNameField = new TextField("Name");
        poaAddress1Field = new TextField("Address");
        poaAddress2Field = new TextField("");
        poaHomePhoneField = new TextField("Home Phone");
        poaWorkPhoneField = new TextField("Work Phone");
        poaCellPhoneField = new TextField("Cell Phone");

        poaSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"),intro4, new BasicDivider(),
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
        Html intro5 = new Html("<p><b>Selection of my Mental Healthcare Power of Attorney and Alternate:</b> "+
                "I choose the following person to act as an <b>alternate</b> to make mental health care decisions for me if my "+
                "first agent is unavailable, unwilling, or unable to make decisions for me.</p>");

        altFullNameField = new TextField("Name");
        altAddress1Field = new TextField("Address");
        altAddress2Field = new TextField("");
        altHomePhoneField = new TextField("Home Phone");
        altWorkPhoneField = new TextField("Work Phone");
        altCellPhoneField = new TextField("Cell Phone");

        altSelectionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"),intro5, new BasicDivider(),
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
        Html intro6 = new Html("<p><b>Mental health treatments that I AUTHORIZE if I am unable to make decisions for myself:</b</p>");
        Html intro7 = new Html("<p>Here are the mental health treatments I authorize my agent to make for me if I become incapable of " +
                "making my own mental health care decisions due to mental or physical illness, injury, disability, or " +
                "incapacity. This appointment is effective unless and until it is revoked by me or by an order of a court. " +
                "My agent is authorized to do the following which I have initialed or marked:</p>");

        authorizedDecisions1 = new RadioButtonGroup();
        authorizedDecisions1.setLabel("");
        authorizedDecisions1.setItems("To receive medical records and information regarding my mental health treatment and to receive, " +
                "review, and consent to disclosure of any of my medical records related to that treatment.");
        authorizedDecisions2 = new RadioButtonGroup();
        authorizedDecisions2.setLabel("");
        authorizedDecisions2.setItems("To consent to the administration of any medications recommended by my treating physician.");
        authorizedDecisions3 = new RadioButtonGroup();
        authorizedDecisions3.setLabel("");
        authorizedDecisions3.setItems("To admit me to an inpatient or partial psychiatric hospitalization program.");
        authorizedDecisions4 = new RadioButtonGroup();
        authorizedDecisions4.setLabel("");
        authorizedDecisions4.setItems("Other:");
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

        authorizationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"),intro6, intro7, new BasicDivider(),
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
        Html intro8 = new Html("<p><b>Mental health care treatments that I expressly DO NOT AUTHORIZE if I am unable " +
                "to make decisions for myself:</b> (Explain or write in \"None\") </p>");

        authException1Field = new TextField("");
        authException2Field = new TextField("");

        authExceptionLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"),intro8, new BasicDivider(),
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
        Html intro9 = new Html("<p><b>Revocability of this Mental Health Care Power of Attorney:</b> This mental health care " +
                "power of attorney or any portion of it may not be revoked and any designated agent may not be disqualified by me during "+
                "times that I am found to be unable to give informed consent. However, at all other times I retain the right to revoke all "+
                "or any portion of this mental health care power of attorney or to disqualify any agent designated by me in this document. ");

        revocationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Healthcare Power of Attorney"),intro9, new BasicDivider());
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
        Html intro10 = new Html("<p><b>HIPAA WAIVER OF CONFIDENTIALITY FOR MY AGENT</b></p>");

        hipaaButton = new RadioButtonGroup();
        hipaaButton.setItems("I intend for my agent to be treated as I would be with respect to my rights regarding "+
                "the use and disclosure of my individually identifiable health information or other medical " +
                "records. This release authority applies to any information governed by the Health Insurance "+
                "Portability and Accountability Act of 1996 (aka HIPAA), 42 USC 1320d and 45 CFR 160-164.");
        hipaaButton.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);

        hipaaLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"), intro10, new BasicDivider(),
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
        Html intro11 = new Html("<p><b>MY SIGNATURE VERIFICATION FOR THE MENTAL HEALTH CARE POWER OF ATTORNEY</b></p>");

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

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"), intro11, new BasicDivider(),
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
        Html intro12 = new Html("<p><b>If you are unable to physically sign this document, "+
                "your witness/notary may sign and initial for you. If applicable have your witness/notary sign below.</b></p>");
        Html intro13 = new Html("<p>Witness/Notary Verification: The principal of this document directly indicated to me "+
                "that Mental Health Care Power of Attorney expresses their wishes and that they intend to adopt it at this time.</p>");

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

        patientUnableSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"), intro12, intro13, new BasicDivider(),
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
        Html intro14 = new Html("<p><b>SIGNATURE OF WITNESS</b></p>");
        Html intro15 = new Html("<p>I was present when this form was signed (or marked). The principal appeared to "+
                "be of sound mind and was not forced to sign this form. I affirm that I meet the requirements to be a witness "+
                "as indicated on page one of the mental health care power of attorney form.</p>");

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

        witnessSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Mental Healthcare Power of Attorney"), intro14, intro15, new BasicDivider(),
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
        viewStateForm = new Button("View your states Mental Healthcare Power of Attorney instructions");
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
        Span content = new Span("FHIR advanced directive - Mental Healthcare successfully created!");

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
        PowerOfAttorneyMentalHealth poa = new PowerOfAttorneyMentalHealth();
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
        if (authDecision1.contains("To receive medical records")) {
            poa.setAuthorizeReleaseOfRecords(true);
        }
        if (authDecision2.contains("administration of any medications")) {
            poa.setAuthorizeMedicationAdminstration(true);
        }
        if (authDecision3.contains("hospitalization program")) {
            poa.setAuthorizeCommitIfNecessary(true);
        }
        if (authDecision4.contains("Other:")) {
            poa.setAuthorizeOtherMentalHealthActions(true);
            poa.setMentalHealthActionsList1(authOtherDecisionsField1.getValue());
            poa.setMentalHealthActionsList2(authOtherDecisionsField2.getValue());
            poa.setMentalHealthActionsList3(authOtherDecisionsField3.getValue());
        }

        poa.setDoNotAuthorizeActionList1(authException1Field.getValue());
        poa.setDoNotAuthorizeActionList2(authException2Field.getValue());

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

        PDFPOAMentalHealthHandler pdfHandler = new PDFPOAMentalHealthHandler(pdfSigningService);
        StreamResource res = pdfHandler.retrievePDFForm(poa, base64PatientInitials);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;
    }
    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent poaDirective = new Consent();
        poaDirective.setId("DNR-"+patient.getId());
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

        fhirConsentClient.createConsent(poaDirective);
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
}
