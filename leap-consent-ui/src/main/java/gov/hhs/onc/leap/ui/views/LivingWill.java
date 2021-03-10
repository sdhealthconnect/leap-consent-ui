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
import gov.hhs.onc.leap.ui.util.pdf.PDFLivingWillHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFPOAMentalHealthHandler;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;




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

    private RadioButtonGroup comfortCareOnly;
    private RadioButtonGroup comfortCareOnlyButNo;

    private RadioButtonGroup noCardioPulmonaryRecusitation;
    private RadioButtonGroup noArtificialFluidsOrFood;
    private RadioButtonGroup avoidTakingToHospital;

    private RadioButtonGroup ifPregnantSaveFetus;
    private RadioButtonGroup careUntilDoctorConcludesNoHope;
    private RadioButtonGroup prolongLifeToGreatestExtentPossible;

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
        setId("livingwillview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html("<p><b>GENERAL INSTRUCTIONS:</b> Use this form to make decisions now about your medical care if you are " +
                "ever in a terminal condition, a persistent vegetative state or an irreversible coma. You should talk to " +
                "your doctor about what these terms mean. The Living Will is your written directions to your health care power of attorney, "+
                "also referred to as your <b>agent</b>, your family, your physician, and any other person who might make medical care decisions for" +
                "you if you are unable to communicate yourself. It is a good idea to talk to your doctor and loved ones if you have questions about "+
                "the type of care you do or do not want.</p>" );


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
        Html intro2 = new Html("<p>Before you begin with the <b>Living Will</b> questionnaire we need to capture" +
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

        patientInitialsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"),intro2, new BasicDivider(), patientInitials, sigLayout);
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

        patientGeneralInfoLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"),intro3, new BasicDivider(),
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
        Html intro4 = new Html("<p>Some general statements about your health care choices are listed below. If you agree with one of" +
                "the statements, you should select that statement. Read all of these statements carefully BEFORE you " +
                "select your preferred statement. You can also write your own statement concerning life-sustaining " +
                "treatment and other matters relating to your health care. You may select any combination of " +
                "items 1, 2, 3 and 4, BUT if you 'select' item 5 the others will not be selected.</p>");

        comfortCareOnly = new RadioButtonGroup();
        comfortCareOnly.setItems("1. If I have a terminal condition I do not want my life to be prolonged, and I do not want lifesustaining " +
                "treatment, beyond comfort care, that would serve only to artificially delay the " +
                "moment of my death.");
        Html intro5 = new Html("<p><b>**Comfort care</b> is treatment given in an attempt to protect and enhance the " +
                "quality of life without artificially prolonging life.</p>");

        comfortCareOnlyButNo = new RadioButtonGroup();
        comfortCareOnlyButNo.setItems("2. If I am in a terminal condition or an irreversible coma or a persistent vegetative state that my " +
                "doctors reasonably feel to be irreversible or incurable, I do want the medical treatment " +
                "necessary to provide care that would keep me comfortable, but I DO NOT want the " +
                "following:");
        comfortCareOnlyButNo.addValueChangeListener(event -> {
           String s = (String)event.getValue();
           if (s != null && s.contains("I DO NOT want the following:")) {
               noArtificialFluidsOrFood.setVisible(true);
               noCardioPulmonaryRecusitation.setVisible(true);
               avoidTakingToHospital.setVisible(true);
           }
           else {
               noArtificialFluidsOrFood.setVisible(false);
               noCardioPulmonaryRecusitation.setVisible(false);
               avoidTakingToHospital.setVisible(false);
           }
        });

        noCardioPulmonaryRecusitation = new RadioButtonGroup();
        noCardioPulmonaryRecusitation.setItems("a. Cardiopulmonary resuscitation (CPR). For example: the use of drugs, electric " +
                "shock and artificial breathing.");
        noCardioPulmonaryRecusitation.setVisible(false);

        noArtificialFluidsOrFood = new RadioButtonGroup();
        noArtificialFluidsOrFood.setItems("b. Artificially administered food and fluids.");
        noArtificialFluidsOrFood.setVisible(false);

        avoidTakingToHospital = new RadioButtonGroup();
        avoidTakingToHospital.setItems("c. To be taken to a hospital if at all avoidable.");
        avoidTakingToHospital.setVisible(false);

        ifPregnantSaveFetus = new RadioButtonGroup();
        ifPregnantSaveFetus.setItems("3. Regardless of any other directions I have given in this Living Will, if I am known to be " +
                "pregnant, I do not want life-sustaining treatment withheld or withdrawn if it is possible that " +
                "the embryo/fetus will develop to the point of live birth with the continued application of lifesustaining " +
                "treatment.");
        if (consentSession.getConsentUser().getGender().equals("M")) {
            ifPregnantSaveFetus.setEnabled(false);
        }
        careUntilDoctorConcludesNoHope = new RadioButtonGroup();
        careUntilDoctorConcludesNoHope.setItems("4. Regardless of any other directions I have given in this Living Will, I do want the use of all " +
                "medical care necessary to treat my condition until my doctors reasonably conclude that my " +
                "condition is terminal or is irreversible and incurable or I am in a persistent vegetative state.");
        prolongLifeToGreatestExtentPossible = new RadioButtonGroup();
        prolongLifeToGreatestExtentPossible.setItems("5. I want my life to be prolonged to the greatest extent possible (If you select here, all others " +
                "will be unselected).");
        prolongLifeToGreatestExtentPossible.addValueChangeListener(event -> {
            String s = (String)event.getValue();
            if (s != null &&  s.contains("my life to be prolonged")) {
                //clear all others
                try {
                    comfortCareOnlyButNo.clear();
                    comfortCareOnly.clear();
                    noCardioPulmonaryRecusitation.clear();
                    noArtificialFluidsOrFood.clear();
                    avoidTakingToHospital.clear();
                    ifPregnantSaveFetus.clear();
                    careUntilDoctorConcludesNoHope.clear();
                }
                catch(Exception ex) {
                    log.error("Living Will clear instructions error "+ex.getMessage());
                }
            }
        });
        instructionsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"),intro4, new BasicDivider(),
                comfortCareOnly, intro5, comfortCareOnlyButNo, noCardioPulmonaryRecusitation, noArtificialFluidsOrFood, avoidTakingToHospital,
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
        Html intro6 = new Html("<p><b>PLEASE NOTE:</b> You can attach additional instructions on your medical care wishes that have not "+
                "been included in this Living Will form. Select A or B below. Be sure to include the attachment if you check B.</p>");

        additionalInstructionsButtonGroup = new RadioButtonGroup();
        additionalInstructionsButtonGroup.setItems("A. I HAVE NOT attached additional special instructions about End of Life Care I want.",
                "B. I HAVE attached additional special provisions or limitations about End of Life Care I want.");

        //todo create issue and task to attach additional instructions to the pdf that is generated either during this process or after

        additionalInstructionsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"),intro6, new BasicDivider(),
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
        Html intro11 = new Html("<p><b>MY SIGNATURE VERIFICATION FOR LIVING WILL</b></p>");

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

        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"), intro11, new BasicDivider(),
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
                "that this Living Will expresses their wishes and that they intend to adopt it at this time.</p>");

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

        patientUnableSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"), intro12, intro13, new BasicDivider(),
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
        Html intro15 = new Html("<p>I was present when this form was signed (or marked). The principal appeared to be of sound mind "+
                "and was not forced to sign this form.");

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

        witnessSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Living Will"), intro14, intro15, new BasicDivider(),
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
        viewStateForm = new Button("View your states Living Will instructions");
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
        Span content = new Span("FHIR advanced directive - Living Will successfully created!");

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
                forwardButton.setEnabled(true);
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
        gov.hhs.onc.leap.adr.model.LivingWill livingWill = new gov.hhs.onc.leap.adr.model.LivingWill();
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
        String prolongLife = (String)prolongLifeToGreatestExtentPossible.getValue();
        if (prolongLife != null && prolongLife.contains("my life to be prolonged")) {
            livingWill.setProlongLifeToGreatestExtentPossible(true);
        }
        else {
            String comfortOnly = (String) comfortCareOnly.getValue();
            if (comfortOnly != null && comfortOnly.contains("I do not want my life to be prolonged")) {
                livingWill.setComfortCareOnly(true);
            }
            String comfortAndNot = (String) comfortCareOnlyButNo.getValue();
            if (comfortAndNot != null && comfortAndNot.contains("terminal condition or an irreversible")) {
                livingWill.setComfortCareOnlyButNot(true);
                String noCPR = (String) noCardioPulmonaryRecusitation.getValue();
                if (noCPR != null && noCPR.contains("Cardiopulmonary resuscitation")) {
                    livingWill.setNoCardioPulmonaryRecusitation(true);
                }
                String noFluidsFood = (String) noArtificialFluidsOrFood.getValue();
                if (noFluidsFood != null && noFluidsFood.contains("Artificially administered")) {
                    livingWill.setNoArtificalFluidsFoods(true);
                }
                String noHospital = (String) avoidTakingToHospital.getValue();
                if (noHospital != null && noHospital.contains("taken to a hospital")) {
                    livingWill.setAvoidTakingToHospital(true);
                }
            }
            String pregnant = (String) ifPregnantSaveFetus.getValue();
            if (pregnant != null && pregnant.contains("pregnant")) {
                livingWill.setPregnantSaveFetus(true);
            }
            String noHope = (String) careUntilDoctorConcludesNoHope.getValue();
            if (noHope != null && noHope.contains("doctors reasonably conclude")) {
                livingWill.setCareUntilDoctorsConcludeNoHope(true);
            }
        }

        //additional instructions
        String addlInstructions = (String)additionalInstructionsButtonGroup.getValue();
        if (addlInstructions != null &&  addlInstructions.contains("I HAVE NOT attached")) {
            livingWill.setNoAdditionalInstructions(true);
        }
        else if (addlInstructions != null && addlInstructions.contains("I HAVE attached")) {
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

        poaDirective.setProvision(provision);

        fhirConsentClient.createConsent(poaDirective);
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
}
