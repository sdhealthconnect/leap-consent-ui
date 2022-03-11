package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRMedicationRequest;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRResearchSubject;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.Badge;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.components.detailsdrawer.DetailsDrawer;
import gov.hhs.onc.leap.ui.components.detailsdrawer.DetailsDrawerHeader;
import gov.hhs.onc.leap.ui.components.navigation.bar.AppBar;
import gov.hhs.onc.leap.ui.layout.size.Bottom;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.layout.size.Vertical;
import gov.hhs.onc.leap.ui.util.LumoStyles;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.WhiteSpace;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.hl7.fhir.r4.model.Attachment;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.ResearchSubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.imageio.ImageIO;
import javax.imageio.stream.IIOByteBuffer;
import javax.imageio.stream.ImageOutputStream;
import java.io.*;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@PageTitle("Consent Documents")
@Route(value = "consentdocumentview", layout = MainLayout.class)
public class ConsentDocumentsView extends SplitViewFrame {
    private static final Logger log = LoggerFactory.getLogger(ConsentDocumentsView.class);
    private Grid<ConsentDocument> grid;
    private ListDataProvider<ConsentDocument> dataProvider;
    private DetailsDrawer detailsDrawer;
    private MemoryBuffer uploadBuffer;
    private Upload upload;
    private Tabs tabs;
    private Tab details;
    private Tab attachments;

    @Autowired
    private FHIRConsent fhirConsentClient;

    @Autowired
    private FHIRMedicationRequest fhirMedicationRequest;

    @Autowired
    private FHIRResearchSubject fhirResearchSubject;

    private Dialog docDialog;

   @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initAppBar();
        setViewContent(createContent());
        setViewDetails(createDetailsDrawer());
        filter();
    }

    private void initAppBar() {
        AppBar appBar = MainLayout.get().getAppBar();
        for (ConsentDocument.Status status : ConsentDocument.Status.values()) {
            appBar.addTab(status.getName(), getTranslation("consentDocumentsView-status_" + status.getName()));
        }
        appBar.addTabSelectionListener(e -> {
            filter();
            detailsDrawer.hide();
        });
        appBar.centerTabs();
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout(createGrid());
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private Grid createGrid() {
        dataProvider = DataProvider.ofCollection(getAllPatientConsents());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresentOrElse(this::showDetails, this::hideDetail));
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();

        ComponentRenderer<Badge, ConsentDocument> badgeRenderer = new ComponentRenderer<>(
                consentDocument -> {
                    ConsentDocument.Status status = consentDocument.getStatus();
                    Badge badge = new Badge(status.getName(), status.getTheme());
                    UIUtils.setTooltip(status.getDesc(), badge);
                    return badge;
                }
        );
        grid.addColumn(badgeRenderer)
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setHeader(getTranslation("consentDocumentsView-status"));
        grid.addColumn(new ComponentRenderer<>(this::createPolicyType))
                .setHeader(getTranslation("consentDocumentsView-policy_type"))
                .setWidth("100px");
        grid.addColumn(new ComponentRenderer<>(this::createCustodian))
                .setHeader(getTranslation("consentDocumentsView-custodian"))
                .setWidth("150px");
        grid.addColumn(new ComponentRenderer<>(this::createRecipient))
                .setHeader(getTranslation("consentDocumentsView-recipient"))
                .setWidth("150px");
        grid.addColumn(TemplateRenderer.<ConsentDocument>of("[[item.startDate]]")
                .withProperty("startDate", consentDocument -> UIUtils.formatDate(consentDocument.getStartDate())))
                .setAutoWidth(true)
                .setComparator(ConsentDocument::getStartDate)
                .setFlexGrow(0)
                .setHeader(getTranslation("consentDocumentsView-effective_date"));
        grid.addColumn(TemplateRenderer.<ConsentDocument>of("[[item.endDate]]")
                .withProperty("endDate", consentDocument -> UIUtils.formatDate(consentDocument.getEndDate())))
                .setAutoWidth(true)
                .setComparator(ConsentDocument::getEndDate)
                .setFlexGrow(0)
                .setHeader(getTranslation("consentDocumentsView-expires_on"));

        return grid;
    }

    private Component createPolicyType(ConsentDocument consentDocument) {
        ListItem item = new ListItem(consentDocument.getPolicyType());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createCustodian(ConsentDocument consentDocument) {
        ListItem item = new ListItem(consentDocument.getSource());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Component createRecipient(ConsentDocument consentDocument) {
        ListItem item = new ListItem(consentDocument.getDestination());
        item.setPadding(Vertical.XS);
        return item;
    }

    private DetailsDrawer createDetailsDrawer() {
        detailsDrawer = new DetailsDrawer(DetailsDrawer.Position.RIGHT);

        // Header
        details = new Tab(getTranslation("consentDocumentsView-details"));
        attachments = new Tab(getTranslation("consentDocumentsView-attachments"));


        tabs = new Tabs(details, attachments);
        tabs.addThemeVariants(TabsVariant.LUMO_EQUAL_WIDTH_TABS);
        tabs.addSelectedChangeListener(e -> {
            Tab selectedTab = tabs.getSelectedTab();
            if (selectedTab.equals(details)) {
                detailsDrawer.setContent(createDetails(grid.getSelectionModel().getFirstSelectedItem().get()));
            } else if (selectedTab.equals(attachments)) {
                detailsDrawer.setContent(createAttachments(grid.getSelectionModel().getFirstSelectedItem().get()));
            }
        });

        DetailsDrawerHeader detailsDrawerHeader = new DetailsDrawerHeader("Consent Details", tabs);
        detailsDrawerHeader.addCloseListener(buttonClickEvent -> detailsDrawer.hide());
        detailsDrawer.setHeader(detailsDrawerHeader);

        return detailsDrawer;
    }

    private Component createDetails(ConsentDocument consentDocument) {
        ListItem status = new ListItem(consentDocument.getStatus().getIcon(),
                consentDocument.getStatus().getName(), getTranslation("consentDocumentsView-status"));

        status.getContent().setAlignItems(FlexComponent.Alignment.BASELINE);
        status.getContent().setSpacing(Bottom.XS);
        UIUtils.setTheme(consentDocument.getStatus().getTheme().getThemeName(),
                status.getPrimary());
        UIUtils.setTooltip(consentDocument.getStatus().getDesc(), status);

        ListItem policy = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD_ALT),
                consentDocument.getPolicyType() , getTranslation("consentDocumentsView-policy_type"));
        ListItem source = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD_ALT),
                consentDocument.getSource() , getTranslation("consentDocumentsView-source"));
        ListItem destination = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.DOWNLOAD_ALT),
                consentDocument.getDestination(),  getTranslation("consentDocumentsView-destination"));
        ListItem startDate = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.CALENDAR),
                UIUtils.formatDate(consentDocument.getStartDate()), getTranslation("consentDocumentsView-start_date"));
        ListItem endDate = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.CALENDAR),
                UIUtils.formatDate(consentDocument.getEndDate()), getTranslation("consentDocumentsView-expires_on"));
        ListItem sensitivity = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.TAG),
                consentDocument.getConstrainSensitivity(), getTranslation("consentDocumentsView-constrain_sensitivity"));
        ListItem domains = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.TAGS),
                consentDocument.getConstrainDomains(),  getTranslation("consentDocumentsView-constrain_domains"));


        for (ListItem item : new ListItem[]{status, source, destination, startDate, endDate,
                sensitivity, domains}) {
            item.setReverse(true);
            item.setWhiteSpace(WhiteSpace.PRE_LINE);
        }

        Button consentAction = new Button("Patient Signature");
        if (consentDocument.getStatus().getName().equals("Active")) {
            consentAction.setText(getTranslation("consentDocumentsView-status_Revoke_consent"));
            consentAction.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.STOP));
            consentAction.setEnabled(true);
        }
        else if (consentDocument.getStatus().getName().equals("Revoked")) {
            consentAction.setText(getTranslation("consentDocumentsView-reinstate_consent"));
            consentAction.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.START_COG));
            if (consentDocument.getPolicyType().contains("acd-") || consentDocument.getPolicyType().equals("treatment")  || consentDocument.getPolicyType().equals("research") || consentDocument.getPolicyType().equals("polst")) {
                consentAction.setEnabled(false);
            }
            else {
                consentAction.setEnabled(true);
            }
        }
        else if (consentDocument.getStatus().getName().equals("Pending")) {
            consentAction.setText(getTranslation("consentDocumentsView-status_Revoke_consent"));
            consentAction.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.STOP));
            consentAction.setEnabled(false);
        }
        else {
            consentAction.setText(getTranslation("consentDocumentsView-no_actions_available"));
            consentAction.setEnabled(false);
        }
        consentAction.addClickListener(clickEvent ->{
            if (consentAction.getText().equals(getTranslation("consentDocumentsView-status_Revoke_consent"))) {
                setConsentToRevoked();
            }
            else if (consentAction.getText().equals(getTranslation("consentDocumentsView-reinstate_consent"))) {
                setConsentToReinstated();
            }
            else {
                //do nothing
            }
        });

        Div details = new Div(status, source, destination, startDate, endDate, sensitivity, domains, consentAction);
        details.addClassName(LumoStyles.Padding.Vertical.S);
        return details;
    }

    private Component createAttachments(ConsentDocument consentDocument) {

        uploadBuffer = new MemoryBuffer();
        upload = new Upload(uploadBuffer);
        upload.setDropAllowed(true);
        upload.setVisible(true);

        upload.addSucceededListener(event -> {
            updateAttachmentAndStatus();
        });

        ListItem docTitle;
        ListItem docType;
        ListItem uploadAction;
        Button viewDocument = new Button(getTranslation("consentDocumentsView-view_document"));
        viewDocument.addClickListener(buttonClickEvent -> {
            createDocumentDialog();
            docDialog.open();
        });
        viewDocument.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_O));
        if (consentDocument.getFhirConsentResource().getSourceAttachment().getTitle() != null) {
            docTitle = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_TEXT),
                    consentDocument.getFhirConsentResource().getSourceAttachment().getTitle(), getTranslation("consentDocumentsView-title"));
            docType = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_CODE),
                    consentDocument.getFhirConsentResource().getSourceAttachment().getContentType(), getTranslation("consentDocumentsView-content_type"));
            if (consentDocument.getStatus().getName().equals(getTranslation("consentDocumentsView-status_Pending"))) {
                uploadAction = new ListItem(UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD),
                        getTranslation("consentDocumentsView-to_activate_upload_notarized_copy"), getTranslation("consentDocumentsView-user_action_required"));
                upload.setVisible(true);
            }
            else {
                uploadAction = new ListItem(UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD),
                        getTranslation("consentDocumentsView-none"), getTranslation("consentDocumentsView-user_action_required"));
                upload.setVisible(false);
            }
        }
        else {
            docTitle = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_TEXT),
                    new String(getTranslation("consentDocumentsView-no_documents_attached_to_this_consent")), getTranslation("consentDocumentsView-title"));
            docType = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_CODE),
                    new String(getTranslation("consentDocumentsView-not_applicable")), getTranslation("consentDocumentsView-content_type"));
            uploadAction = new ListItem(UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD),
                    getTranslation("consentDocumentsView-none"), getTranslation("consentDocumentsView-user_action_required"));
            upload.setVisible(false);
            viewDocument.setEnabled(false);
        }

        for (ListItem item : new ListItem[]{docTitle, docType, uploadAction}) {
            item.setReverse(true);
            item.setWhiteSpace(WhiteSpace.PRE_LINE);
        }

        Div attachments = new Div(docTitle, docType, uploadAction, upload, viewDocument);
        attachments.addClassName(LumoStyles.Padding.Vertical.S);
        return attachments;

    }

    private void filter() {
        Tab selectedTab = MainLayout.get().getAppBar().getSelectedTab();
        if (selectedTab != null)
            dataProvider.setFilterByValue(ConsentDocument::getStatus, ConsentDocument.Status
                    .valueOf(selectedTab.getId().get().toUpperCase()));
    }

    private void showDetails(ConsentDocument consentDocument) {
        tabs.setSelectedTab(details);
        detailsDrawer.setContent(createDetails(consentDocument));
        detailsDrawer.show();
    }

    private void hideDetail() {
        detailsDrawer.hide();
    }

    private Collection<ConsentDocument> getAllPatientConsents() {
        //public ConsentDocument(Status status, boolean consentState, String policyType, String source, String destination, LocalDate startDate, LocalDate endDate, String constrainSensitivity, String constrainDomains, Consent fhirConsentResource)
        Collection<Consent> consentList = fhirConsentClient.getPatientConsents();
        Map<Long, ConsentDocument> CONSENT = new HashMap<>();
        Iterator iter = consentList.iterator();
        long i = 0;
        while(iter.hasNext()) {
            Consent c = (Consent)iter.next();
            //determine state
            ConsentDocument.Status status;
            Consent.ConsentState consentState = c.getStatus();
            Date startDate = c.getProvision().getPeriod().getStart();
            Date endDate = c.getProvision().getPeriod().getEnd();
            if (endDate != null) {
                if (endDate.before(new Date()) && consentState.equals(Consent.ConsentState.ACTIVE)) {
                    status = ConsentDocument.Status.EXPIRED;
                } else if (consentState.equals(Consent.ConsentState.REJECTED)) {
                    status = ConsentDocument.Status.REVOKED;
                } else if (consentState.equals(Consent.ConsentState.INACTIVE)) {
                    status = ConsentDocument.Status.EXPIRED;
                } else if (consentState.equals(Consent.ConsentState.PROPOSED)) {
                    status = ConsentDocument.Status.PENDING;
                } else {
                    status = ConsentDocument.Status.ACTIVE;
                }
            }
            else {
                status = ConsentDocument.Status.ACTIVE;
            }
            String policyType = c.getCategory().get(0).getCoding().get(0).getCode();
            String destination = "N/A";
            try {
                if (!policyType.equals("acd")) {
                    if (policyType.equals("sdoh")) {
                        if (c.getProvision().getProvision().size() > 1) {
                            destination = "Multiple Organizations";
                        } else {
                            destination = c.getProvision().getProvision().get(0).getActor().get(0).getReference().getDisplay();
                        }
                    }
                    else {
                        destination = c.getProvision().getProvision().get(1).getActor().get(0).getReference().getDisplay();
                    }
                }
            }
            catch (Exception ex) {
                log.warn(policyType + " No Destination for patient-privacy scoped consent");
            }

            String source = c.getOrganization().get(0).getDisplay();

            String constrainSensitivity = "No";
            String constrainDomains = "No";
            try {
                if (c.getProvision() != null) {
                    List<Consent.provisionComponent> pList = c.getProvision().getProvision();
                    Iterator iter2 = pList.iterator();
                    while (iter2.hasNext()) {
                        Consent.provisionComponent pC = (Consent.provisionComponent)iter2.next();
                        try {
                            try {
                                if (pC.getSecurityLabel().get(0).getCode().equals("R")) {
                                    constrainSensitivity = "Yes";
                                }
                            }
                            catch (Exception ex) {}
                            try {
                                if (!pC.getClass_().isEmpty()) {
                                    constrainDomains = "Yes";
                                }
                            }
                            catch (Exception ex) {}
                        }
                        catch (Exception ex) {
                            log.warn("Error determining Sensitivity and Class Requirements: "+ex.getMessage()+" constrainSensitivity:"+constrainSensitivity+" constrainDomains: "+constrainDomains);
                        }
                    }
                }
            }
            catch (Exception ex) {
                log.warn("Error determining Sensitivity and Class Requirements: "+ex.getMessage()+" constrainSensitivity:"+constrainSensitivity+" constrainDomains: "+constrainDomains);
            }

            LocalDate localStart = LocalDate.of(2021, 2, 24);
            LocalDate localEnd = LocalDate.of(2031, 2, 23);
            if (startDate != null && endDate != null) {
                localStart = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                localEnd = endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }
            //augment if policy type is acd
            if (policyType.equals("acd")) {
                policyType = policyType +"-"+ c.getSourceAttachment().getTitle();
            }

            CONSENT.put(i , new ConsentDocument(status, true, policyType, source, destination, localStart, localEnd, constrainSensitivity, constrainDomains, c));
            i++;
        }
        return CONSENT.values();
    }

    private void setConsentToRevoked() {
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        fhirConsentClient.revokeConsent(consent);
        if (consent.getCategory().get(0).getCoding().get(0).getCode().equals("treatment")) {
            String extensionValue = consent.getExtension().get(0).getValue().toString();
            if (extensionValue.contains("MedicationRequest")) {
                revokeAndStopMedicationRequest(extensionValue);
            }
            else if (extensionValue.contains("ServiceRequest")) {
                //not implemented at this time
            }
            else {
                log.error("Unable to determine treatment type to be revoked.");
            }
        }
        else if (consent.getCategory().get(0).getCoding().get(0).getCode().equals("research")) {
            String extensionValue = consent.getExtension().get(0).getValue().toString();
            revokeAndWithdrawResearchSubject(extensionValue);
        }
        else {
            log.warn("Additional resources not considered in revocation based on scope.");
        }
        dataProvider = DataProvider.ofCollection(getAllPatientConsents());
        grid.setDataProvider(dataProvider);
        grid.getDataProvider().refreshAll();
        filter();
        detailsDrawer.hide();
    }

    private void setConsentToReinstated() {
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        fhirConsentClient.reinstateConsent(consent);
        dataProvider = DataProvider.ofCollection(getAllPatientConsents());
        grid.setDataProvider(dataProvider);
        grid.getDataProvider().refreshAll();
        filter();
        detailsDrawer.hide();
    }

    private void updateAttachmentAndStatus() {
        byte[] notarizedDocumentByteArray = null;
        try {
            InputStream in = uploadBuffer.getInputStream();
            ByteArrayInputStream bais = new ByteArrayInputStream(IOUtils.toByteArray(in));
            notarizedDocumentByteArray = bais.readAllBytes();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        Attachment attachment = consent.getSourceAttachment();
        attachment.setCreation(new Date());

        String encodedString = Base64.getEncoder().encodeToString(notarizedDocumentByteArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());
        consent.setSource(attachment);

        consent.setStatus(Consent.ConsentState.ACTIVE);

        fhirConsentClient.createConsent(consent);

        dataProvider = DataProvider.ofCollection(getAllPatientConsents());
        grid.setDataProvider(dataProvider);
        grid.getDataProvider().refreshAll();
        filter();
        detailsDrawer.hide();
    }

    private void createDocumentDialog() {
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        String docTitle = consent.getSourceAttachment().getTitle();
        byte[] docB = consent.getSourceAttachment().getData();
        byte[] docBdecoded = Base64.getDecoder().decode(docB);
        ByteArrayInputStream bais = new ByteArrayInputStream(docBdecoded);
        PdfBrowserViewer viewer = null;
        //if mobile use image layout
        //VerticalLayout imageLayout = new VerticalLayout();
        //imageLayout.setHeight("600px");
        //imageLayout.setWidth("640px");
        //imageLayout.setPadding(true);
        docDialog = new Dialog();
        Scroller scroller = new Scroller();
        scroller.setScrollDirection(Scroller.ScrollDirection.VERTICAL);
        Div div = new Div();
        if (isMobileDevice()) {
            try {
                PDDocument pdf = PDDocument.load(docBdecoded);
                PDFRenderer renderer = new PDFRenderer(pdf);
                int pageSize = pdf.getNumberOfPages();
                for (int i = 0; i < pageSize; i++) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(renderer.renderImageWithDPI(i, 96), "png", baos);
                    StreamResource stream = new StreamResource(docTitle+"-"+ i +".jpg", () -> new ByteArrayInputStream(baos.toByteArray()));
                    Image image = new Image(stream, docTitle+"-"+i);
                    image.setWidthFull();
                    div.add(image);
                }
                scroller.setContent(div);
                pdf.close();
            }
            catch (Exception ex) {
                log.error("Failed to create pdf image array for display. "+ex.getMessage());
            }
        }
        else {
            StreamResource streamResource = new StreamResource(
                    docTitle, () -> {
                try {
                    return new ByteArrayInputStream(docBdecoded);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            });


            streamResource.setContentType("application/pdf");

            viewer = new PdfBrowserViewer(streamResource);
            viewer.setHeight("800px");
            viewer.setWidth("840px");
        }
        Button closeButton = new Button("Close", e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        FlexBoxLayout content;
        if (isMobileDevice()) {
            content = new FlexBoxLayout(scroller, closeButton);
        }
        else {
            content = new FlexBoxLayout(viewer, closeButton);
        }
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        docDialog.add(content);

        docDialog.setModal(false);
        docDialog.setResizable(true);
        docDialog.setDraggable(true);
    }

    private boolean revokeAndStopMedicationRequest(String url) {
       boolean res = false;
       try {
           MedicationRequest medRequest = fhirMedicationRequest.getMedicationRequestByID(url);
           res = fhirMedicationRequest.consentRevoked(medRequest);
       }
       catch (Exception ex) {
           log.error("Failed revoke action for Medication request. "+ex.getMessage());
       }
       return res;
    }

    private boolean revokeAndWithdrawResearchSubject(String url) {
       boolean res = false;
       try {
           ResearchSubject researchSubject = fhirResearchSubject.getResearchSubjectByID(url);
           res = fhirResearchSubject.consentRevoked(researchSubject);
       }
       catch (Exception ex) {
           log.error("Failed to revoke consent and withdraw research subject "+ex.getMessage());
       }
       return res;
    }

    private  boolean isMobileDevice() {
       boolean res = false;
        WebBrowser webB = VaadinSession.getCurrent().getBrowser();
        System.out.println(webB.isMacOSX() +" "+webB.isSafari()+" "+webB.isAndroid()+" "+webB.isIPhone()+" "+webB.isWindowsPhone());
        System.out.println(webB.getBrowserApplication());
        if ((webB.isMacOSX() && webB.isSafari()) || webB.isAndroid() || webB.isIPhone() || webB.isWindowsPhone() || (webB.getBrowserApplication().indexOf("iPad") > -1))  {
            res = true;
        }
        else {
            res = false;
        }
        return res;
    }
}
