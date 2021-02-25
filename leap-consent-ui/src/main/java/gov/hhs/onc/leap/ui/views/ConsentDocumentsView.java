package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.TabsVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.function.ContentTypeResolver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import gov.hhs.onc.leap.backend.TestData;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
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
import gov.hhs.onc.leap.ui.util.FontSize;
import gov.hhs.onc.leap.ui.util.LumoStyles;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.WhiteSpace;
import org.hl7.fhir.r4.model.Consent;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@PageTitle("Consent Documents")
@Route(value = "consentdocumentview", layout = MainLayout.class)
public class ConsentDocumentsView extends SplitViewFrame {

    private Grid<ConsentDocument> grid;
    private ListDataProvider<ConsentDocument> dataProvider;
    private DetailsDrawer detailsDrawer;
    private FHIRConsent fhirConsentClient = new FHIRConsent();
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
            appBar.addTab(status.getName());
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
        grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(this::showDetails));
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
                .setHeader("Status");
        grid.addColumn(new ComponentRenderer<>(this::createPolicyType))
                .setHeader("Policy Type")
                .setWidth("100px");
        grid.addColumn(new ComponentRenderer<>(this::createCustodian))
                .setHeader("Custodian")
                .setWidth("150px");
        grid.addColumn(new ComponentRenderer<>(this::createRecipient))
                .setHeader("Recipient")
                .setWidth("150px");
        grid.addColumn(TemplateRenderer.<ConsentDocument>of("[[item.startDate]]")
                .withProperty("startDate", consentDocument -> UIUtils.formatDate(consentDocument.getStartDate())))
                .setAutoWidth(true)
                .setComparator(ConsentDocument::getStartDate)
                .setFlexGrow(0)
                .setHeader("Effective Date");
        grid.addColumn(TemplateRenderer.<ConsentDocument>of("[[item.endDate]]")
                .withProperty("endDate", consentDocument -> UIUtils.formatDate(consentDocument.getEndDate())))
                .setAutoWidth(true)
                .setComparator(ConsentDocument::getEndDate)
                .setFlexGrow(0)
                .setHeader("Expires On");

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
        Tab details = new Tab("Details");
        Tab attachments = new Tab("Attachments");


        Tabs tabs = new Tabs(details, attachments);
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
                consentDocument.getStatus().getName(), "Status");

        status.getContent().setAlignItems(FlexComponent.Alignment.BASELINE);
        status.getContent().setSpacing(Bottom.XS);
        UIUtils.setTheme(consentDocument.getStatus().getTheme().getThemeName(),
                status.getPrimary());
        UIUtils.setTooltip(consentDocument.getStatus().getDesc(), status);

        ListItem policy = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD_ALT),
                consentDocument.getPolicyType() , "Policy Type");
        ListItem source = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.UPLOAD_ALT),
                consentDocument.getSource() , "Source");
        ListItem destination = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.DOWNLOAD_ALT),
                consentDocument.getDestination(), "Destination");
        ListItem startDate = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.CALENDAR),
                UIUtils.formatDate(consentDocument.getStartDate()), "Start Date");
        ListItem endDate = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.CALENDAR),
                UIUtils.formatDate(consentDocument.getEndDate()), "Expires On");
        ListItem sensitivity = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.TAG),
                consentDocument.getConstrainSensitivity(), "Constrain Sensitivity");
        ListItem domains = new ListItem(
                UIUtils.createTertiaryIcon(VaadinIcon.TAGS),
                consentDocument.getConstrainDomains(), "Constrain Domains");


        for (ListItem item : new ListItem[]{status, source, destination, startDate, endDate,
                sensitivity, domains}) {
            item.setReverse(true);
            item.setWhiteSpace(WhiteSpace.PRE_LINE);
        }

        Button consentAction = new Button("Patient Signature");
        if (consentDocument.getStatus().getName().equals("Active")) {
            consentAction.setText("Revoke Consent");
            consentAction.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.STOP));
        }
        else if (consentDocument.getStatus().getName().equals("Revoked")) {
            consentAction.setText("Reinstate Consent");
            consentAction.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.START_COG));
            if (consentDocument.getPolicyType().equals("adr")) {
                consentAction.setEnabled(false);
            }
            else {
                consentAction.setEnabled(true);
            }
        }
        else {
            consentAction.setText("No Actions Available");
            consentAction.setEnabled(false);
        }
        consentAction.addClickListener(clickEvent ->{
            if (consentAction.getText().equals("Revoke Consent")) {
                setConsentToRevoked();
            }
            else if (consentAction.getText().equals("Reinstate Consent")) {
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

        ListItem docTitle;
        ListItem docType;
        Button viewDocument = new Button("View Document");
        viewDocument.addClickListener(buttonClickEvent -> {
            createDocumentDialog();
            docDialog.open();
        });
        viewDocument.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_O));
        if (consentDocument.getFhirConsentResource().getSourceAttachment().getTitle() != null) {
            docTitle = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_TEXT),
                    consentDocument.getFhirConsentResource().getSourceAttachment().getTitle(), "Title");
            docType = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_CODE),
                    consentDocument.getFhirConsentResource().getSourceAttachment().getContentType(), "Content Type");
        }
        else {
            docTitle = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_TEXT),
                    new String("No Document(s) attached to this Consent."), "Title");
            docType = new ListItem(
                    UIUtils.createTertiaryIcon(VaadinIcon.FILE_CODE),
                    new String("Not Applicable"), "Content Type");
            viewDocument.setEnabled(false);
        }

        for (ListItem item : new ListItem[]{docTitle, docType}) {
            item.setReverse(true);
            item.setWhiteSpace(WhiteSpace.PRE_LINE);
        }

        Div attachments = new Div(docTitle, docType, viewDocument);
        attachments.addClassName(LumoStyles.Padding.Vertical.S);
        return attachments;

    }

    private void filter() {
        Tab selectedTab = MainLayout.get().getAppBar().getSelectedTab();
        if (selectedTab != null)
            dataProvider.setFilterByValue(ConsentDocument::getStatus, ConsentDocument.Status
                    .valueOf(selectedTab.getLabel().toUpperCase()));
    }

    private void showDetails(ConsentDocument consentDocument) {
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
                } else {
                    status = ConsentDocument.Status.ACTIVE;
                }
            }
            else {
                status = ConsentDocument.Status.ACTIVE;
            }
            String policyType = c.getScope().getCoding().get(0).getCode();
            String destination = "N/A";
            if (!policyType.equals("adr")) {
                destination = c.getProvision().getProvision().get(0).getActor().get(0).getReference().getDisplay();
            }
            String source = c.getOrganization().get(0).getDisplay();

            String constrainSensitivity = "No";
            String constrainDomains = "No";
            try {
                if (c.getProvision() != null) {
                    if (c.getProvision().getProvision().get(0).getSecurityLabel().get(0).getCode().equals("R")) {
                        //if (c.getProvision().getSecurityLabel().get(0).getCode().equals("R")) {
                            constrainSensitivity = "Yes";
                        //}
                    }
                }
            }
            catch (Exception ex) {

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

            CONSENT.put(i , new ConsentDocument(status, true, policyType, source, destination, localStart, localEnd, constrainSensitivity, constrainDomains, c));
            i++;
        }
        return CONSENT.values();
    }

    private void setConsentToRevoked() {
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        hideDetail();
        fhirConsentClient.revokeConsent(consent);
        dataProvider = DataProvider.ofCollection(getAllPatientConsents());
        grid.setDataProvider(dataProvider);
        grid.getDataProvider().refreshAll();
        MainLayout.get().getAppBar().getSelectedTab().setSelected(false);
    }

    private void setConsentToReinstated() {
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        hideDetail();
        fhirConsentClient.reinstateConsent(consent);
        dataProvider = DataProvider.ofCollection(getAllPatientConsents());
        grid.setDataProvider(dataProvider);
        grid.getDataProvider().refreshAll();
        MainLayout.get().getAppBar().getSelectedTab().setSelected(false);
    }

    private void createDocumentDialog() {
        Optional<ConsentDocument> ocd = grid.getSelectionModel().getFirstSelectedItem();
        ConsentDocument cd = ocd.get();
        Consent consent = cd.getFhirConsentResource();
        String docTitle = consent.getSourceAttachment().getTitle();
        byte[] docB = consent.getSourceAttachment().getData();
        byte[] docBdecoded = Base64.getDecoder().decode(docB);
        ByteArrayInputStream bais = new ByteArrayInputStream(docBdecoded);

        StreamResource streamResource = new StreamResource(
                docTitle, () -> {
            try {
                return new ByteArrayInputStream(docBdecoded);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
        docDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");

        Button closeButton = new Button("Close", e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        FlexBoxLayout content = new FlexBoxLayout(viewer, closeButton);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);

        docDialog.add(content);

        docDialog.setModal(false);
        docDialog.setResizable(true);
        docDialog.setDraggable(true);
    }
}
