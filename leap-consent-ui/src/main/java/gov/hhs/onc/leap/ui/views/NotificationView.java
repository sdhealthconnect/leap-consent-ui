package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.backend.ConsentNotification;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.fhir.client.utils.*;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.signature.PDFSigningService;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.Badge;
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
import gov.hhs.onc.leap.ui.util.pdf.PDFDocumentHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFInformedConsentHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFPOAHealthcareHandler;
import gov.hhs.onc.leap.ui.util.pdf.PDFResearchStudyHandler;
import org.hl7.fhir.instance.model.api.IBaseResource;
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

@PageTitle("Notifications")
@Route(value = "notificationview", layout = MainLayout.class)
public class NotificationView extends ViewFrame {
    private static final Logger log = LoggerFactory.getLogger(NotificationView.class);

    private ConsentNotification patientPrivacyNotification;
    private ConsentNotification adrLivingWillNotification;
    private ConsentNotification adrPOAHealthCareNotification;
    private ConsentNotification adrPOAMentalHealthNotification;
    private ConsentNotification adrDNRNotification;
    private ConsentNotification polstNotification;

    private ConsentSession consentSession;

    private boolean activeExchangeExists = false;

    private Grid<ConsentNotification> grid;
    private ListDataProvider<ConsentNotification> dataProvider;

    private Grid<ConsentNotification> medRequestGrid;
    private ListDataProvider<ConsentNotification> medRequestDataProvider;

    private Grid<ConsentNotification> clinicalTrialsGrid;
    private ListDataProvider<ConsentNotification> clinicalTrialsDataProvider;

    private Tabs notificationTabs;
    private Tab policyTab;
    private Tab medicationRequestTab;
    private Tab clinicalTrialsTab;

    private FlexBoxLayout policyLayout;

    private FlexBoxLayout medicationRequestLayout;
    private FlexBoxLayout reviewInformedConsentLayout;
    private ConsentNotification selectedConsentNotification;
    private Dialog infoDialog;
    private FlexBoxLayout patientSignatureLayout;
    private FlexBoxLayout physicianSignatureLayout;
    private TextField physicianName;
    private TextField medicationName;
    private SignaturePad patientSignature;
    private Date patientSignatureDate;
    private Checkbox treatmentAccepted;
    private Checkbox treatmentDeclined;
    private byte[] base64PatientSignature;
    private boolean patientConsents = false;
    private boolean patientDeclines = false;

    private SignaturePad physcianSignature;
    private byte[] base64PhysicianSignature;
    private TextField attestationDRName;
    private TextField attestationPatientName;
    private TextField attestationDate;

    private byte[] consentPDFAsByteArray;

    private Dialog docDialog;

    private ConsentUser consentUser;

    private FlexBoxLayout clinicalTrialsLayout;
    private FlexBoxLayout rsReviewInformedConsentLayout;
    private FlexBoxLayout rsPatientSignatureLayout;
    private TextField rsTitleField;
    private Checkbox participateAccepted;
    private Checkbox participateDeclined;
    private SignaturePad rsPatientSignature;
    private byte[] base64ResearchStudyPatientSignature;
    private Date rsPatientSignatureDate;
    private Dialog rsInfoDialog;
    private String nctNumber;
    private Anchor clinicalTrialsLink;




    @Autowired
    private FHIRConsent fhirConsentClient;

    @Autowired
    private FHIRMedicationRequest fhirMedicationRequestClient;

    @Autowired
    private FHIRResearchSubject fhirResearchSubject;

    @Autowired
    private FHIRResearchStudy fhirResearchStudy;

    @Autowired
    private FHIROrganization fhirOrganization;

    @Autowired
    private PDFSigningService pdfSigningService;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;

    @PostConstruct
    public void setup() {
        setId("notificationview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        setViewContent(getViewContent());
    }

    private Component getViewContent() {
        createTabs();
        createPolicyLayout();
        createMedicationRequestLayout();
        createClinicalTrialsLayout();
        createInformedConsentLayout();
        createPatientSignatureLayout();
        createPhysicianSignatureLayout();
        createRSInformedConsentLayout();
        createRSPatientSignatureLayout();
        FlexBoxLayout content = new FlexBoxLayout(notificationTabs,policyLayout, medicationRequestLayout, clinicalTrialsLayout, reviewInformedConsentLayout,
                patientSignatureLayout, physicianSignatureLayout, rsReviewInformedConsentLayout, rsPatientSignatureLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();

        return content;
    }

    private void createTabs() {
        notificationTabs = new Tabs();
        notificationTabs.setOrientation(Tabs.Orientation.HORIZONTAL);
        policyTab = new Tab("Policy");
        medicationRequestTab = new Tab("Medication Requests");
        clinicalTrialsTab = new Tab("Clinical Trials");
        notificationTabs.add(policyTab, medicationRequestTab, clinicalTrialsTab);
        notificationTabs.addSelectedChangeListener(event -> {
            String selectedTabName = notificationTabs.getSelectedTab().getLabel();
            if (selectedTabName.equals("Policy")) {
                policyLayout.setVisible(true);
                medicationRequestLayout.setVisible(false);
                clinicalTrialsLayout.setVisible(false);
                reviewInformedConsentLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                physicianSignatureLayout.setVisible(false);
                rsReviewInformedConsentLayout.setVisible(false);
                rsPatientSignatureLayout.setVisible(false);
            }
            else if (selectedTabName.equals("Medication Requests")) {
                policyLayout.setVisible(false);
                medicationRequestLayout.setVisible(true);
                clinicalTrialsLayout.setVisible(false);
                reviewInformedConsentLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                physicianSignatureLayout.setVisible(false);
                rsReviewInformedConsentLayout.setVisible(false);
                rsPatientSignatureLayout.setVisible(false);
            }
            else if (selectedTabName.equals("Clinical Trials")) {
                policyLayout.setVisible(false);
                medicationRequestLayout.setVisible(false);
                clinicalTrialsLayout.setVisible(true);
                reviewInformedConsentLayout.setVisible(false);
                patientSignatureLayout.setVisible(false);
                physicianSignatureLayout.setVisible(false);
                rsReviewInformedConsentLayout.setVisible(false);
                rsPatientSignatureLayout.setVisible(false);
            }
            else {
                //nothing here
            }
        });
    }

    private void createPolicyLayout() {
        dataProvider = DataProvider.ofCollection(getConsentRequirements());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        ComponentRenderer<Badge, ConsentNotification> badgeRenderer = new ComponentRenderer<>(
                consentNotification -> {
                    ConsentNotification.Status status = consentNotification.getStatus();
                    Badge badge = new Badge(status.getName(), status.getTheme());
                    UIUtils.setTooltip(status.getDesc(), badge);
                    return badge;
                }
        );
        grid.addColumn(new ComponentRenderer<>(this::createActionRequirement))
                .setHeader("Requirement")
                .setAutoWidth(true);
        grid.addColumn(badgeRenderer)
                .setAutoWidth(true)
                .setHeader("Current Status");
        grid.addColumn(new ComponentRenderer<>(this::createShortName))
                .setHeader("Name")
                .setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::createDescription))
                .setHeader("Description")
                .setWidth("250px");
        grid.addColumn(new ComponentRenderer<>(this::createDestination))
                .setHeader("Take Me There")
                .setAutoWidth(true);

        policyLayout = new FlexBoxLayout(grid);
        policyLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        policyLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        policyLayout.setHeightFull();
        policyLayout.setBackgroundColor("white");
        policyLayout.setShadow(Shadow.S);
        policyLayout.setBorderRadius(BorderRadius.S);
        policyLayout.getStyle().set("margin-bottom", "10px");
        policyLayout.getStyle().set("margin-right", "10px");
        policyLayout.getStyle().set("margin-left", "10px");
        policyLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        policyLayout.setVisible(true);
    }

    private void createMedicationRequestLayout() {
        createMedicationRequestGrid();


        medicationRequestLayout = new FlexBoxLayout(medRequestGrid);
        medicationRequestLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        medicationRequestLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        medicationRequestLayout.setHeightFull();
        medicationRequestLayout.setBackgroundColor("white");
        medicationRequestLayout.setShadow(Shadow.S);
        medicationRequestLayout.setBorderRadius(BorderRadius.S);
        medicationRequestLayout.getStyle().set("margin-bottom", "10px");
        medicationRequestLayout.getStyle().set("margin-right", "10px");
        medicationRequestLayout.getStyle().set("margin-left", "10px");
        medicationRequestLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        medicationRequestLayout.setVisible(false);
    }

    private void createMedicationRequestGrid() {
        medRequestDataProvider = DataProvider.ofCollection(createMedicationRequestsArray());

        medRequestGrid = new Grid<>();
        medRequestGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        medRequestGrid.setDataProvider(medRequestDataProvider);
        medRequestGrid.setHeightFull();
        medRequestGrid.setMultiSort(true);
        medRequestGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        ComponentRenderer<Badge, ConsentNotification> badgeRenderer = new ComponentRenderer<>(
                consentNotification -> {
                    ConsentNotification.Status status = consentNotification.getStatus();
                    Badge badge = new Badge(status.getName(), status.getTheme());
                    UIUtils.setTooltip(status.getDesc(), badge);
                    return badge;
                }
        );
        medRequestGrid.addColumn(ConsentNotification::getNotificationDate)
                .setHeader("Date Authored")
                .setSortable(true)
                .setAutoWidth(true);
        medRequestGrid.addColumn(new ComponentRenderer<>(this::createActionRequirement))
                .setHeader("Requirement")
                .setAutoWidth(true);
        medRequestGrid.addColumn(badgeRenderer)
                .setAutoWidth(true)
                .setHeader("Current Status");
        medRequestGrid.addColumn(new ComponentRenderer<>(this::createShortName))
                .setHeader("Medication")
                .setAutoWidth(true);
        medRequestGrid.addColumn(new ComponentRenderer<>(this::createDescription))
                .setHeader("Requestor/Author")
                .setWidth("250px");
        medRequestGrid.addColumn(new ComponentRenderer<>(this::createDestination))
                .setHeader("Take Me There")
                .setAutoWidth(true);

    }

    private void createClinicalTrialsLayout() {
        createClinicalTrialsGrid();


        clinicalTrialsLayout = new FlexBoxLayout(clinicalTrialsGrid);
        clinicalTrialsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        clinicalTrialsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        clinicalTrialsLayout.setHeightFull();
        clinicalTrialsLayout.setBackgroundColor("white");
        clinicalTrialsLayout.setShadow(Shadow.S);
        clinicalTrialsLayout.setBorderRadius(BorderRadius.S);
        clinicalTrialsLayout.getStyle().set("margin-bottom", "10px");
        clinicalTrialsLayout.getStyle().set("margin-right", "10px");
        clinicalTrialsLayout.getStyle().set("margin-left", "10px");
        clinicalTrialsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        clinicalTrialsLayout.setVisible(false);
    }

    private void createClinicalTrialsGrid() {
        clinicalTrialsDataProvider = DataProvider.ofCollection(createResearchSubjectsArray());

        clinicalTrialsGrid = new Grid<>();
        clinicalTrialsGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        clinicalTrialsGrid.setDataProvider(clinicalTrialsDataProvider);
        clinicalTrialsGrid.setHeightFull();
        clinicalTrialsGrid.setMultiSort(true);
        clinicalTrialsGrid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        ComponentRenderer<Badge, ConsentNotification> badgeRenderer = new ComponentRenderer<>(
                consentNotification -> {
                    ConsentNotification.Status status = consentNotification.getStatus();
                    Badge badge = new Badge(status.getName(), status.getTheme());
                    UIUtils.setTooltip(status.getDesc(), badge);
                    return badge;
                }
        );
        clinicalTrialsGrid.addColumn(ConsentNotification::getNotificationDate)
                .setHeader("Last Activity Date")
                .setSortable(true)
                .setAutoWidth(true);
        clinicalTrialsGrid.addColumn(new ComponentRenderer<>(this::createActionRequirement))
                .setHeader("Requirement")
                .setAutoWidth(true);
        clinicalTrialsGrid.addColumn(badgeRenderer)
                .setAutoWidth(true)
                .setHeader("Current Status");
        clinicalTrialsGrid.addColumn(new ComponentRenderer<>(this::createShortName))
                .setHeader("Study Identifier")
                .setAutoWidth(true);
        clinicalTrialsGrid.addColumn(new ComponentRenderer<>(this::createDescription))
                .setHeader("Study Title")
                .setWidth("250px");
        clinicalTrialsGrid.addColumn(new ComponentRenderer<>(this::createDestination))
                .setHeader("Take Me There")
                .setAutoWidth(true);
    }

    private void createInformedConsentLayout() {
        Html intro = new Html("<p>Completion of the form is voluntary.  If not completed, this medication can not be administered.  This consent will be maintained " +
                "in your records and will accessible to authorized users.</p>");
        physicianName = new TextField("Your Physician:");
        Html medIntro = new Html("<p>Wishes to prescribe the following medication for you.</p>");
        medicationName = new TextField("Medication Name:");
        Html medIntro2 = new Html("<p>This medication can not be administered without your consent.  The first step in that process you, and your physician, will review some " +
                "information regarding this medication.  Such as its use, risks, side-effects, and other important information.  To begin that process click on the <b>Get Informed</b> button.</p>");
        Button getInformedBtn = new Button("Get Informed");
        getInformedBtn.addClickListener(event -> {
            infoDialog = createInfoDialog();
            infoDialog.open();
        });

        reviewInformedConsentLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Informed Consent - Medication Request (Experimental)"), intro, new BasicDivider(),
                physicianName, medIntro, medicationName, medIntro2, getInformedBtn);
        reviewInformedConsentLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        reviewInformedConsentLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        reviewInformedConsentLayout.setHeightFull();
        reviewInformedConsentLayout.setBackgroundColor("white");
        reviewInformedConsentLayout.setShadow(Shadow.S);
        reviewInformedConsentLayout.setBorderRadius(BorderRadius.S);
        reviewInformedConsentLayout.getStyle().set("margin-bottom", "10px");
        reviewInformedConsentLayout.getStyle().set("margin-right", "10px");
        reviewInformedConsentLayout.getStyle().set("margin-left", "10px");
        reviewInformedConsentLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        reviewInformedConsentLayout.setVisible(false);
    }

    private void createRSInformedConsentLayout() {
        Html intro = new Html("<p>Completion of the form is voluntary.  If not completed, you will not be considered for the research study listed below.  This consent will be maintained " +
                "in your records and will accessible to authorized users.</p>");
        rsTitleField = new TextField("Research Study Title:");


        Html intro2 = new Html("<p>The first step in this process you will review the informed consent document which contains some " +
                "basic information regarding this research study.  To begin that process click on the <b>Get Informed</b> button. " +
                "Additional detail information regarding this research study. Such as: eligibility criteria, intervention, "+
                "outcome measure, etc.  That detail information can be found at following link:</p>");

        clinicalTrialsLink = new Anchor("https://clinicaltrials.gov/ct2/show/", UIUtils.createButton("www.clinicaltrials.gov", VaadinIcon.EXTERNAL_LINK));

        Html intro3 = new Html("<p>These concepts are complex, because of this, it is recommended you discuss them with your primary physician. </p>");
        Button getInformedBtn = new Button("Get Informed");
        getInformedBtn.addClickListener(event -> {
            rsInfoDialog = createRSInfoDialog();
            rsInfoDialog.open();
        });

        rsReviewInformedConsentLayout = new FlexBoxLayout(createHeader(VaadinIcon.HOSPITAL, "Informed Consent - Research Study (Experimental)"), intro, new BasicDivider(),
                rsTitleField, intro2, clinicalTrialsLink, intro3, getInformedBtn);
        rsReviewInformedConsentLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        rsReviewInformedConsentLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        rsReviewInformedConsentLayout.setHeightFull();
        rsReviewInformedConsentLayout.setBackgroundColor("white");
        rsReviewInformedConsentLayout.setShadow(Shadow.S);
        rsReviewInformedConsentLayout.setBorderRadius(BorderRadius.S);
        rsReviewInformedConsentLayout.getStyle().set("margin-bottom", "10px");
        rsReviewInformedConsentLayout.getStyle().set("margin-right", "10px");
        rsReviewInformedConsentLayout.getStyle().set("margin-left", "10px");
        rsReviewInformedConsentLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        rsReviewInformedConsentLayout.setVisible(false);
    }

    private void createPatientSignatureLayout() {
        Html intro = new Html("<p>I have been counseled about potential side effects of the medication, " +
                "when they may occur, and when and where I should seek treatment.  I have read, or have had read to me, the informed consent " +
                "provided for the medication to be administered. I have had the opportunity to ask questions, and all " +
                "my questions have been answered to my satisfaction. I understand the benefits and risks of this medication.</p>");

        treatmentAccepted = new Checkbox("I consent to this treatment");
        treatmentAccepted.addClickListener(event -> {
           if (treatmentAccepted.getValue()) {
               treatmentDeclined.setValue(false);
           }
        });
        treatmentDeclined = new Checkbox("I decline this treatment");
        treatmentDeclined.addClickListener(event -> {
            if (treatmentDeclined.getValue()) {
                treatmentAccepted.setValue(false);
            }
        });

        patientSignature = new SignaturePad();
        patientSignature.setHeight("100px");
        patientSignature.setWidth("400px");
        patientSignature.setPenColor("#2874A6");

        Button backButton = new Button("Back");
        backButton.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.FAST_BACKWARD));
        backButton.addClickListener(event -> {
           patientSignatureLayout.setVisible(false);
           reviewInformedConsentLayout.setVisible(true);
        });
        Button clearWitnessSig = new Button("Clear Signature");
        clearWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearWitnessSig.addClickListener(event -> {
            patientSignature.clear();
        });
        Button saveWitnessSig = new Button("Accept Signature");
        saveWitnessSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveWitnessSig.addClickListener(event -> {
            base64PatientSignature = patientSignature.getImageBase64();
            patientSignatureDate = new Date();
            patientSignatureLayout.setVisible(false);
            physicianSignatureLayout.setVisible(true);
        });

        HorizontalLayout sigLayout = new HorizontalLayout(backButton, clearWitnessSig, saveWitnessSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);
        patientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Informed Consent - Medication Request (Experimental)"), intro, new BasicDivider(),
                treatmentAccepted, treatmentDeclined, patientSignature, sigLayout);
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

    private void createRSPatientSignatureLayout() {
        Html intro = new Html("<p>I have reviewed the informed consent document and detail information regarding this research study. " +
                "I have had the opportunity to ask questions, and all my questions have been answered to my satisfaction. " +
                "I understand the benefits and risks of my participation in this research study.</p>");

        participateAccepted = new Checkbox("I consent, and wish to participate in this study");
        participateAccepted.addClickListener(event -> {
            if (participateAccepted.getValue()) {
                participateDeclined.setValue(false);
                patientConsents = true;
                patientDeclines = false;
            }
        });
        participateDeclined = new Checkbox("I decline, and will not participate in this study");
        participateDeclined.addClickListener(event -> {
            if (participateDeclined.getValue()) {
                participateAccepted.setValue(false);
                patientConsents = false;
                patientDeclines = true;
            }
        });

        rsPatientSignature = new SignaturePad();
        rsPatientSignature.setHeight("100px");
        rsPatientSignature.setWidth("400px");
        rsPatientSignature.setPenColor("#2874A6");

        Button backButton = new Button("Back");
        backButton.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.FAST_BACKWARD));
        backButton.addClickListener(event -> {
            rsPatientSignatureLayout.setVisible(false);
            rsReviewInformedConsentLayout.setVisible(true);
        });
        Button clearSig = new Button("Clear Signature");
        clearSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearSig.addClickListener(event -> {
            rsPatientSignature.clear();
        });
        Button saveSig = new Button("Accept Signature");
        saveSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveSig.addClickListener(event -> {
            base64ResearchStudyPatientSignature = rsPatientSignature.getImageBase64();
            rsPatientSignatureDate = new Date();
            createRSHumanReadable();
            docDialog.open();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(backButton, clearSig, saveSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        rsPatientSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Informed Consent - Research Study (Experimental)"), intro, new BasicDivider(),
                participateAccepted, participateDeclined, rsPatientSignature, sigLayout);
        rsPatientSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        rsPatientSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        rsPatientSignatureLayout.setHeightFull();
        rsPatientSignatureLayout.setBackgroundColor("white");
        rsPatientSignatureLayout.setShadow(Shadow.S);
        rsPatientSignatureLayout.setBorderRadius(BorderRadius.S);
        rsPatientSignatureLayout.getStyle().set("margin-bottom", "10px");
        rsPatientSignatureLayout.getStyle().set("margin-right", "10px");
        rsPatientSignatureLayout.getStyle().set("margin-left", "10px");
        rsPatientSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        rsPatientSignatureLayout.setVisible(false);
    }

    private void createPhysicianSignatureLayout() {
        Html para1 = new Html("<p>I, Dr.</p>");
        attestationDRName = new TextField("Physician's Name");
        Html para2 = new Html("<p>have reviewed this document and have discussed with</p>");
        attestationPatientName = new TextField("Patient's Name");
        attestationPatientName.setValue(consentUser.getFirstName()+" "+consentUser.getMiddleName()+" "+consentUser.getLastName());
        Html para3 = new Html("<p>any questions regarding the probable medical consequences of the treatment choices provided. "+
                "This discussion with the patient occurred on this day.</p>");
        attestationDate = new TextField("Date");
        attestationDate.setValue(getDateStringForDisplay(new Date()));

        physcianSignature = new SignaturePad();
        physcianSignature.setHeight("100px");
        physcianSignature.setWidth("400px");
        physcianSignature.setPenColor("#2874A6");

        Button backButton = new Button("Back");
        backButton.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.FAST_BACKWARD));
        backButton.addClickListener(event -> {
            physicianSignatureLayout.setVisible(false);
            patientSignatureLayout.setVisible(true);
        });
        Button clearPatientSig = new Button("Clear Signature");
        clearPatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.ERASER));
        clearPatientSig.addClickListener(event -> {
            physcianSignature.clear();
        });
        Button savePatientSig = new Button("Accept Signature");
        savePatientSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        savePatientSig.addClickListener(event -> {
            base64PhysicianSignature = physcianSignature.getImageBase64();
            createHumanReadable();
            docDialog.open();
        });

        HorizontalLayout sigLayout = new HorizontalLayout(backButton, clearPatientSig, savePatientSig);
        sigLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sigLayout.setPadding(true);
        sigLayout.setSpacing(true);

        Html intro = new Html("<p>Physician's attestation.</p>");
        physicianSignatureLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHART, "Informed Consent - Medication Request (Experimental)"), intro, new BasicDivider(),
                para1, attestationDRName, para2, attestationPatientName, para3, attestationDate, physcianSignature, sigLayout);
        physicianSignatureLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        physicianSignatureLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        physicianSignatureLayout.setHeightFull();
        physicianSignatureLayout.setBackgroundColor("white");
        physicianSignatureLayout.setShadow(Shadow.S);
        physicianSignatureLayout.setBorderRadius(BorderRadius.S);
        physicianSignatureLayout.getStyle().set("margin-bottom", "10px");
        physicianSignatureLayout.getStyle().set("margin-right", "10px");
        physicianSignatureLayout.getStyle().set("margin-left", "10px");
        physicianSignatureLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        physicianSignatureLayout.setVisible(false);
    }

    private Component createActionRequirement(ConsentNotification consentNotification) {
        ListItem item = new ListItem(consentNotification.getActionRequired());
        item.setPadding(Vertical.XS);
        return item;
    }
    private Component createShortName(ConsentNotification consentNotification) {
        ListItem item = new ListItem(consentNotification.getShortName());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createDescription(ConsentNotification consentNotification) {
        ListItem item = new ListItem(consentNotification.getDescription());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createDestination(ConsentNotification consentNotification) {
        Button btn = new Button("Get Started");
        btn.addClickListener(event -> {
            try {
                if (consentNotification.getStatus().equals(ConsentNotification.Status.PENDING)) {
                    UI.getCurrent().navigate("consentdocumentview");
                } else if (consentNotification.getStatus().equals(ConsentNotification.Status.ONHOLD)) {
                    selectedConsentNotification = medRequestGrid.getSelectionModel().getFirstSelectedItem().get();
                    ConsentNotification.Status selectedStatus = selectedConsentNotification.getStatus();

                    if (!selectedStatus.equals(ConsentNotification.Status.ONHOLD)) {
                        Span content = new Span("You must select/highlight this row for this request to function.");

                        Notification notification = new Notification(content);
                        notification.setDuration(5000);

                        notification.setPosition(Notification.Position.MIDDLE);
                        notification.setThemeName("error");

                        notification.open();
                        return;
                    }
                    physicianName.setValue(((MedicationRequest) selectedConsentNotification.getFhirResource()).getRequester().getDisplay());
                    medicationName.setValue(((MedicationRequest) selectedConsentNotification.getFhirResource()).getMedicationCodeableConcept().getCoding().get(0).getDisplay());
                    attestationDRName.setValue(((MedicationRequest) selectedConsentNotification.getFhirResource()).getRequester().getDisplay());
                    medicationRequestLayout.setVisible(false);
                    reviewInformedConsentLayout.setVisible(true);
                } else if (consentNotification.getStatus().equals(ConsentNotification.Status.POTENTIALCANDIDATE)) {
                    selectedConsentNotification = clinicalTrialsGrid.getSelectionModel().getFirstSelectedItem().get();
                    ConsentNotification.Status selectedStatus = selectedConsentNotification.getStatus();

                    if (!selectedStatus.equals(ConsentNotification.Status.POTENTIALCANDIDATE)) {
                        Span content = new Span("You must select/highlight this row for this request to function.");

                        Notification notification = new Notification(content);
                        notification.setDuration(5000);

                        notification.setPosition(Notification.Position.MIDDLE);
                        notification.setThemeName("error");

                        notification.open();
                        return;
                    }
                    ResearchSubject subject = (ResearchSubject) selectedConsentNotification.getFhirResource();
                    nctNumber = subject.getStudy().getReference().replaceAll("ResearchStudy/", "");
                    clinicalTrialsLink.setHref("https://clinicaltrials.gov/ct2/show/" + nctNumber);
                    rsTitleField.setValue(subject.getStudy().getDisplay());
                    clinicalTrialsLayout.setVisible(false);
                    rsReviewInformedConsentLayout.setVisible(true);
                } else {
                    UI.getCurrent().navigate(consentNotification.getDestinationView());
                }
            }
            catch (Exception ex) {
                Span content = new Span("You must select/highlight this row for this request to function.");

                Notification notification = new Notification(content);
                notification.setDuration(5000);

                notification.setPosition(Notification.Position.MIDDLE);
                notification.setThemeName("error");

                notification.open();
                return;
            }
        });
        if (consentNotification.getActionRequired().equals("None")) {
            btn.setEnabled(false);
        }
        return btn;
    }

    private Component createDateAuthored(ConsentNotification consentNotification) {
        ListItem item = new ListItem(getDateStringForDisplay(consentNotification.getNotificationDate()));
        item.setPadding(Vertical.XS);
        return item;
    }

    private String getDateStringForDisplay(Date dt) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(dt);
        return date;
    }

    private Collection<ConsentNotification> getConsentRequirements() {
        createBaseNotification();
        determineCurrentConsentStates();
        Collection<ConsentNotification> consentReqList = createConsentRequirementArray();
        return consentReqList;
    }

    private void createBaseNotification() {
        //todo This is a listing of user consent requirements, ideally this is read in from a properties file or db based on the user's primary state's requirements and may require evaluation of patient's age and other demographic or conditional info
        //patient privacy
        patientPrivacyNotification = new ConsentNotification(new Date(),"Action Required", ConsentNotification.Status.NOTCOMPLETE, "patient-privacy",
                "Requires, at minimum, an exchange policy between your primary provider's organization and the hosting Healthcare Information Exchange(HIE).", "sharepatientdataview", null);
        //Advance Directives
        adrLivingWillNotification = new ConsentNotification(new Date(),"Action Required",ConsentNotification.Status.NOTCOMPLETE,"Advance Directive - Living Will",
                "Use this form to make decisions now about your medical care if you are ever in a terminal condition, a persistent vegetative state or an irreversible coma.",
                "livingwillview", null);
        adrPOAHealthCareNotification = new ConsentNotification(new Date(),"Action Required", ConsentNotification.Status.NOTCOMPLETE, "Advance Directive - Health Care Power of Attorney",
                "Helps you identify a person, called an \"agent\", to make future health care decisions for you so that if you become too ill or cannot make those decisions for yourself the person you choose and trust to make medical decisions for you.",
                "healthcarepowerofattorney", null);
        adrPOAMentalHealthNotification = new ConsentNotification(new Date(),"Optional", ConsentNotification.Status.NOTCOMPLETE, "Advance Directive - Mental Health Power of Attorney",
                "Helps you identify a person, also referred to as your \"agent\", to make future mental health care decisions for you if you become incapable of making those decisions for yourself.",
                "mentalhealthpowerofattorney", null);
        adrDNRNotification = new ConsentNotification(new Date(),"Optional", ConsentNotification.Status.NOTCOMPLETE, "Advance Directive - Do Not Resuscitate",
                "A document signed by you and your doctor that informs emergency medical technicians (EMTs) or hospital emergency personnel not to resuscitate you.",
                "dnrview", null);
        polstNotification = new ConsentNotification(new Date(),"Optional", ConsentNotification.Status.NOTCOMPLETE, "National Portable Medical Order",
                "Health care providers, the patient, or patient representative, should complete this form only after the " +
                        "health care provider has had a conversation with their patient or the patient’s representative.  " +
                        "The POLST decision-making process is for patients who are at risk for a life-threatening clinical event because they have a serious life-limiting medical " +
                        "condition, which may include advanced frailty.",
                "portablemedicalorderview", null);
    }

    private void determineCurrentConsentStates() {
        Collection<Consent> consentList = fhirConsentClient.getPatientConsents();
        Map<Long, ConsentDocument> CONSENT = new HashMap<>();
        Iterator iter = consentList.iterator();
        long i = 0;
        while(iter.hasNext()) {
            Consent c = (Consent)iter.next();
            String notificationType = "";
            ConsentNotification.Status status = ConsentNotification.Status.NOTCOMPLETE;
            Consent.ConsentState consentState = c.getStatus();
            Date endDate = c.getProvision().getPeriod().getEnd();
            if (endDate != null) {
                if (endDate.before(new Date()) && consentState.equals(Consent.ConsentState.ACTIVE)) {
                    status = ConsentNotification.Status.EXPIRED;
                } else if (consentState.equals(Consent.ConsentState.REJECTED)) {
                    status = ConsentNotification.Status.REVOKED;
                } else if (consentState.equals(Consent.ConsentState.INACTIVE)) {
                    status = ConsentNotification.Status.EXPIRED;
                } else if (consentState.equals(Consent.ConsentState.PROPOSED)) {
                    status = ConsentNotification.Status.PENDING;
                } else {
                    status = ConsentNotification.Status.ACTIVE;
                }
            }
            String policyType = c.getScope().getCoding().get(0).getCode();
            if (policyType.equals("adr")) {
                String title = c.getSourceAttachment().getTitle();
                notificationType = "adr - "+title;
            }
            else {
                notificationType = policyType;
            }

            String destination = "";
            try {
                if (c.getProvision().getProvision().get(0).getActor() != null && !c.getProvision().getProvision().get(0).getActor().isEmpty()) {
                    destination = c.getProvision().getProvision().get(0).getActor().get(0).getReference().getReference();
                }
            }
            catch (Exception ex) {
                log.warn("No provision actor for patient consent "+ex.getMessage());
            }

            // Set values for specfic consent notification
            if (notificationType.equals("patient-privacy") && destination != null && destination.equals(orgReference) && status.equals(ConsentNotification.Status.ACTIVE)) {
                if (!activeExchangeExists) {
                    //at least one exchange consent should exist
                    patientPrivacyNotification.setStatus(status);
                    patientPrivacyNotification.setActionRequired("None");
                    activeExchangeExists = true;
                }
            }
            else if (notificationType.equals("adr - LivingWill")) {
                adrLivingWillNotification.setStatus(status);
                if (status.equals(ConsentNotification.Status.ACTIVE)) {
                    adrLivingWillNotification.setActionRequired("None");
                }
            }
            else if (notificationType.equals("adr - POAHealthcare")) {
                adrPOAHealthCareNotification.setStatus(status);
                if (status.equals(ConsentNotification.Status.ACTIVE)) {
                    adrPOAHealthCareNotification.setActionRequired("None");
                }
            }
            else if (notificationType.equals("adr - POAMentalHealth")) {
                adrPOAMentalHealthNotification.setStatus(status);
                if (status.equals(ConsentNotification.Status.ACTIVE)) {
                    adrPOAMentalHealthNotification.setActionRequired("None");
                }
            }
            else if (notificationType.equals("adr - DNR")) {
                adrDNRNotification.setStatus(status);
                if (status.equals(ConsentNotification.Status.ACTIVE)) {
                    adrDNRNotification.setActionRequired("None");
                }
            }
            else if (notificationType.equals("adr - POLST")) {
                polstNotification.setStatus(status);
                if (status.equals(ConsentNotification.Status.ACTIVE)) {
                    polstNotification.setActionRequired("None");
                }
            }
        }
    }

    private Collection<ConsentNotification> createConsentRequirementArray() {
        Collection<ConsentNotification> reqList = new ArrayList<>();
        reqList.add(patientPrivacyNotification);
        reqList.add(adrLivingWillNotification);
        reqList.add(adrPOAHealthCareNotification);
        reqList.add(adrPOAMentalHealthNotification);
        reqList.add(adrDNRNotification);
        reqList.add(polstNotification);
        return reqList;
    }

    private Collection<ConsentNotification> createMedicationRequestsArray() {
        Collection<ConsentNotification> reqList = new ArrayList<>();
        Collection<MedicationRequest> medReqList = fhirMedicationRequestClient.getPatientMedicationRequests();
        Iterator iter = medReqList.iterator();
        while (iter.hasNext()) {
            MedicationRequest mReq = (MedicationRequest)iter.next();
            Date reqDate = mReq.getAuthoredOn();
            String actionRequired = "None";
            ConsentNotification.Status status = ConsentNotification.Status.ACTIVE;
            if (mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.CANCELLED)) {
                status = ConsentNotification.Status.CANCELLED;
                actionRequired = "None";
            }
            else if (mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.COMPLETED)) {
                status = ConsentNotification.Status.COMPLETE;
                actionRequired = "None";
            }
            else if (mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.DRAFT)) {
                status = ConsentNotification.Status.DRAFT;
                actionRequired = "None";
            }
            else if (mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.STOPPED)) {
                status = ConsentNotification.Status.STOPPED;
                actionRequired = "None";
            }
            else if (mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.ONHOLD)) {
                status = ConsentNotification.Status.ONHOLD;
                actionRequired = "Consent Required";
            }
            else if(mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.ENTEREDINERROR)) {
                status = ConsentNotification.Status.ENTEREDINERROR;
                actionRequired = "None";
            }
            else if(mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.UNKNOWN)) {
                status = ConsentNotification.Status.UNKNOWN;
                actionRequired = "None";
            }
            else if(mReq.getStatus().equals(MedicationRequest.MedicationRequestStatus.ACTIVE)) {
                status = ConsentNotification.Status.ACTIVE;
                actionRequired = "None";
            }
            else {
                status = ConsentNotification.Status.UNKNOWN;
                actionRequired = "None";
            }
            String shortName = mReq.getMedicationCodeableConcept().getCoding().get(0).getDisplay();
            String requestor = mReq.getRequester().getDisplay();
            String destination = "unknownview";
            //filter for ones we care about
            if (status.equals(ConsentNotification.Status.ACTIVE) || status.equals(ConsentNotification.Status.ONHOLD) || status.equals(ConsentNotification.Status.CANCELLED)) {
                ConsentNotification res = new ConsentNotification(reqDate, actionRequired, status, shortName, requestor, destination, mReq);
                reqList.add(res);
            }
        }

        return reqList;
    }

    private Collection<ConsentNotification> createResearchSubjectsArray() {
        Collection<ConsentNotification> reqList = new ArrayList<>();
        List<IBaseResource> subjectsList = fhirResearchSubject.getResearchSubjectsForSpecificPatientReference();
        Iterator iter = subjectsList.iterator();
        while(iter.hasNext()) {
            ResearchSubject subject = (ResearchSubject)iter.next();
            Date reqDate = subject.getMeta().getLastUpdated();
            String actionRequired = "None";
            ConsentNotification.Status status = ConsentNotification.Status.UNKNOWN;
            if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.POTENTIALCANDIDATE)) {
                status = ConsentNotification.Status.POTENTIALCANDIDATE;
                actionRequired = "Consent Required";
            }
            else if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.CANDIDATE)) {
                status = ConsentNotification.Status.CANDIDATE;
                actionRequired = "None";
            }
            else if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.SCREENING)) {
                status = ConsentNotification.Status.SCREENING;
                actionRequired = "None";
            }
            else if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.ELIGIBLE)) {
                status = ConsentNotification.Status.ELIGIBLE;
                actionRequired = "None";
            }
            else if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.INELIGIBLE)) {
                status = ConsentNotification.Status.INELIGIBLE;
                actionRequired = "None";
            }
            else if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.ONSTUDY)) {
                status = ConsentNotification.Status.ONSTUDY;
                actionRequired = "None";
            }
            else if (subject.getStatus().equals(ResearchSubject.ResearchSubjectStatus.WITHDRAWN)) {
                status = ConsentNotification.Status.WITHDRAWN;
                actionRequired = "None";
            }
            else {
                status = ConsentNotification.Status.UNKNOWN;
                actionRequired = "None";
            }
            String shortName = subject.getStudy().getReference();
            String requestor = subject.getStudy().getDisplay();
            String destination = "unknownview";
            ConsentNotification consentNotification = new ConsentNotification(reqDate, actionRequired, status, shortName, requestor, destination, subject);
            reqList.add(consentNotification);
        }
        return reqList;
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

    private Dialog createInfoDialog() {
        PDFInformedConsentHandler pdfHandler = new PDFInformedConsentHandler(pdfSigningService);
        StreamResource streamResource = pdfHandler.retrievePDFForm("antidepressants");

        Dialog infoDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button("Close");
        closeButton.addClickListener(event -> {
            infoDialog.close();
            reviewInformedConsentLayout.setVisible(false);
            patientSignatureLayout.setVisible(true);
        });
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

    private Dialog createRSInfoDialog() {
        StreamResource streamResource = setRSFieldsCreatePDF();

        Dialog infoDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button("Close");
        closeButton.addClickListener(event -> {
            rsInfoDialog.close();
            rsReviewInformedConsentLayout.setVisible(false);
            rsPatientSignatureLayout.setVisible(true);
        });
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
    private void createHumanReadable() {
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
            updateMedicationRequestStatus();
            successNotification("MedicationRequest");
            resetFormAndNavigation();

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

    private void createRSHumanReadable() {
        StreamResource streamResource = setRSFieldsCreatePDF();
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
            createResearchFHIRConsent();
            updateResearchSubjectStatus();
            successNotification("ResearchSubject");
            resetRSFormAndNavigation();
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
        String rxnormPreferredLabel = medicationName.getValue();
        String medicationRequestID = ((MedicationRequest)selectedConsentNotification.getFhirResource()).getIdElement().getIdPart();
        patientConsents = false;
        patientDeclines = false;
        //consider null
        try { patientConsents = treatmentAccepted.getValue(); } catch (Exception ex) {}
        try { patientDeclines = treatmentDeclined.getValue(); } catch (Exception ex) {}
        String patientName = attestationPatientName.getValue();
        String physicianName = attestationDRName.getValue();
        String signatureDate = getDateStringForDisplay(new Date());


        PDFInformedConsentHandler pdfHandler = new PDFInformedConsentHandler(pdfSigningService);
        StreamResource res = pdfHandler.updateAndRetrievePDFForm("antidepressants", rxnormPreferredLabel, medicationRequestID, patientConsents, patientDeclines,
                base64PatientSignature, patientName, signatureDate, base64PhysicianSignature, physicianName, signatureDate);

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;
    }

    private StreamResource setRSFieldsCreatePDF() {
        ResearchSubject subject = (ResearchSubject)selectedConsentNotification.getFhirResource();
        String studyId = subject.getStudy().getReference().replaceAll("ResearchStudy/","");
        Bundle studyBundle = fhirResearchStudy.getResearchStudy(studyId);
        ResearchStudy study = (ResearchStudy)studyBundle.getEntry().get(0).getResource();

        String sTitle = study.getTitle();
        String sDescription = study.getDescription();
        String sSponsoringOrgRef = study.getSponsor().getReference();
        String sSponsoringOrg = study.getSponsor().getDisplay();
        String orgId = sSponsoringOrgRef.replaceAll("Organization/","");
        Organization org = fhirOrganization.getOrganizationById(orgId);
        String city = org.getAddress().get(0).getCity();
        String state = org.getAddress().get(0).getState();
        String zip = org.getAddress().get(0).getPostalCode();

        String cityStateAndZip = city+" "+state+" "+zip;

        String sContactName = study.getContact().get(0).getName();
        String sContactEmailAddress = "";
        String sContactPhoneNumber = "";
        List<ContactPoint> cList = study.getContact().get(0).getTelecom();
        Iterator iter = cList.iterator();
        while (iter.hasNext()) {
            ContactPoint contactPoint = (ContactPoint) iter.next();
            if (contactPoint.getSystem().equals(ContactPoint.ContactPointSystem.EMAIL)) {
                sContactEmailAddress = contactPoint.getValue();
            }
            if (contactPoint.getSystem().equals(ContactPoint.ContactPointSystem.PHONE)) {
                sContactPhoneNumber = contactPoint.getValue();
            }
        }

        patientConsents = false;
        patientDeclines = false;
        //consider null
        try { patientConsents = participateAccepted.getValue(); } catch (Exception ex) {}
        try { patientDeclines = participateDeclined.getValue(); } catch (Exception ex) {}

        base64ResearchStudyPatientSignature = null;
        try { base64ResearchStudyPatientSignature = rsPatientSignature.getImageBase64(); } catch (Exception ex) {}
        String signatureDate = getDateStringForDisplay(new Date());
        String rsPatientName = consentUser.getFirstName()+" "+consentUser.getMiddleName()+" "+consentUser.getLastName();

        PDFResearchStudyHandler pdfHandler = new PDFResearchStudyHandler(pdfSigningService);
        StreamResource res = pdfHandler.updateAndRetrievePDFForm("research-study", sTitle, studyId, sDescription, patientConsents,
                patientDeclines, base64ResearchStudyPatientSignature, rsPatientName, signatureDate, sSponsoringOrg,
                cityStateAndZip, sContactName, sContactEmailAddress, sContactPhoneNumber );

        consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
        return res;

    }

    private void createFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        String medicationRequestID = ((MedicationRequest)selectedConsentNotification.getFhirResource()).getIdElement().getIdPart();

        Consent informedConsent = new Consent();

        informedConsent.setId("MedicationRequest-"+medicationRequestID+"-"+consentSession.getFhirPatientId());
        boolean consentGranted = false;
        boolean consentDeclined = false;
        try { consentGranted = treatmentAccepted.getValue(); } catch (Exception ex) {}
        try { consentDeclined = treatmentDeclined.getValue(); } catch (Exception ex) {}
        if (consentGranted) informedConsent.setStatus(Consent.ConsentState.ACTIVE);
        if (consentDeclined) informedConsent.setStatus(Consent.ConsentState.REJECTED);
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("treatment");
        cConcept.addCoding(coding);
        informedConsent.setScope(cConcept);
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        informedConsent.setCategory(cList);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        informedConsent.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        //todo - this is the deployment and custodian organization for advanced directives and should be valid in fhir consent repository
        orgRef.setReference(orgReference);
        orgRef.setDisplay(orgDisplay);
        refList.add(orgRef);
        informedConsent.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        attachment.setCreation(new Date());
        attachment.setTitle("InformedConsent");


        String encodedString = Base64.getEncoder().encodeToString(consentPDFAsByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        informedConsent.setSource(attachment);

        //set rule
        CodeableConcept policyCode = new CodeableConcept();
        Coding codes = new Coding();
        codes.setCode("OPTOUT");
        codes.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
        policyCode.addCoding(codes);
        informedConsent.setPolicyRule(policyCode);

        Consent.provisionComponent provision = new Consent.provisionComponent();
        Period period = new Period();
        LocalDate sDate = LocalDate.now();
        LocalDate eDate = LocalDate.now().plusYears(10);
        if (consentGranted) {
            sDate = LocalDate.now();
            eDate = LocalDate.now().plusYears(1);
        }
        if (consentDeclined) {
            sDate = LocalDate.now();
            eDate = LocalDate.now();
        }
        Date startDate = Date.from(sDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(eDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        period.setStart(startDate);
        period.setEnd(endDate);

        provision.setPeriod(period);

        Consent.provisionComponent purpose = new Consent.provisionComponent();



            Consent.provisionComponent requestorProvision = new Consent.provisionComponent();
            requestorProvision.setType(Consent.ConsentProvisionType.PERMIT);
            List<Coding> purposeList = new ArrayList<>();
            Coding purposeCoding = new Coding();
            purposeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActReason");
            purposeCoding.setCode("TREAT");
            purposeList.add(purposeCoding);
            requestorProvision.setPurpose(purposeList);

            //actor
            Consent.provisionActorComponent actor = new Consent.provisionActorComponent();
            CodeableConcept sensRoleConcept = new CodeableConcept();
            Coding sensRolecoding = new Coding();
            sensRolecoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            sensRolecoding.setCode("IRCP");
            sensRoleConcept.addCoding(sensRolecoding);
            actor.setRole(sensRoleConcept);

            String medRequestRequestorRef = ((MedicationRequest) selectedConsentNotification.getFhirResource()).getRequester().getReference();
            String medRequestRequestorName = ((MedicationRequest) selectedConsentNotification.getFhirResource()).getRequester().getDisplay();
            Reference actorRef = new Reference();
            actorRef.setReference(medRequestRequestorRef);
            actorRef.setDisplay(medRequestRequestorName);

            actor.setReference(actorRef);

            List<Consent.provisionActorComponent> sensActorList = new ArrayList<>();
            sensActorList.add(actor);

            requestorProvision.setActor(sensActorList);

            Coding sensactioncoding = new Coding();
            sensactioncoding.setSystem("http://terminology.hl7.org/CodeSystem/consentaction");
            sensactioncoding.setCode("access");

            Coding sensactioncodingcorrect = new Coding();
            sensactioncodingcorrect.setSystem("http://terminology.hl7.org/CodeSystem/consentaction");
            sensactioncodingcorrect.setCode("correct");

            List<CodeableConcept> sensActionCodeList = new ArrayList<>();
            CodeableConcept sensActionConcept = new CodeableConcept();
            sensActionConcept.addCoding(sensactioncoding);
            sensActionConcept.addCoding(sensactioncodingcorrect);
            sensActionCodeList.add(sensActionConcept);

            requestorProvision.setAction(sensActionCodeList);

            provision.addProvision(requestorProvision);




        informedConsent.setProvision(provision);

        Extension extension = createMedicationRequestExtension();
        informedConsent.getExtension().add(extension);

        fhirConsentClient.createConsent(informedConsent);
    }

    private void createResearchFHIRConsent() {
        Patient patient = consentSession.getFhirPatient();
        String researchSubjectID = ((ResearchSubject)selectedConsentNotification.getFhirResource()).getIdElement().getIdPart();
        ResearchSubject subject = (ResearchSubject)selectedConsentNotification.getFhirResource();
        String studyId = subject.getStudy().getReference().replaceAll("ResearchStudy/","");
        Bundle studyBundle = fhirResearchStudy.getResearchStudy(studyId);
        ResearchStudy study = (ResearchStudy)studyBundle.getEntry().get(0).getResource();

        String sponsoringOrgRef = study.getSponsor().getReference();
        String sponsoringOrg = study.getSponsor().getDisplay();

        Consent informedConsent = new Consent();

        informedConsent.setId("ResearchSubject-"+researchSubjectID+"-"+consentSession.getFhirPatientId());
        boolean consentGranted = false;
        boolean consentDeclined = false;
        try { consentGranted = participateAccepted.getValue(); } catch (Exception ex) {}
        try { consentDeclined = participateDeclined.getValue(); } catch (Exception ex) {}
        if (consentGranted) informedConsent.setStatus(Consent.ConsentState.ACTIVE);
        if (consentDeclined) informedConsent.setStatus(Consent.ConsentState.REJECTED);
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("research");
        cConcept.addCoding(coding);
        informedConsent.setScope(cConcept);
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        informedConsent.setCategory(cList);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        informedConsent.setPatient(patientRef);
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        //todo - this is the deployment and custodian organization for advanced directives and should be valid in fhir consent repository
        orgRef.setReference(orgReference);
        orgRef.setDisplay(orgDisplay);
        refList.add(orgRef);
        informedConsent.setOrganization(refList);
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        attachment.setCreation(new Date());
        attachment.setTitle("InformedConsent");


        String encodedString = Base64.getEncoder().encodeToString(consentPDFAsByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        informedConsent.setSource(attachment);

        //set rule
        CodeableConcept policyCode = new CodeableConcept();
        Coding codes = new Coding();
        codes.setCode("OPTOUT");
        codes.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
        policyCode.addCoding(codes);
        informedConsent.setPolicyRule(policyCode);

        Consent.provisionComponent provision = new Consent.provisionComponent();
        Period period = new Period();
        LocalDate sDate = LocalDate.now();
        LocalDate eDate = LocalDate.now().plusYears(10);
        if (consentGranted) {
            sDate = LocalDate.now();
            eDate = LocalDate.now().plusYears(1);
        }
        if (consentDeclined) {
            sDate = LocalDate.now();
            eDate = LocalDate.now();
        }
        Date startDate = Date.from(sDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(eDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        period.setStart(startDate);
        period.setEnd(endDate);

        provision.setPeriod(period);

        Consent.provisionComponent purpose = new Consent.provisionComponent();



        Consent.provisionComponent requestorProvision = new Consent.provisionComponent();
        requestorProvision.setType(Consent.ConsentProvisionType.PERMIT);
        List<Coding> purposeList = new ArrayList<>();
        Coding purposeCoding = new Coding();
        purposeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActReason");
        purposeCoding.setCode("HRESCH");
        purposeList.add(purposeCoding);
        requestorProvision.setPurpose(purposeList);

        //actor
        Consent.provisionActorComponent actor = new Consent.provisionActorComponent();
        CodeableConcept sensRoleConcept = new CodeableConcept();
        Coding sensRolecoding = new Coding();
        sensRolecoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
        sensRolecoding.setCode("IRCP");
        sensRoleConcept.addCoding(sensRolecoding);
        actor.setRole(sensRoleConcept);


        Reference actorRef = new Reference();
        actorRef.setReference(sponsoringOrgRef);
        actorRef.setDisplay(sponsoringOrg);

        actor.setReference(actorRef);

        List<Consent.provisionActorComponent> sensActorList = new ArrayList<>();
        sensActorList.add(actor);

        requestorProvision.setActor(sensActorList);

        Coding sensactioncoding = new Coding();
        sensactioncoding.setSystem("http://terminology.hl7.org/CodeSystem/consentaction");
        sensactioncoding.setCode("access");

        Coding sensactioncodingcorrect = new Coding();
        sensactioncodingcorrect.setSystem("http://terminology.hl7.org/CodeSystem/consentaction");
        sensactioncodingcorrect.setCode("correct");

        List<CodeableConcept> sensActionCodeList = new ArrayList<>();
        CodeableConcept sensActionConcept = new CodeableConcept();
        sensActionConcept.addCoding(sensactioncoding);
        sensActionConcept.addCoding(sensactioncodingcorrect);
        sensActionCodeList.add(sensActionConcept);

        requestorProvision.setAction(sensActionCodeList);

        provision.addProvision(requestorProvision);




        informedConsent.setProvision(provision);

        Extension extension = createResearchSubjectExtension();
        informedConsent.getExtension().add(extension);

        fhirConsentClient.createConsent(informedConsent);
    }

    private Extension createMedicationRequestExtension() {
        String medicationRequestFullPath = selectedConsentNotification.getFhirResource().getId();
        int pos = medicationRequestFullPath.indexOf("/_history");
        if (pos > 0) medicationRequestFullPath = medicationRequestFullPath.substring(0, pos);
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.org/leap/treatment/informedconsent");
        extension.setValue(new StringType(medicationRequestFullPath));
        return extension;
    }

    private Extension createResearchSubjectExtension() {
        String researchSubjectFullPath = selectedConsentNotification.getFhirResource().getId();
        int pos = researchSubjectFullPath.indexOf("/_history");
        if (pos > 0) researchSubjectFullPath = researchSubjectFullPath.substring(0, pos);
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.org/leap/research/informedconsent");
        extension.setValue(new StringType(researchSubjectFullPath));
        return extension;
    }

    private void successNotification(String type) {
        Span content = new Span("FHIR Informed Consent for "+type+" successfully created!");

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }

    private void resetFormAndNavigation() {
        //clear fields
        medicationName.clear();
        physicianName.clear();
        treatmentAccepted.clear();
        treatmentDeclined.clear();
        patientSignature.clear();
        attestationDRName.clear();
        attestationDate.clear();
        attestationPatientName.clear();
        physcianSignature.clear();
        reviewInformedConsentLayout.setVisible(false);
        patientSignatureLayout.setVisible(false);
        physicianSignatureLayout.setVisible(false);
        medRequestDataProvider = DataProvider.ofCollection(createMedicationRequestsArray());
        grid.setDataProvider(medRequestDataProvider);
        grid.getDataProvider().refreshAll();
        medicationRequestLayout.setVisible(true);
        UI.getCurrent().navigate("consentdocumentview");
    }

    private void resetRSFormAndNavigation() {
        rsTitleField.clear();
        participateDeclined.clear();
        participateAccepted.clear();
        rsPatientSignature.clear();
        rsReviewInformedConsentLayout.setVisible(false);
        rsPatientSignatureLayout.setVisible(false);
        clinicalTrialsDataProvider = DataProvider.ofCollection(createResearchSubjectsArray());
        clinicalTrialsGrid.setDataProvider(clinicalTrialsDataProvider);
        grid.getDataProvider().refreshAll();
        clinicalTrialsLayout.setVisible(true);
        UI.getCurrent().navigate("consentdocumentview");
    }

    private void updateMedicationRequestStatus() {
        MedicationRequest medicationRequest = (MedicationRequest)selectedConsentNotification.getFhirResource();
        if (patientConsents) {
            fhirMedicationRequestClient.consentGranted(medicationRequest);
        }
        if (patientDeclines) {
            fhirMedicationRequestClient.consentDeclined(medicationRequest);
        }
    }

    private void updateResearchSubjectStatus() {
        ResearchSubject researchSubject = (ResearchSubject)selectedConsentNotification.getFhirResource();
        String consentReference = "Consent/ResearchSubject-"+researchSubject.getIdElement().getIdPart()+"-"+consentSession.getFhirPatientId();
        Reference ref = new Reference();
        ref.setReference(consentReference);
        researchSubject.setConsent(ref);
        if (patientConsents) {
            fhirResearchSubject.consentGranted(researchSubject);
        }
        if (patientDeclines) {
            fhirResearchSubject.consentDeclined(researchSubject);
        }
    }

    public String getNctNumber() {
        return nctNumber;
    }

    public void setNctNumber(String nctNumber) {
        this.nctNumber = nctNumber;
    }
}
