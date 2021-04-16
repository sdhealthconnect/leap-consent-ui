package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.adr.model.ConsentNotification;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.Badge;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.layout.size.Vertical;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.hl7.fhir.r4.model.Consent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
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

    private FlexBoxLayout gridLayout;

    @Autowired
    private FHIRConsent fhirConsentClient;

    @Value("${org-reference:Organization/privacy-consent-scenario-H-healthcurrent}")
    private String orgReference;

    @Value("${org-display:HealthCurrent FHIR Connectathon}")
    private String orgDisplay;

    @PostConstruct
    public void setup() {
        setId("notificationview");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        setViewContent(getViewContent());
    }

    private Component getViewContent() {

        createGridLayout();
        FlexBoxLayout content = new FlexBoxLayout(gridLayout);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();

        return content;
    }

    private void createGridLayout() {
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

        gridLayout = new FlexBoxLayout(grid);
        gridLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        gridLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        gridLayout.setHeightFull();
        gridLayout.setBackgroundColor("white");
        gridLayout.setShadow(Shadow.S);
        gridLayout.setBorderRadius(BorderRadius.S);
        gridLayout.getStyle().set("margin-bottom", "10px");
        gridLayout.getStyle().set("margin-right", "10px");
        gridLayout.getStyle().set("margin-left", "10px");
        gridLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        gridLayout.setVisible(true);
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
            UI.getCurrent().navigate(consentNotification.getDestinationView());
        });
        if (consentNotification.getActionRequired().equals("None")) {
            btn.setEnabled(false);
        }
        return btn;
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
        patientPrivacyNotification = new ConsentNotification("Action Required", ConsentNotification.Status.NOTCOMPLETE, "patient-privacy",
                "Requires, at minimum, an exchange policy between your primary provider's organization and the hosting Healthcare Information Exchange(HIE).", "sharepatientdataview");
        //Advance Directives
        adrLivingWillNotification = new ConsentNotification("Action Required",ConsentNotification.Status.NOTCOMPLETE,"Advance Directive - Living Will",
                "Use this form to make decisions now about your medical care if you are ever in a terminal condition, a persistent vegetative state or an irreversible coma.",
                "livingwillview");
        adrPOAHealthCareNotification = new ConsentNotification("Action Required", ConsentNotification.Status.NOTCOMPLETE, "Advance Directive - Health Care Power of Attorney",
                "Helps you identify a person, called an \"agent\", to make future health care decisions for you so that if you become too ill or cannot make those decisions for yourself the person you choose and trust to make medical decisions for you.",
                "healthcarepowerofattorney");
        adrPOAMentalHealthNotification = new ConsentNotification("Optional", ConsentNotification.Status.NOTCOMPLETE, "Advance Directive - Mental Health Power of Attorney",
                "Helps you identify a person, also referred to as your \"agent\", to make future mental health care decisions for you if you become incapable of making those decisions for yourself.",
                "mentalhealthpowerofattorney");
        adrDNRNotification = new ConsentNotification("Optional", ConsentNotification.Status.NOTCOMPLETE, "Advance Directive - Do Not Resuscitate",
                "A document signed by you and your doctor that informs emergency medical technicians (EMTs) or hospital emergency personnel not to resuscitate you.",
                "dnrview");
        polstNotification = new ConsentNotification("Optional", ConsentNotification.Status.NOTCOMPLETE, "National Portable Medical Order",
                "Health care providers, the patient, or patient representative, should complete this form only after the " +
                        "health care provider has had a conversation with their patient or the patient’s representative.  " +
                        "The POLST decision-making process is for patients who are at risk for a life-threatening clinical event because they have a serious life-limiting medical " +
                        "condition, which may include advanced frailty.",
                "portablemedicalorderview");
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
}
