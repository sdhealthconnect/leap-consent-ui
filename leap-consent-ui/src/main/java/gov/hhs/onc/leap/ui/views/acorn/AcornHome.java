package gov.hhs.onc.leap.ui.views.acorn;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRQuestionnaire;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRQuestionnaireResponse;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.backend.model.SDOHOrganization;
import gov.hhs.onc.leap.backend.repository.SDOHOrganizationRepository;
import gov.hhs.onc.leap.sdoh.data.ACORNDisplayData;
import gov.hhs.onc.leap.sdoh.model.QuestionnaireItem;
import gov.hhs.onc.leap.sdoh.model.QuestionnaireSection;
import gov.hhs.onc.leap.sdoh.model.SDOHNeed;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.signature.PDFSigningService;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.components.navigation.BasicDivider;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Right;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.layout.size.Vertical;
import gov.hhs.onc.leap.ui.util.IconSize;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import gov.hhs.onc.leap.ui.util.pdf.PDFACORNHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFPatientPrivacyHandler;
import gov.hhs.onc.leap.ui.views.ViewFrame;
import gov.va.idiq.himms2022.BasicSDCQuestionnaireProcessor;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@PageTitle("ACORN Project - Home")
@Route(value = "acornhome", layout = MainLayout.class)
public class AcornHome extends ViewFrame {

    private ConsentSession consentSession;
    private ConsentUser consentUser;
    private Button beginQuestionnaire;

    private Button returnButton;
    private Button forwardButton;
    private Button declineButton;
    private int questionPosition = 0;

    private String currentType = "Housing";

    private FlexBoxLayout introPage;

    //questionnaire layouts
    private FlexBoxLayout qLivingSituation1Layout;
    private FlexBoxLayout qLivingSituation2Layout;
    private FlexBoxLayout qFoodLayout1;
    private FlexBoxLayout qFoodLayout2;
    private FlexBoxLayout qTransportationAccessLayout1;
    private FlexBoxLayout qUtilitiesLayout;
    private FlexBoxLayout qSafetyLayout1;
    private FlexBoxLayout qSafetyLayout2;
    private FlexBoxLayout qSafetyLayout3;
    private FlexBoxLayout qSafetyLayout4;
    private FlexBoxLayout qFinancialStrainLayout;
    private FlexBoxLayout qEmploymentLayout;
    private FlexBoxLayout qFamilyCommunitySupportLayout1;
    private FlexBoxLayout qFamilyCommunitySupportLayout2;
    private FlexBoxLayout qEducationLayout1;
    private FlexBoxLayout qEducationLayout2;
    private FlexBoxLayout qLegal;


    //referral select layouts
    private FlexBoxLayout housingInsecurityLayout;
    private FlexBoxLayout foodSecurityLayout;
    private FlexBoxLayout utilityNeedsLayout;
    private FlexBoxLayout transportationAccessLayout;
    private FlexBoxLayout personalSafetyLayout;
    private FlexBoxLayout socialSupportLayout;
    private FlexBoxLayout employmentEducationLayout;
    private FlexBoxLayout legalSupportLayout;
    private FlexBoxLayout signatureRequirements;

    private Grid foodgrid;
    private Grid housinggrid;
    private Grid transportationgrid;
    private Grid utilitygrid;
    private Grid socialgrid;
    private Grid personalgrid;
    private Grid employgrid;
    private Grid legalgrid;

    private Dialog dialog;
    private SignaturePad signature;
    private byte[] base64Signature;
    private Dialog docDialog;
    private byte[] consentPDFAsByteArray;

    private boolean questionnaireComplete = false;

    private RadioButtonGroup<QuestionnaireItem> q1Group;
    private CheckboxGroup<QuestionnaireItem> q2Group;
    private Checkbox noneOfAboveBox;
    private RadioButtonGroup<QuestionnaireItem> q3Group;
    private RadioButtonGroup<QuestionnaireItem> q4Group;
    private RadioButtonGroup<QuestionnaireItem> q5Group;
    private RadioButtonGroup<QuestionnaireItem> q6Group;
    private RadioButtonGroup<QuestionnaireItem> q7Group;
    private RadioButtonGroup<QuestionnaireItem> q8Group;
    private RadioButtonGroup<QuestionnaireItem> q9Group;
    private RadioButtonGroup<QuestionnaireItem> q10Group;
    private RadioButtonGroup<QuestionnaireItem> q11Group;
    private RadioButtonGroup<QuestionnaireItem> q12Group;
    private RadioButtonGroup<QuestionnaireItem> q13Group;
    private RadioButtonGroup<QuestionnaireItem> q14Group;
    private RadioButtonGroup<QuestionnaireItem> q15Group;
    private RadioButtonGroup<QuestionnaireItem> q16Group;
    private RadioButtonGroup<QuestionnaireItem> q17Group;

    private List<QuestionnaireItem> answerList;


    private List<SDOHOrganization> selectedSDOHOrganizations = new ArrayList<>();

    private ACORNDisplayData displayData;

    private Questionnaire acornQuestionnaire;
    private QuestionnaireResponse acornQuestionnaireResponse;
    private QuestionnaireResponse acornQuestionnaireResponseFinal;
    private List<String> orgsWithConsentGranted;

    private List<SDOHNeed> sdohNeeds;

    private String sdohActionNav = "FORWARD";

    @Autowired
    private FHIRQuestionnaire fhirQuestionnaire;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @Autowired
    private SDOHOrganizationRepository sdohOrganizationRepository;

    @Autowired
    private PDFSigningService pdfSigningService;

    @Autowired
    private FHIRConsent fhirConsentClient;

    public AcornHome() { setId("acornhome"); }

    @PostConstruct
    public void setup() {
        setId("acornhome");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        this.displayData = new ACORNDisplayData();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private void getAcornQuestionnaire() {
        acornQuestionnaire = fhirQuestionnaire.getQuestionnaire("acorn-himss2022-demonstration");
    }

    private Component createViewContent() {
        //Html intro = new Html("<p><b>Accessing Circumstances Offering Resources for Needs</b></p>");
        getAcornQuestionnaire();
        createIntroPage();
        createHousingAction();
        createFoodSecurityAction();
        createUtilityAction();
        createTransportationAction();
        createPersonalSafetyAction();
        createSocialSupportAction();
        createEmploymentEductionAction();
        createLegalSupportAction();
        createSignatureRequirements();
        createLivingSituation1();
        createLivingSituation2();
        createFood1();
        createFood2();
        createTransportation();
        createUtilitiesNeeds();
        createPersonalSafety1();
        createPersonalSafety2();
        createPersonalSafety3();
        createPersonalSafety4();
        createFinancialStrain();
        createEmployment();
        createEducation1();
        createEducation2();
        createFamilySupport1();
        createFamilySupport2();
        createLegalSupport();

        FlexBoxLayout content = new FlexBoxLayout(introPage, qLivingSituation1Layout,
                qLivingSituation2Layout, qFoodLayout1, qFoodLayout2, qTransportationAccessLayout1,
                qUtilitiesLayout, qSafetyLayout1, qSafetyLayout2, qSafetyLayout3, qSafetyLayout4,
                qFinancialStrainLayout, qEmploymentLayout, qEducationLayout1, qEducationLayout2,
                qFamilyCommunitySupportLayout1, qFamilyCommunitySupportLayout2, qLegal,
                housingInsecurityLayout, foodSecurityLayout, utilityNeedsLayout, transportationAccessLayout, personalSafetyLayout, socialSupportLayout, employmentEducationLayout, legalSupportLayout, signatureRequirements);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private void createIntroPage() {

        String fullFormPath = UIUtils.IMG_PATH + "logos";
        Image logo = UIUtils.createImage(fullFormPath,"acornproject.png", "");
        H1 header = new H1("Veterans Facing Health-Related Social Needs");
        Html intro = new Html("<p>This <b>HIMSS 2022</b> interoperability demonstration of " +
                "US Department of Veterans Affairs <b>ACORN initiative</b> utilizes a <b>FHIR Questionnaire</b>" +
                " to screen Veterans for " +
                "non-clinical needs to provide resources at the point of clinical care. The assessment " +
                "currently screens for the following nine domains of health-related social needs: food, " +
                "housing security, utility, transportation, legal, " +
                "educational, employment needs, " +
                "personal safety and social support.</p>");
        Html intro2 = new Html("<p>Various social determinants interact with other behavioral, environmental, " +
                "and economic factors to contribute up to <b>80% of overall health outcomes.</b>  Addressing a Veteran's " +
                "unmet health-related social needs can have a positive impact on their health and quality of life.</p>");
        introPage = new FlexBoxLayout(logo, header, intro, intro2);
        introPage.setBoxSizing(BoxSizing.BORDER_BOX);
        introPage.setHeightFull();
        introPage.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        introPage.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        introPage.setMargin(Horizontal.AUTO);
        introPage.setMaxWidth("840px");
        //content.setPadding(Uniform.RESPONSIVE_L);
    }

    private void createFoodSecurityAction() {
        Html foodIntro = new Html("<p>Based on your responses in the <b>Food</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        foodgrid = createGrid("Food Security");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        foodSecurityLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Food Security"),foodIntro, new BasicDivider(), foodgrid, consent);
        foodSecurityLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        foodSecurityLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        foodSecurityLayout.setHeightFull();
        foodSecurityLayout.setBackgroundColor("white");
        foodSecurityLayout.setShadow(Shadow.S);
        foodSecurityLayout.setBorderRadius(BorderRadius.S);
        foodSecurityLayout.getStyle().set("margin-bottom", "10px");
        foodSecurityLayout.getStyle().set("margin-right", "10px");
        foodSecurityLayout.getStyle().set("margin-left", "10px");
        foodSecurityLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        foodSecurityLayout.setVisible(false);
    }

    private void createTransportationAction() {
        Html transportationIntro = new Html("<p>Based on your responses in the <b>Transportation Access</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        transportationgrid = createGrid("Transportation Access");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        transportationAccessLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Transportation Access"),transportationIntro, new BasicDivider(), transportationgrid, consent);
        transportationAccessLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        transportationAccessLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        transportationAccessLayout.setHeightFull();
        transportationAccessLayout.setBackgroundColor("white");
        transportationAccessLayout.setShadow(Shadow.S);
        transportationAccessLayout.setBorderRadius(BorderRadius.S);
        transportationAccessLayout.getStyle().set("margin-bottom", "10px");
        transportationAccessLayout.getStyle().set("margin-right", "10px");
        transportationAccessLayout.getStyle().set("margin-left", "10px");
        transportationAccessLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        transportationAccessLayout.setVisible(false);
    }

    private void createHousingAction() {
        Html housingIntro = new Html("<p>Based on your responses in the <b>Housing</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        housinggrid = createGrid("Housing Insecurity");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        housingInsecurityLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Housing Insecurity"),housingIntro, new BasicDivider(), housinggrid, consent);
        housingInsecurityLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        housingInsecurityLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        housingInsecurityLayout.setHeightFull();
        housingInsecurityLayout.setBackgroundColor("white");
        housingInsecurityLayout.setShadow(Shadow.S);
        housingInsecurityLayout.setBorderRadius(BorderRadius.S);
        housingInsecurityLayout.getStyle().set("margin-bottom", "10px");
        housingInsecurityLayout.getStyle().set("margin-right", "10px");
        housingInsecurityLayout.getStyle().set("margin-left", "10px");
        housingInsecurityLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        housingInsecurityLayout.setVisible(false);
    }

    private void createUtilityAction() {
        Html utilityIntro = new Html("<p>Based on your responses in the <b>Utility Needs</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        utilitygrid = createGrid("Utility Needs");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        utilityNeedsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Utility Needs"),utilityIntro, new BasicDivider(), utilitygrid, consent);
        utilityNeedsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        utilityNeedsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        utilityNeedsLayout.setHeightFull();
        utilityNeedsLayout.setBackgroundColor("white");
        utilityNeedsLayout.setShadow(Shadow.S);
        utilityNeedsLayout.setBorderRadius(BorderRadius.S);
        utilityNeedsLayout.getStyle().set("margin-bottom", "10px");
        utilityNeedsLayout.getStyle().set("margin-right", "10px");
        utilityNeedsLayout.getStyle().set("margin-left", "10px");
        utilityNeedsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        utilityNeedsLayout.setVisible(false);
    }

    private void createPersonalSafetyAction() {
        Html personalIntro = new Html("<p>Based on your responses in the <b>Personal Safety</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        personalgrid = createGrid("Personal Safety");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        personalSafetyLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Personal Safety"),personalIntro, new BasicDivider(), personalgrid, consent);
        personalSafetyLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        personalSafetyLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        personalSafetyLayout.setHeightFull();
        personalSafetyLayout.setBackgroundColor("white");
        personalSafetyLayout.setShadow(Shadow.S);
        personalSafetyLayout.setBorderRadius(BorderRadius.S);
        personalSafetyLayout.getStyle().set("margin-bottom", "10px");
        personalSafetyLayout.getStyle().set("margin-right", "10px");
        personalSafetyLayout.getStyle().set("margin-left", "10px");
        personalSafetyLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        personalSafetyLayout.setVisible(false);
    }

    private void createSocialSupportAction() {
        Html socialIntro = new Html("<p>Based on your responses in the <b>Social Support</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        socialgrid = createGrid("Social Support");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        socialSupportLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Social Support"),socialIntro, new BasicDivider(), socialgrid, consent);
        socialSupportLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        socialSupportLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        socialSupportLayout.setHeightFull();
        socialSupportLayout.setBackgroundColor("white");
        socialSupportLayout.setShadow(Shadow.S);
        socialSupportLayout.setBorderRadius(BorderRadius.S);
        socialSupportLayout.getStyle().set("margin-bottom", "10px");
        socialSupportLayout.getStyle().set("margin-right", "10px");
        socialSupportLayout.getStyle().set("margin-left", "10px");
        socialSupportLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        socialSupportLayout.setVisible(false);
    }

    private void createEmploymentEductionAction() {
        Html employIntro = new Html("<p>Based on your responses in the <b>Employment and Education</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        employgrid = createGrid("Employment and Education");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        employmentEducationLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Employment and Education"),employIntro, new BasicDivider(), employgrid, consent);
        employmentEducationLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        employmentEducationLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        employmentEducationLayout.setHeightFull();
        employmentEducationLayout.setBackgroundColor("white");
        employmentEducationLayout.setShadow(Shadow.S);
        employmentEducationLayout.setBorderRadius(BorderRadius.S);
        employmentEducationLayout.getStyle().set("margin-bottom", "10px");
        employmentEducationLayout.getStyle().set("margin-right", "10px");
        employmentEducationLayout.getStyle().set("margin-left", "10px");
        employmentEducationLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        employmentEducationLayout.setVisible(false);
    }

    private void createLegalSupportAction() {
        Html legalIntro = new Html("<p>Based on your responses in the <b>Legal Support</b> section of the ACORN questionnaire we have identified organizations in your area that may be able to help.</>");
        legalgrid = createGrid("Legal Support");
        Html consent = new Html("<p>Highlight the organization that best fits your needs " +
                "and click on the \"Accept\" button to authorize <b>U.S. Department of Veterans Affairs</b> to release your contact information while scheduling a referral.</p>");

        legalSupportLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Legal Support"),legalIntro, new BasicDivider(), legalgrid, consent);
        legalSupportLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        legalSupportLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        legalSupportLayout.setHeightFull();
        legalSupportLayout.setBackgroundColor("white");
        legalSupportLayout.setShadow(Shadow.S);
        legalSupportLayout.setBorderRadius(BorderRadius.S);
        legalSupportLayout.getStyle().set("margin-bottom", "10px");
        legalSupportLayout.getStyle().set("margin-right", "10px");
        legalSupportLayout.getStyle().set("margin-left", "10px");
        legalSupportLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        legalSupportLayout.setVisible(false);
    }

    private void createSignatureRequirements() {
        Html eSignLabel = new Html("<p>This last step will capture your signature and create a <b>human readable pdf</b> of this consent for you approval.</p>");
        Button eSignButton = new Button("eSign Consent and Submit");
        eSignButton.addClickListener(event -> {
            dialog = createSignatureDialog();
            dialog.open();
        });

        signatureRequirements = new FlexBoxLayout(createHeader(VaadinIcon.PENCIL, getTranslation("sharePatient-signature")), eSignLabel, eSignButton, new BasicDivider());
        signatureRequirements.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        signatureRequirements.setBoxSizing(BoxSizing.BORDER_BOX);
        signatureRequirements.setHeightFull();
        signatureRequirements.setBackgroundColor("white");
        signatureRequirements.setShadow(Shadow.S);
        signatureRequirements.setBorderRadius(BorderRadius.S);
        signatureRequirements.getStyle().set("margin-bottom", "10px");
        signatureRequirements.getStyle().set("margin-right", "10px");
        signatureRequirements.getStyle().set("margin-left", "10px");
        signatureRequirements.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        signatureRequirements.setVisible(false);
    }

    private Component getFooter() {
        beginQuestionnaire = new Button("Begin Questionnaire");
        beginQuestionnaire.setIcon(new Icon(VaadinIcon.ARROW_CIRCLE_RIGHT));
        beginQuestionnaire.setIconAfterText(true);
        beginQuestionnaire.addClickListener(event -> {
            questionPosition++;
            sdohActionNav = "FORWARD";
            evalNavigation();
        });
        returnButton = new Button("Back", new Icon(VaadinIcon.BACKWARDS));
        returnButton.addClickListener(event -> {
            questionPosition--;
            sdohActionNav = "BACKWARD";
            evalNavigation();
        });
        returnButton.setVisible(false);
        forwardButton = new Button("Accept", new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
            questionPosition++;
            sdohActionNav = "FORWARD";
            evalNavigation();
        });
        forwardButton.setVisible(false);
        declineButton = new Button("Decline", new Icon(VaadinIcon.FORWARD));
        declineButton.setIconAfterText(true);
        declineButton.addClickListener(event -> {
            evalDecline();
            questionPosition++;
            sdohActionNav = "FORWARD";
            evalNavigation();
        });
        declineButton.setVisible(false);

        HorizontalLayout footer = new HorizontalLayout(beginQuestionnaire, returnButton, forwardButton, declineButton);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(true);
        footer.setSpacing(true);
        return footer;
    }

    private Grid createGrid(String type) {
        ListDataProvider<SDOHOrganization> dataProvider = DataProvider.ofCollection(getSDOHOrganizations(type));

        Grid grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();

        grid.addColumn(new ComponentRenderer<>(this::createSDOHName))
                .setHeader(getTranslation("Program Name"))
                .setWidth("220px");
        grid.addColumn(new ComponentRenderer<>(this::createCity))
                .setHeader(getTranslation("City"))
                .setWidth("70px");
        grid.addColumn(new ComponentRenderer<>(this::createState))
                .setHeader(getTranslation("State"))
                .setWidth("50px");
        grid.addColumn(new ComponentRenderer<>(this::createPhone))
                .setHeader(getTranslation("Phone #"))
                .setWidth("90px");
        grid.addColumn(new ComponentRenderer<>(this::createHoursOfOperations))
                .setHeader(getTranslation("Operating Hours"))
                .setWidth("80px");
        grid.addColumn(new ComponentRenderer<>(this::createEmailAddress))
                .setHeader(getTranslation("Website"))
                .setWidth("150px");

        return grid;
    }

    private Component createSDOHName(SDOHOrganization org) {
        ListItem item = new ListItem(org.getProgramname());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createAddress(SDOHOrganization org) {
        ListItem item = new ListItem(org.getAddress());
        item.setPadding(Vertical.XS);
        return item;
    }
    private Component createCity(SDOHOrganization org) {
        ListItem item = new ListItem(org.getCity());
        item.setPadding(Vertical.XS);
        return item;
    }
    private Component createState(SDOHOrganization org) {
        ListItem item = new ListItem(org.getState());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createPhone(SDOHOrganization org) {
        ListItem item = new ListItem(org.getPhonenumber());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createHoursOfOperations(SDOHOrganization org) {
        ListItem item = new ListItem(org.getHoursofoperation(), org.getDaysopen());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createEmailAddress(SDOHOrganization org) {
        ListItem item = new ListItem(org.getWebsite());
        item.setPadding(Vertical.XS);
        return item;
    }
    
    
    private Collection<SDOHOrganization> getSDOHOrganizations(String type) {
        String state = "AZ";
        String county = "Santa Cruz";
        List<SDOHOrganization> sdohOrganizationList = sdohOrganizationRepository.getSDOHOrganizationByTypeAndStateAndCounty(type, state, county);
        return sdohOrganizationList;
    }

    private void evalNavigation() {
        if (!questionnaireComplete) {
            forwardButton.setText("Next");
            disableQuestionnaireComponents();
            disableActionComponents();
            switch (questionPosition) {
                case 0:
                    beginQuestionnaire.setVisible(true);
                    introPage.setVisible(true);
                    break;
                case 1:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qFoodLayout1.setVisible(true);
                    break;
                case 2:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qFoodLayout2.setVisible(true);
                    break;
                case 3:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qLivingSituation1Layout.setVisible(true);
                    break;
                case 4:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qLivingSituation2Layout.setVisible(true);
                    break;
                case 5:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qUtilitiesLayout.setVisible(true);
                    break;
                case 6:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qTransportationAccessLayout1.setVisible(true);
                    break;
                case 7:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qSafetyLayout1.setVisible(true);
                    break;
                case 8:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qSafetyLayout2.setVisible(true);
                    break;
                case 9:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qSafetyLayout3.setVisible(true);
                    break;
                case 10:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qSafetyLayout4.setVisible(true);
                    break;
                case 11:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qFinancialStrainLayout.setVisible(true);
                    break;
                case 12:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qEmploymentLayout.setVisible(true);
                    break;
                case 13:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qEducationLayout1.setVisible(true);
                    break;
                case 14:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qEducationLayout2.setVisible(true);
                    break;
                case 15:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qFamilyCommunitySupportLayout1.setVisible(true);
                    break;
                case 16:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qFamilyCommunitySupportLayout2.setVisible(true);
                    break;
                case 17:
                    returnButton.setVisible(true);
                    forwardButton.setVisible(true);
                    qLegal.setVisible(true);
                    break;
                case 18:
                    Notification notification = Notification.show("Congrats! You've completed the ACORN questionnaire.");
                    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    notification.setPosition(Notification.Position.MIDDLE);
                    processAnswers();
                    forwardButton.setText("Accept");
                    questionnaireComplete = true;
                    questionPosition = 0;
                    displayFoodSecurity();
                    break;
            }
        }
        else {
            forwardButton.setText("Accept");
            switch (questionPosition) {
                case 0:
                    displayFoodSecurity();
                    break;
                case 1:
                    displayHousingInsecurity();
                    break;
                case 2:
                    displayUtilityNeeds();
                    break;
                case 3:
                    displayTransportationAccess();
                    break;
                case 4:
                    displayPersonalSafety();
                    break;
                case 5:
                    displaySocialSupport();
                    break;
                case 6:
                    displayEmploymentEducation();
                    break;
                case 7:
                    displayLegalSupport();
                    break;
                case 8:
                    disableQuestionnaireComponents();
                    disableActionComponents();
                    beginQuestionnaire.setVisible(false);
                    returnButton.setVisible(true);
                    forwardButton.setVisible(false);
                    declineButton.setVisible(false);
                    signatureRequirements.setVisible(true);
                    break;
            }
        }
    }

    private void displayHousingInsecurity() {
        if (isSDOHNeeded("housing-security-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            housingInsecurityLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void displayFoodSecurity() {
        if (isSDOHNeeded("food-security-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            foodSecurityLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }
    private void displayUtilityNeeds() {
        if (isSDOHNeeded("utility-access-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need") || isSDOHNeeded("housing-security-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            utilityNeedsLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void displayTransportationAccess() {
        if (isSDOHNeeded("transportation-access-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need") || isSDOHNeeded("housing-security-need") || isSDOHNeeded("utility-access-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            transportationAccessLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void displayPersonalSafety() {
        if (isSDOHNeeded("personal-safety-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need") || isSDOHNeeded("housing-security-need") ||
                    isSDOHNeeded("utility-access-need") || isSDOHNeeded("transportation-access-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            personalSafetyLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void displaySocialSupport() {
        if (isSDOHNeeded("social-support-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need") || isSDOHNeeded("housing-security-need") ||
                    isSDOHNeeded("utility-access-need") || isSDOHNeeded("transportation-access-need") ||
                    isSDOHNeeded("personal-safety-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            socialSupportLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void displayEmploymentEducation() {
        if (isSDOHNeeded("employment-and-education-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need") || isSDOHNeeded("housing-security-need") ||
                    isSDOHNeeded("utility-access-need") || isSDOHNeeded("transportation-access-need") ||
                    isSDOHNeeded("personal-safety-need") || isSDOHNeeded("social-support-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            employmentEducationLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void displayLegalSupport() {
        if (isSDOHNeeded("legal-support-need")) {
            disableQuestionnaireComponents();
            disableActionComponents();
            if (isSDOHNeeded("food-security-need") || isSDOHNeeded("housing-security-need") ||
                    isSDOHNeeded("utility-access-need") || isSDOHNeeded("transportation-access-need") ||
                    isSDOHNeeded("personal-safety-need") || isSDOHNeeded("social-support-need") ||
                    isSDOHNeeded("employment-and-education-need")) {
                returnButton.setVisible(true);
            }
            forwardButton.setVisible(true);
            declineButton.setVisible(true);
            legalSupportLayout.setVisible(true);
        }
        else {
            if (sdohActionNav.equals("FORWARD")) {
                questionPosition++;
                if (questionPosition > 8) questionPosition = 8;
                evalNavigation();
            }
            else {
                questionPosition--;
                if (questionPosition < 0) questionPosition = 0;
                evalNavigation();
            }
        }
    }

    private void disableQuestionnaireComponents() {
        beginQuestionnaire.setVisible(false);
        returnButton.setVisible(false);
        forwardButton.setVisible(false);
        declineButton.setVisible(false);
        introPage.setVisible(false);
        qLivingSituation1Layout.setVisible(false);
        qLivingSituation2Layout.setVisible(false);
        qFoodLayout1.setVisible(false);
        qFoodLayout2.setVisible(false);
        qTransportationAccessLayout1.setVisible(false);
        qUtilitiesLayout.setVisible(false);
        qSafetyLayout1.setVisible(false);
        qSafetyLayout2.setVisible(false);
        qSafetyLayout3.setVisible(false);
        qSafetyLayout4.setVisible(false);
        qFinancialStrainLayout.setVisible(false);
        qEmploymentLayout.setVisible(false);
        qEducationLayout1.setVisible(false);
        qEducationLayout2.setVisible(false);
        qFamilyCommunitySupportLayout1.setVisible(false);
        qFamilyCommunitySupportLayout2.setVisible(false);
        qLegal.setVisible(false);
    }

    private void disableActionComponents() {
        beginQuestionnaire.setVisible(false);
        returnButton.setVisible(false);
        forwardButton.setVisible(false);
        declineButton.setVisible(false);
        housingInsecurityLayout.setVisible(false);
        foodSecurityLayout.setVisible(false);
        utilityNeedsLayout.setVisible(false);
        transportationAccessLayout.setVisible(false);
        personalSafetyLayout.setVisible(false);
        socialSupportLayout.setVisible(false);
        employmentEducationLayout.setVisible(false);
        legalSupportLayout.setVisible(false);
        signatureRequirements.setVisible(false);
    }

    private void evalDecline() {
        if (!questionnaireComplete) {

        }
        else {
            switch (questionPosition) {
                case 0:
                    foodgrid.getSelectionModel().deselectAll();
                    break;
                case 1:
                    housinggrid.getSelectionModel().deselectAll();
                    break;
                case 2:
                    utilitygrid.getSelectionModel().deselectAll();
                    break;
                case 3:
                    transportationgrid.getSelectionModel().deselectAll();
                    break;
                case 4:
                    personalgrid.getSelectionModel().deselectAll();
                    break;
                case 5:
                    socialgrid.getSelectionModel().deselectAll();
                    break;
                case 6:
                    employgrid.getSelectionModel().deselectAll();
                    break;
                case 7:
                    legalgrid.getSelectionModel().deselectAll();
                    break;
            }
        }
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

    private Dialog createSignatureDialog() {
        signature = new SignaturePad();
        signature.setHeight("100px");
        signature.setWidth("400px");
        signature.setPenColor("#2874A6");

        Button saveSig = new Button("Done");
        saveSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveSig.addClickListener(event -> {
            base64Signature = signature.getImageBase64();
            //todo create fhir consent resource and pdf for review in flow and final submittal of consent
            dialog.close();
            getHumanReadable();
            docDialog.open();

        });
        Button cancelSign = new Button("Cancel");
        cancelSign.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CLOSE));
        cancelSign.addClickListener(event -> {
            dialog.close();
        });
        Html signHere = new Html("<p><b>Sign Here</b</p>");
        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.add(cancelSign, saveSig);
        hLayout.setAlignItems(FlexComponent.Alignment.END);


        Dialog dialog = new Dialog();
        dialog.setHeight("250px");
        dialog.setWidth("450px");
        dialog.setCloseOnOutsideClick(true);
        dialog.setCloseOnEsc(true);
        dialog.setResizable(true);

        dialog.add(signHere, signature, hLayout);

        return dialog;
    }

    private void getHumanReadable() {
        getSelectedOrganizations();
        StreamResource streamResource = setFieldsCreatePDF();
        if (streamResource == null) {
            return;
        }
        docDialog = new Dialog();
        docDialog.setModal(true);

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");


        Button closeButton = new Button(getTranslation("sharePatient-cancel"));
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));
        closeButton.addClickListener(event -> {
            docDialog.close();
        });

        Button acceptButton = new Button(getTranslation("sharePatient-accept_and_submit"));
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            docDialog.close();
            //createQuestionnaireResponse();
            createFHIRConsent();
            //createFHIRProvenance();
            successNotification();
            //todo test for fhir consent create success
            //resetQuestionNavigation();
            UI.getCurrent().navigate("consentdocumentview");
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
        PDFACORNHandler pdfHandler = new PDFACORNHandler(pdfSigningService);
        getSelectedOrganizations();
        StreamResource res = pdfHandler.updateAndRetrievePDFForm("acorn", selectedSDOHOrganizations, base64Signature);
        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return  res;
    }

    private void getSelectedOrganizations() {
        selectedSDOHOrganizations = new ArrayList<>();
        Optional<SDOHOrganization> ocd = housinggrid.getSelectionModel().getFirstSelectedItem();
        if (ocd.isPresent()) {
            SDOHOrganization housing = ocd.get();
            selectedSDOHOrganizations.add(housing);
        }
        Optional<SDOHOrganization> ocd2 = foodgrid.getSelectionModel().getFirstSelectedItem();
        if (ocd2.isPresent()) {
            SDOHOrganization food = ocd2.get();
            selectedSDOHOrganizations.add(food);
        }
        Optional<SDOHOrganization> ocd3 = utilitygrid.getSelectionModel().getFirstSelectedItem();
        if (ocd3.isPresent()) {
            SDOHOrganization utility = ocd3.get();
            selectedSDOHOrganizations.add(utility);
        }
        Optional<SDOHOrganization> ocd4 = transportationgrid.getSelectionModel().getFirstSelectedItem();
        if (ocd4.isPresent()) {
            SDOHOrganization transportation = ocd4.get();
            selectedSDOHOrganizations.add(transportation);
        }
        Optional<SDOHOrganization> ocd5 = personalgrid.getSelectionModel().getFirstSelectedItem();
        if (ocd5.isPresent()) {
            SDOHOrganization personal = ocd5.get();
            selectedSDOHOrganizations.add(personal);
        }
        Optional<SDOHOrganization> ocd6 = socialgrid.getSelectionModel().getFirstSelectedItem();
        if (ocd6.isPresent()) {
            SDOHOrganization social = ocd6.get();
            selectedSDOHOrganizations.add(social);
        }
        Optional<SDOHOrganization> ocd7 = employgrid.getSelectionModel().getFirstSelectedItem();
        if (ocd7.isPresent()) {
            SDOHOrganization employ = ocd7.get();
            selectedSDOHOrganizations.add(employ);
        }
        Optional<SDOHOrganization> ocd8 = legalgrid.getSelectionModel().getFirstSelectedItem();
        if (ocd8.isPresent()) {
            SDOHOrganization legal = ocd8.get();
            selectedSDOHOrganizations.add(legal);
        }
    }

    private void createLivingSituation1() {
        Html domainIntro = new Html(displayData.getHousingInsecurityInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "1", "Housing Insecurity");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q1Group = new RadioButtonGroup<QuestionnaireItem>();
        q1Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q1Group.setItems(selections);
        q1Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qLivingSituation1Layout = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q1Group);
        qLivingSituation1Layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qLivingSituation1Layout.setBoxSizing(BoxSizing.BORDER_BOX);
        qLivingSituation1Layout.setHeightFull();
        qLivingSituation1Layout.setBackgroundColor("white");
        qLivingSituation1Layout.setShadow(Shadow.S);
        qLivingSituation1Layout.setBorderRadius(BorderRadius.S);
        qLivingSituation1Layout.getStyle().set("margin-bottom", "10px");
        qLivingSituation1Layout.getStyle().set("margin-right", "10px");
        qLivingSituation1Layout.getStyle().set("margin-left", "10px");
        qLivingSituation1Layout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qLivingSituation1Layout.setVisible(false);
    }

    private void createLivingSituation2() {
        Html domainIntro = new Html(displayData.getHousingInsecurityInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "2", "Housing Insecurity");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q2Group = new CheckboxGroup<QuestionnaireItem>();
        q2Group.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        q2Group.setItems(selections);
        q2Group.setItemLabelGenerator(questionnaireItem -> questionnaireItem.getDisplay());
        q2Group.addValueChangeListener(event -> {
           if (event.getValue().size() > 0) {
               noneOfAboveBox.setValue(false);
           }
        });

        noneOfAboveBox = new Checkbox("None of the above");
        noneOfAboveBox.addValueChangeListener(event ->{
           if (event.getValue()) {
               q2Group.deselectAll();
               noneOfAboveBox.setValue(true);
           }
        });


        qLivingSituation2Layout = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q2Group, noneOfAboveBox);
        qLivingSituation2Layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qLivingSituation2Layout.setBoxSizing(BoxSizing.BORDER_BOX);
        qLivingSituation2Layout.setHeightFull();
        qLivingSituation2Layout.setBackgroundColor("white");
        qLivingSituation2Layout.setShadow(Shadow.S);
        qLivingSituation2Layout.setBorderRadius(BorderRadius.S);
        qLivingSituation2Layout.getStyle().set("margin-bottom", "10px");
        qLivingSituation2Layout.getStyle().set("margin-right", "10px");
        qLivingSituation2Layout.getStyle().set("margin-left", "10px");
        qLivingSituation2Layout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qLivingSituation2Layout.setVisible(false);
    }

    private void createFood1() {
        Html domainIntro = new Html(displayData.getFoodSecurityInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "3", "Food Security");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q3Group = new RadioButtonGroup<QuestionnaireItem>();
        q3Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q3Group.setItems(selections);
        q3Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qFoodLayout1 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q3Group);
        qFoodLayout1.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qFoodLayout1.setBoxSizing(BoxSizing.BORDER_BOX);
        qFoodLayout1.setHeightFull();
        qFoodLayout1.setBackgroundColor("white");
        qFoodLayout1.setShadow(Shadow.S);
        qFoodLayout1.setBorderRadius(BorderRadius.S);
        qFoodLayout1.getStyle().set("margin-bottom", "10px");
        qFoodLayout1.getStyle().set("margin-right", "10px");
        qFoodLayout1.getStyle().set("margin-left", "10px");
        qFoodLayout1.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qFoodLayout1.setVisible(false);
    }

    private void createFood2() {
        Html domainIntro = new Html(displayData.getFoodSecurityInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "4", "Food Security");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q4Group = new RadioButtonGroup<QuestionnaireItem>();
        q4Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q4Group.setItems(selections);
        q4Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qFoodLayout2 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q4Group);
        qFoodLayout2.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qFoodLayout2.setBoxSizing(BoxSizing.BORDER_BOX);
        qFoodLayout2.setHeightFull();
        qFoodLayout2.setBackgroundColor("white");
        qFoodLayout2.setShadow(Shadow.S);
        qFoodLayout2.setBorderRadius(BorderRadius.S);
        qFoodLayout2.getStyle().set("margin-bottom", "10px");
        qFoodLayout2.getStyle().set("margin-right", "10px");
        qFoodLayout2.getStyle().set("margin-left", "10px");
        qFoodLayout2.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qFoodLayout2.setVisible(false);
    }

    private void createTransportation() {
        Html domainIntro = new Html(displayData.getTransportationAccessInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "5", "Transportation Access");

        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q5Group = new RadioButtonGroup<QuestionnaireItem>();
        q5Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q5Group.setItems(selections);
        q5Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qTransportationAccessLayout1 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q5Group);
        qTransportationAccessLayout1.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qTransportationAccessLayout1.setBoxSizing(BoxSizing.BORDER_BOX);
        qTransportationAccessLayout1.setHeightFull();
        qTransportationAccessLayout1.setBackgroundColor("white");
        qTransportationAccessLayout1.setShadow(Shadow.S);
        qTransportationAccessLayout1.setBorderRadius(BorderRadius.S);
        qTransportationAccessLayout1.getStyle().set("margin-bottom", "10px");
        qTransportationAccessLayout1.getStyle().set("margin-right", "10px");
        qTransportationAccessLayout1.getStyle().set("margin-left", "10px");
        qTransportationAccessLayout1.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qTransportationAccessLayout1.setVisible(false);
    }

    private void createUtilitiesNeeds() {
            Html domainIntro = new Html(displayData.getUtilityNeedsInfo());
            QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "6", "Utility Needs");
            List<QuestionnaireItem> selections = section.getItemList();

            Html question = new Html(section.getQuestion());
            q6Group = new RadioButtonGroup<QuestionnaireItem>();
            q6Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
            q6Group.setItems(selections);
            q6Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

            qUtilitiesLayout = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q6Group);
            qUtilitiesLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
            qUtilitiesLayout.setBoxSizing(BoxSizing.BORDER_BOX);
            qUtilitiesLayout.setHeightFull();
            qUtilitiesLayout.setBackgroundColor("white");
            qUtilitiesLayout.setShadow(Shadow.S);
            qUtilitiesLayout.setBorderRadius(BorderRadius.S);
            qUtilitiesLayout.getStyle().set("margin-bottom", "10px");
            qUtilitiesLayout.getStyle().set("margin-right", "10px");
            qUtilitiesLayout.getStyle().set("margin-left", "10px");
            qUtilitiesLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
            qUtilitiesLayout.setVisible(false);
    }

    private void createPersonalSafety1() {
        Html domainIntro = new Html(displayData.getPersonalSafetyInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "7", "Personal Safety");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q7Group = new RadioButtonGroup<QuestionnaireItem>();
        q7Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q7Group.setItems(selections);
        q7Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qSafetyLayout1 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q7Group);
        qSafetyLayout1.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qSafetyLayout1.setBoxSizing(BoxSizing.BORDER_BOX);
        qSafetyLayout1.setHeightFull();
        qSafetyLayout1.setBackgroundColor("white");
        qSafetyLayout1.setShadow(Shadow.S);
        qSafetyLayout1.setBorderRadius(BorderRadius.S);
        qSafetyLayout1.getStyle().set("margin-bottom", "10px");
        qSafetyLayout1.getStyle().set("margin-right", "10px");
        qSafetyLayout1.getStyle().set("margin-left", "10px");
        qSafetyLayout1.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qSafetyLayout1.setVisible(false);
    }

    private void createPersonalSafety2() {
        Html domainIntro = new Html(displayData.getPersonalSafetyInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "8", "Personal Safety");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q8Group = new RadioButtonGroup<QuestionnaireItem>();
        q8Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q8Group.setItems(selections);
        q8Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qSafetyLayout2 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q8Group);
        qSafetyLayout2.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qSafetyLayout2.setBoxSizing(BoxSizing.BORDER_BOX);
        qSafetyLayout2.setHeightFull();
        qSafetyLayout2.setBackgroundColor("white");
        qSafetyLayout2.setShadow(Shadow.S);
        qSafetyLayout2.setBorderRadius(BorderRadius.S);
        qSafetyLayout2.getStyle().set("margin-bottom", "10px");
        qSafetyLayout2.getStyle().set("margin-right", "10px");
        qSafetyLayout2.getStyle().set("margin-left", "10px");
        qSafetyLayout2.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qSafetyLayout2.setVisible(false);
    }

    private void createPersonalSafety3() {
        Html domainIntro = new Html(displayData.getPersonalSafetyInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "9", "Personal Safety");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q9Group = new RadioButtonGroup<QuestionnaireItem>();
        q9Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q9Group.setItems(selections);
        q9Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qSafetyLayout3 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q9Group);
        qSafetyLayout3.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qSafetyLayout3.setBoxSizing(BoxSizing.BORDER_BOX);
        qSafetyLayout3.setHeightFull();
        qSafetyLayout3.setBackgroundColor("white");
        qSafetyLayout3.setShadow(Shadow.S);
        qSafetyLayout3.setBorderRadius(BorderRadius.S);
        qSafetyLayout3.getStyle().set("margin-bottom", "10px");
        qSafetyLayout3.getStyle().set("margin-right", "10px");
        qSafetyLayout3.getStyle().set("margin-left", "10px");
        qSafetyLayout3.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qSafetyLayout3.setVisible(false);
    }

    private void createPersonalSafety4() {
        Html domainIntro = new Html(displayData.getPersonalSafetyInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "10", "Personal Safety");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q10Group = new RadioButtonGroup<QuestionnaireItem>();
        q10Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q10Group.setItems(selections);
        q10Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qSafetyLayout4 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q10Group);
        qSafetyLayout4.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qSafetyLayout4.setBoxSizing(BoxSizing.BORDER_BOX);
        qSafetyLayout4.setHeightFull();
        qSafetyLayout4.setBackgroundColor("white");
        qSafetyLayout4.setShadow(Shadow.S);
        qSafetyLayout4.setBorderRadius(BorderRadius.S);
        qSafetyLayout4.getStyle().set("margin-bottom", "10px");
        qSafetyLayout4.getStyle().set("margin-right", "10px");
        qSafetyLayout4.getStyle().set("margin-left", "10px");
        qSafetyLayout4.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qSafetyLayout4.setVisible(false);
    }

    private void createFinancialStrain() {
        Html domainIntro = new Html(displayData.getEmploymentAndEducationInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "11", "Employment and Education");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q11Group = new RadioButtonGroup<QuestionnaireItem>();
        q11Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q11Group.setItems(selections);
        q11Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qFinancialStrainLayout = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q11Group);
        qFinancialStrainLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qFinancialStrainLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        qFinancialStrainLayout.setHeightFull();
        qFinancialStrainLayout.setBackgroundColor("white");
        qFinancialStrainLayout.setShadow(Shadow.S);
        qFinancialStrainLayout.setBorderRadius(BorderRadius.S);
        qFinancialStrainLayout.getStyle().set("margin-bottom", "10px");
        qFinancialStrainLayout.getStyle().set("margin-right", "10px");
        qFinancialStrainLayout.getStyle().set("margin-left", "10px");
        qFinancialStrainLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qFinancialStrainLayout.setVisible(false);
    }

    private void createEmployment() {
        Html domainIntro = new Html(displayData.getEmploymentAndEducationInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "12", "Employment and Education");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q12Group = new RadioButtonGroup<QuestionnaireItem>();
        q12Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q12Group.setItems(selections);
        q12Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qEmploymentLayout = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q12Group);
        qEmploymentLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qEmploymentLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        qEmploymentLayout.setHeightFull();
        qEmploymentLayout.setBackgroundColor("white");
        qEmploymentLayout.setShadow(Shadow.S);
        qEmploymentLayout.setBorderRadius(BorderRadius.S);
        qEmploymentLayout.getStyle().set("margin-bottom", "10px");
        qEmploymentLayout.getStyle().set("margin-right", "10px");
        qEmploymentLayout.getStyle().set("margin-left", "10px");
        qEmploymentLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qEmploymentLayout.setVisible(false);
    }

    private void createEducation1() {
        Html domainIntro = new Html(displayData.getEmploymentAndEducationInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "13", "Employment and Education");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q13Group = new RadioButtonGroup<QuestionnaireItem>();
        q13Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q13Group.setItems(selections);
        q13Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qEducationLayout1 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q13Group);
        qEducationLayout1.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qEducationLayout1.setBoxSizing(BoxSizing.BORDER_BOX);
        qEducationLayout1.setHeightFull();
        qEducationLayout1.setBackgroundColor("white");
        qEducationLayout1.setShadow(Shadow.S);
        qEducationLayout1.setBorderRadius(BorderRadius.S);
        qEducationLayout1.getStyle().set("margin-bottom", "10px");
        qEducationLayout1.getStyle().set("margin-right", "10px");
        qEducationLayout1.getStyle().set("margin-left", "10px");
        qEducationLayout1.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qEducationLayout1.setVisible(false);
    }

    private void createFamilySupport1() {
        Html domainIntro = new Html(displayData.getSocialSupportInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "15", "Social Support");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q15Group = new RadioButtonGroup<QuestionnaireItem>();
        q15Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q15Group.setItems(selections);
        q15Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qFamilyCommunitySupportLayout1 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q15Group);
        qFamilyCommunitySupportLayout1.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qFamilyCommunitySupportLayout1.setBoxSizing(BoxSizing.BORDER_BOX);
        qFamilyCommunitySupportLayout1.setHeightFull();
        qFamilyCommunitySupportLayout1.setBackgroundColor("white");
        qFamilyCommunitySupportLayout1.setShadow(Shadow.S);
        qFamilyCommunitySupportLayout1.setBorderRadius(BorderRadius.S);
        qFamilyCommunitySupportLayout1.getStyle().set("margin-bottom", "10px");
        qFamilyCommunitySupportLayout1.getStyle().set("margin-right", "10px");
        qFamilyCommunitySupportLayout1.getStyle().set("margin-left", "10px");
        qFamilyCommunitySupportLayout1.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qFamilyCommunitySupportLayout1.setVisible(false);
    }

    private void createFamilySupport2() {
        Html domainIntro = new Html(displayData.getSocialSupportInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "16", "Social Support");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q16Group = new RadioButtonGroup<QuestionnaireItem>();
        q16Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q16Group.setItems(selections);
        q16Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qFamilyCommunitySupportLayout2 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q16Group);
        qFamilyCommunitySupportLayout2.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qFamilyCommunitySupportLayout2.setBoxSizing(BoxSizing.BORDER_BOX);
        qFamilyCommunitySupportLayout2.setHeightFull();
        qFamilyCommunitySupportLayout2.setBackgroundColor("white");
        qFamilyCommunitySupportLayout2.setShadow(Shadow.S);
        qFamilyCommunitySupportLayout2.setBorderRadius(BorderRadius.S);
        qFamilyCommunitySupportLayout2.getStyle().set("margin-bottom", "10px");
        qFamilyCommunitySupportLayout2.getStyle().set("margin-right", "10px");
        qFamilyCommunitySupportLayout2.getStyle().set("margin-left", "10px");
        qFamilyCommunitySupportLayout2.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qFamilyCommunitySupportLayout2.setVisible(false);
    }
    private void createEducation2() {
        Html domainIntro = new Html(displayData.getEmploymentAndEducationInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "14", "Employment and Education");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q14Group = new RadioButtonGroup<QuestionnaireItem>();
        q14Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q14Group.setItems(selections);
        q14Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qEducationLayout2 = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q14Group);
        qEducationLayout2.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qEducationLayout2.setBoxSizing(BoxSizing.BORDER_BOX);
        qEducationLayout2.setHeightFull();
        qEducationLayout2.setBackgroundColor("white");
        qEducationLayout2.setShadow(Shadow.S);
        qEducationLayout2.setBorderRadius(BorderRadius.S);
        qEducationLayout2.getStyle().set("margin-bottom", "10px");
        qEducationLayout2.getStyle().set("margin-right", "10px");
        qEducationLayout2.getStyle().set("margin-left", "10px");
        qEducationLayout2.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qEducationLayout2.setVisible(false);
    }

    private void createLegalSupport() {
        Html domainIntro = new Html(displayData.getLegalSupportInfo());
        QuestionnaireSection section = displayData.getFHIRQuestionnaireSection(acornQuestionnaire, "17", "Legal Support");
        List<QuestionnaireItem> selections = section.getItemList();

        Html question = new Html(section.getQuestion());
        q17Group = new RadioButtonGroup<QuestionnaireItem>();
        q17Group.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        q17Group.setItems(selections);
        q17Group.setRenderer(new ComponentRenderer<>(this::createQuestionItem));

        qLegal = new FlexBoxLayout(domainIntro, createHeader(VaadinIcon.USERS, section.getTitle()),question, new BasicDivider(), q17Group);
        qLegal.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        qLegal.setBoxSizing(BoxSizing.BORDER_BOX);
        qLegal.setHeightFull();
        qLegal.setBackgroundColor("white");
        qLegal.setShadow(Shadow.S);
        qLegal.setBorderRadius(BorderRadius.S);
        qLegal.getStyle().set("margin-bottom", "10px");
        qLegal.getStyle().set("margin-right", "10px");
        qLegal.getStyle().set("margin-left", "10px");
        qLegal.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        qLegal.setVisible(false);
    }


    private Component createQuestionItem(QuestionnaireItem qItem) {
        ListItem item = new ListItem(qItem.getDisplay());
        item.setPadding(Vertical.XS);
        return item;
    }

    private void processAnswers() {
        answerList = new ArrayList<>();
        QuestionnaireItem q1GroupAnswer = q1Group.getValue();
        Set<QuestionnaireItem> q2GroupAnswers = q2Group.getSelectedItems();
        QuestionnaireItem q3GroupAnswer = q3Group.getValue();
        QuestionnaireItem q4GroupAnswer = q4Group.getValue();
        QuestionnaireItem q5GroupAnswer = q5Group.getValue();
        QuestionnaireItem q6GroupAnswer = q6Group.getValue();
        QuestionnaireItem q7GroupAnswer = q7Group.getValue();
        QuestionnaireItem q8GroupAnswer = q8Group.getValue();
        QuestionnaireItem q9GroupAnswer = q9Group.getValue();
        QuestionnaireItem q10GroupAnswer = q10Group.getValue();
        QuestionnaireItem q11GroupAnswer = q11Group.getValue();
        QuestionnaireItem q12GroupAnswer = q12Group.getValue();
        QuestionnaireItem q13GroupAnswer = q13Group.getValue();
        QuestionnaireItem q14GroupAnswer = q14Group.getValue();
        QuestionnaireItem q15GroupAnswer = q15Group.getValue();
        QuestionnaireItem q16GroupAnswer = q16Group.getValue();
        QuestionnaireItem q17GroupAnswer = q17Group.getValue();
        if(q1GroupAnswer != null) answerList.add(q1GroupAnswer);
        Iterator iter = q2GroupAnswers.iterator();
        while (iter.hasNext()) {
            QuestionnaireItem ans = (QuestionnaireItem) iter.next();
            answerList.add(ans);
        }
        if(q3GroupAnswer != null) answerList.add(q3GroupAnswer);
        if(q4GroupAnswer != null) answerList.add(q4GroupAnswer);
        if(q5GroupAnswer != null) answerList.add(q5GroupAnswer);
        if(q6GroupAnswer != null) answerList.add(q6GroupAnswer);
        if(q7GroupAnswer != null) answerList.add(q7GroupAnswer);
        if(q8GroupAnswer != null) answerList.add(q8GroupAnswer);
        if(q9GroupAnswer != null) answerList.add(q9GroupAnswer);
        if(q10GroupAnswer != null) answerList.add(q10GroupAnswer);
        if(q11GroupAnswer != null) answerList.add(q11GroupAnswer);
        if(q12GroupAnswer != null) answerList.add(q12GroupAnswer);
        if(q13GroupAnswer != null) answerList.add(q13GroupAnswer);
        if(q14GroupAnswer != null) answerList.add(q14GroupAnswer);
        if(q15GroupAnswer != null) answerList.add(q15GroupAnswer);
        if(q16GroupAnswer != null) answerList.add(q16GroupAnswer);
        if(q17GroupAnswer != null) answerList.add(q17GroupAnswer);
        if (!answerList.isEmpty()) {
            Iterator iter2 = answerList.iterator();
            while (iter2.hasNext()) {
                QuestionnaireItem disp = (QuestionnaireItem) iter2.next();
                System.out.println(disp.getLink() + " " + disp.getDisplay());
            }
        }
        finalQuestionnaireResponse();
    }

    private boolean getAnswerValue(String linkId) {
        boolean res = false;
        Iterator iter = answerList.iterator();
        while(iter.hasNext()) {
            QuestionnaireItem item = (QuestionnaireItem) iter.next();
            if (item.getLink().equals(linkId)) {
                res = true;
                break;
            }
        }

        return res;
    }

    private QuestionnaireResponse createQuestionnaireResponse() {
        QuestionnaireResponse resp = new QuestionnaireResponse();
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        resp.setAuthor(patientRef);
        resp.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        resp.setAuthored(new Date());
        resp.setId("acorn-"+consentSession.getFhirPatientId());
        resp.setSubject(patientRef);
        resp.setQuestionnaire("Questionnaire/acorn-himss2022-demonstration");


        //create main item
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> mainItem = new ArrayList<>();
        mainItem.add(processDomainResponse("housing-security"));
        mainItem.add(processDomainResponse("food-security"));
        mainItem.add(processDomainResponse("transportation-access"));
        mainItem.add(processDomainResponse("utility-access"));
        mainItem.add(processDomainResponse("personal-safety"));
        mainItem.add(processDomainResponse("employment-and-education"));
        mainItem.add(processDomainResponse("social-support"));
        mainItem.add(processDomainResponse("legal-support"));

        resp.setItem(mainItem);


        return resp;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent processDomainResponse(String linkId) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent res = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        res.setLinkId(linkId);
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> resList = new ArrayList<>();
        Questionnaire.QuestionnaireItemComponent qItem = acornQuestionnaire.getQuestion(linkId);
        List<Questionnaire.QuestionnaireItemComponent> qItemsList = qItem.getItem();
        Iterator qItemsListIterator = qItemsList.iterator();
        while (qItemsListIterator.hasNext()) {
            Questionnaire.QuestionnaireItemComponent comp = (Questionnaire.QuestionnaireItemComponent) qItemsListIterator.next();
            if (comp.getType().toCode().equals("group")) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent groupResponse = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
                groupResponse.setLinkId(comp.getLinkId());
                List<QuestionnaireResponse.QuestionnaireResponseItemComponent> answerList = new ArrayList<>();
                //get questions
                List<Questionnaire.QuestionnaireItemComponent> questionList = comp.getItem();
                Iterator questionListIterator = questionList.iterator();
                while (questionListIterator.hasNext()) {
                    Questionnaire.QuestionnaireItemComponent questionComp = (Questionnaire.QuestionnaireItemComponent) questionListIterator.next();
                    boolean answerResult = getAnswerValue(questionComp.getLinkId());
                    QuestionnaireResponse.QuestionnaireResponseItemComponent fAnswer = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
                    fAnswer.setLinkId(questionComp.getLinkId());
                    List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> fAnswerArray = new ArrayList<>();
                    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent fAnswerArrayItem = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
                    BooleanType b = new BooleanType(answerResult);
                    fAnswerArrayItem.setValue(b);
                    fAnswerArray.add(fAnswerArrayItem);
                    fAnswer.setAnswer(fAnswerArray);
                    answerList.add(fAnswer);
                }
                groupResponse.setItem(answerList);
                resList.add(groupResponse);
            }
            res.setItem(resList);
        }
        return res;
    }

    private void finalQuestionnaireResponse() {

        acornQuestionnaireResponse = createQuestionnaireResponse();
        FhirContext ctx = FhirContext.forR4();
        IParser parser = ctx.newJsonParser();
        parser.setPrettyPrint(true);
        String questionnaireResponse = parser.encodeResourceToString(acornQuestionnaireResponse);
        System.out.println(questionnaireResponse);
        String questionnaire = parser.encodeResourceToString(acornQuestionnaire);
        try {

            String transformedQuestionnaireResponse = BasicSDCQuestionnaireProcessor.transform(questionnaire, questionnaireResponse);
            acornQuestionnaireResponseFinal = (QuestionnaireResponse) parser.parseResource(transformedQuestionnaireResponse);
            fhirQuestionnaireResponse.createQuestionnaireResponse(acornQuestionnaireResponseFinal);
            createNeedsListing();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createNeedsListing() {
        sdohNeeds = new ArrayList<>();
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> compList = acornQuestionnaireResponseFinal.getItem();
        Iterator iter = compList.iterator();
        while (iter.hasNext()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemComponent = (QuestionnaireResponse.QuestionnaireResponseItemComponent) iter.next();
            List<QuestionnaireResponse.QuestionnaireResponseItemComponent> compList2 = itemComponent.getItem();
            Iterator iter3 = compList2.iterator();
            while (iter3.hasNext()) {
                QuestionnaireResponse.QuestionnaireResponseItemComponent itemComponent2 = (QuestionnaireResponse.QuestionnaireResponseItemComponent) iter3.next();
                String linkId2 = itemComponent2.getLinkId();
                if (itemComponent2.getLinkId().indexOf("-need") > -1) {
                    List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answerList2 = itemComponent2.getAnswer();
                    Iterator iter4 = answerList2.iterator();
                    while (iter4.hasNext()) {
                        QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answerComponent2 = (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent) iter4.next();
                        BooleanType bType2 = (BooleanType) answerComponent2.getValue();
                        SDOHNeed needed = new SDOHNeed(linkId2, bType2.getValue());
                        sdohNeeds.add(needed);
                    }
                }
            }
        }
    }

    private boolean isSDOHNeeded(String sdohDomain) {
        boolean res = false;
        Iterator iter = sdohNeeds.iterator();
        while (iter.hasNext()) {
            SDOHNeed need = (SDOHNeed) iter.next();
            if (need.getDomain().equals(sdohDomain)) {
                res = need.isNeeded();
            }
        }
        return res;
    }
    
    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        Consent sdohDirective = new Consent();
        sdohDirective.setId("acorn-sdoh-"+consentSession.getFhirPatientId());
        sdohDirective.setStatus(Consent.ConsentState.ACTIVE);

        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentcategorycodes");
        coding.setCode("sdoh");
        cConcept.addCoding(coding);
        cList.add(cConcept);

        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-0");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        sdohDirective.setCategory(cList);


        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        sdohDirective.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        //todo - this is the deployment and custodian organization for advanced directives and should be valid in fhir consent repository
        orgRef.setReference("Organization/acorn-sdoh-org-va");
        orgRef.setDisplay("US Dept. of Veterans Affairs");
        refList.add(orgRef);
        sdohDirective.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        Date dateRecordedProvenance = new Date();
        attachment.setCreation(dateRecordedProvenance);
        attachment.setTitle("ACORN");


        String encodedString = Base64.getEncoder().encodeToString(consentPDFAsByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        sdohDirective.setSource(attachment);

        //provision root
        Consent.provisionComponent provision = new Consent.provisionComponent();

        //set default rule provision root
        Period period = new Period();
        LocalDate sDate = LocalDate.now();
        LocalDate eDate = LocalDate.now().plusMonths(6);
        Date startDate = Date.from(sDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(eDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        period.setStart(startDate);
        period.setEnd(endDate);
        provision.setType(Consent.ConsentProvisionType.DENY);
        provision.setPeriod(period);

        //set organizational provisions
        Iterator iter = selectedSDOHOrganizations.iterator();
        orgsWithConsentGranted = new ArrayList<>();
        while (iter.hasNext()) {
            SDOHOrganization sdohOrganization = (SDOHOrganization) iter.next();
            if (!orgsWithConsentGranted.contains(sdohOrganization.getParentorganizationid())) {
                Consent.provisionComponent eProvision = new Consent.provisionComponent();

                eProvision.setPeriod(period);

                eProvision.setType(Consent.ConsentProvisionType.PERMIT);

                List<Coding> purposeList = new ArrayList<>();
                //POUs
                Coding ePurposeCoding = new Coding();
                ePurposeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActReason");
                ePurposeCoding.setCode("SDOH");
                purposeList.add(ePurposeCoding);

                Consent.provisionActorComponent actor = new Consent.provisionActorComponent();
                CodeableConcept roleConcept = new CodeableConcept();
                Coding rolecoding = new Coding();
                rolecoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
                rolecoding.setCode("IRCP");
                roleConcept.addCoding(rolecoding);
                actor.setRole(roleConcept);

                Reference actorRef = new Reference();

                actorRef.setReference("Organization/"+sdohOrganization.getParentorganizationid());
                actorRef.setDisplay(sdohOrganization.getParentorganizationname());


                actor.setReference(actorRef);

                List<Consent.provisionActorComponent> actorList = new ArrayList<>();
                actorList.add(actor);

                //set action
                Coding actioncoding = new Coding();
                actioncoding.setSystem("http://terminology.hl7.org/CodeSystem/consentaction");
                actioncoding.setCode("access");

                Coding actioncodingcorrect = new Coding();
                actioncodingcorrect.setSystem("http://terminology.hl7.org/CodeSystem/consentaction");
                actioncodingcorrect.setCode("correct");
                List<CodeableConcept> actionCodeList = new ArrayList<>();
                CodeableConcept actionConcept = new CodeableConcept();
                actionConcept.addCoding(actioncoding);
                actionConcept.addCoding(actioncodingcorrect);
                actionCodeList.add(actionConcept);

                eProvision.setActor(actorList);
                eProvision.setAction(actionCodeList);

                eProvision.setPurpose(purposeList);
                provision.addProvision(eProvision);
                orgsWithConsentGranted.add(sdohOrganization.getParentorganizationid());
            }
        }

        sdohDirective.setProvision(provision);

        Extension extension = createQuestionnaireResponseExtension();
        sdohDirective.getExtension().add(extension);

        Consent completedConsent = fhirConsentClient.createConsent(sdohDirective);
        //consentProvenance = "Consent/"+sdohDirective.getId();
    }

    private Extension createQuestionnaireResponseExtension() {
        Extension extension = new Extension();
        extension.setUrl("https://va.gov/sdoh/acorn");
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/acorn-"+consentSession.getFhirPatientId()));
        return extension;
    }
    private void successNotification() {
        Notification notification = Notification.show("Congrats! You've successfully created FHIR Consent to begin ACORN referral processing.");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }
}
