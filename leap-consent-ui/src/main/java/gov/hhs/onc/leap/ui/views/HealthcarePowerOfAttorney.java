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
import gov.hhs.onc.leap.backend.ConsentUser;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.alejandro.PdfBrowserViewer;

import java.text.SimpleDateFormat;
import java.util.Date;

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




    public HealthcarePowerOfAttorney(@Autowired PDFSigningService PDFSigningService) {
        setId("healthcarepowerofattorney");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
        this.PDFSigningService = PDFSigningService;
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


        createInfoDialog();

        FlexBoxLayout content = new FlexBoxLayout(intro, patientInitialsLayout, patientGeneralInfoLayout, poaSelectionLayout,
                altSelectionLayout, autopsySelectionLayout, organDonationSelectionLayout, burialSelectionLayout);
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
        patientInitials.setWidth("250px");
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
        patientAddress1Field.setValue(consentUser.getStreetAddress1()+" "+consentUser.getStreetAddress1());
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
                "I choose the following person to act as my agent to make health care decisions for me.</p>");

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
                "I choose the following person to act as an alternate to make health care decisions for me if my "+
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

        institutionAgreementField = new TextField("Institution");
        institutionAgreementField.setVisible(false);

        whatTissuesButtonGroup = new RadioButtonGroup();
        whatTissuesButtonGroup.setLabel("What organs/tissues I choose to donate");
        whatTissuesButtonGroup.setItems("Whole body", "Any needed parts or organs","Specifc parts or organs only");
        whatTissuesButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        whatTissuesButtonGroup.setVisible(false);

        specificOrgansField = new TextField("Specific parts or organs only");
        specificOrgansField.setVisible(false);

        pouOrganDonationButtonGroup = new RadioButtonGroup();
        pouOrganDonationButtonGroup.setLabel("I am donating organs/tissue for");
        pouOrganDonationButtonGroup.setItems("Any legally authorized purpose","Transplant or therapeutic purposes only",
                "Research only","Other");
        pouOrganDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        pouOrganDonationButtonGroup.setVisible(false);

        otherPurposesField = new TextField("Other Purposes");
        otherPurposesField.setVisible(false);

        organizationOrganDonationButtonGroup = new RadioButtonGroup();
        organizationOrganDonationButtonGroup.setLabel("The organization or person I want my organs/tissue to go to are");
        organizationOrganDonationButtonGroup.setItems("My List", "Any that my agent chooses");
        organizationOrganDonationButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
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
}
