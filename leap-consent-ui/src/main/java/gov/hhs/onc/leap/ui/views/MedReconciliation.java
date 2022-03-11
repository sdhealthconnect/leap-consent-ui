package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRMedicationRequest;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRQuestionnaireResponse;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.backend.model.SDOHOrganization;
import gov.hhs.onc.leap.medrec.client.MedicationSummaryClient;
import gov.hhs.onc.leap.medrec.component.MedicationSummaryLayout;
import gov.hhs.onc.leap.medrec.model.MedicationSummary;
import gov.hhs.onc.leap.medrec.model.MedicationSummaryList;
import gov.hhs.onc.leap.sdoh.data.ACORNDisplayData;
import gov.hhs.onc.leap.session.ConsentSession;
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
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.*;

@PageTitle("Medication Reconciliation")
@Route(value = "medrec", layout = MainLayout.class)
public class MedReconciliation extends ViewFrame {

    private ConsentSession consentSession;
    private ConsentUser consentUser;
    @Value("${sh.url:http://localhost:8081}")
    private String shHost;
    private Grid activeMedicationsGrid;
    private FlexBoxLayout activeMedicationsLayout;
    private Button resetButton;
    private Button saveChanges;
    private String sourceType;
    private String sourceIdentifier;
    private GridListDataView<MedicationSummaryLayout> dataView;

    @Autowired
    private FHIRMedicationRequest fhirMedicationRequest;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @PostConstruct
    public void setup() {
        setId("medrec");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.consentUser = consentSession.getConsentUser();
        createActiveMedications();
        setViewContent(activeMedicationsLayout);
        setViewFooter(getFooter());
    }


    private Component getFooter() {
        resetButton = new Button("Reset", new Icon(VaadinIcon.RECYCLE));
        resetButton.addClickListener(event -> {
            refreshGrid();
        });
        resetButton.setVisible(true);
        saveChanges = new Button("Save Changes", new Icon(VaadinIcon.STORAGE));
        saveChanges.setIconAfterText(true);
        saveChanges.addClickListener(event -> {
            saveMedRecResponses();
        });
        saveChanges.setVisible(true);

        HorizontalLayout footer = new HorizontalLayout(resetButton, saveChanges);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(true);
        footer.setSpacing(true);
        return footer;
    }

    private void createActiveMedications() {
        Html medIntro = new Html("<p>Please review each <b>Active Medication</b>, and respond <b>\"Yes\"</b> " +
                "if you are still taking this medication as written, or <b>\"No\"</b> if you are no longer taking this medication,  or <b>\"Edit\"</b> " +
                "if you are taking this medication but not as written.</p>");
        activeMedicationsGrid = createActiveGrid();

        activeMedicationsLayout = new FlexBoxLayout(createHeader(VaadinIcon.CHECK_SQUARE, "Active Medications"),medIntro, new BasicDivider(), activeMedicationsGrid);
        activeMedicationsLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        activeMedicationsLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        activeMedicationsLayout.setHeightFull();
        activeMedicationsLayout.setBackgroundColor("white");
        activeMedicationsLayout.setShadow(Shadow.S);
        activeMedicationsLayout.setBorderRadius(BorderRadius.S);
        activeMedicationsLayout.getStyle().set("margin-bottom", "10px");
        activeMedicationsLayout.getStyle().set("margin-right", "10px");
        activeMedicationsLayout.getStyle().set("margin-left", "10px");
        activeMedicationsLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        activeMedicationsLayout.setVisible(true);
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

    private Grid createActiveGrid() {
        ListDataProvider<MedicationSummaryLayout> dataProvider = DataProvider.ofCollection(getMedications("Active"));

        Grid grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        dataView = grid.setItems(dataProvider);
        grid.setHeightFull();

        grid.addColumn(new ComponentRenderer<>(this::createMedComponent))
                .setWidth("100%");
        return grid;
    }

    private void refreshGrid() {
        ListDataProvider<MedicationSummaryLayout> dataProvider = DataProvider.ofCollection(getMedications("Active"));
        dataView = activeMedicationsGrid.setItems(dataProvider);
        dataView.refreshAll();
    }

    private Component createMedComponent(MedicationSummaryLayout medicationSummaryLayout) {
        //MedicationSummaryLayout medicationSummaryLayout = new MedicationSummaryLayout(medicationSummary);

        /*
        TextField commentsField = new TextField();
        commentsField.setLabel("Change/Comments");
        commentsField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        commentsField.setWidthFull();
        commentsField.setVisible(false);
        //commentsField.setEnabled(false);
        VerticalLayout v = new VerticalLayout();
        v.setWidthFull();
        ListItem listItem = new ListItem(medicationSummary.getMedication(), medicationSummary.getDosages());
        listItem.setPadding(Vertical.XS);
        //listItem.setWrapMode(FlexLayout.WrapMode.WRAP);
        RadioButtonGroup buttonGroup = new RadioButtonGroup();
        buttonGroup.setItems("Yes - I am taking this medication as written", "No - I am no longer taking this medication", "Edit - I need to indicate how I am taking this medication");
        buttonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        buttonGroup.addValueChangeListener(event -> {
            if (event.getValue().equals("Edit - I need to indicate how I am taking this medication")) {
                commentsField.setVisible(true);
            }
            else {
                commentsField.setValue("");
                commentsField.setVisible(false);
            }
        });

        v.add(listItem, buttonGroup, commentsField);
        */
        return medicationSummaryLayout;

    }

    private Collection<MedicationSummaryLayout> getMedications(String type) {
        Collection<MedicationSummary> medicationSummaryArrayList = new ArrayList<>();
        Collection<MedicationSummaryLayout> results = new ArrayList<>();
        MedicationSummaryClient client = new MedicationSummaryClient(shHost);
        MedicationSummaryList medicationSummaryList = client.getMedicationSummary(consentSession.getFhirPatientId());
        Iterator iter;
        if (type.equals("Active")) {
            medicationSummaryArrayList = medicationSummaryList.getActiveMedications();
            iter = medicationSummaryArrayList.iterator();
        }
        else {
            medicationSummaryArrayList = medicationSummaryList.getInactiveMedications();
            iter = medicationSummaryArrayList.iterator();
        }
        while (iter.hasNext()) {
            MedicationSummary medicationSummary = (MedicationSummary) iter.next();
            MedicationSummaryLayout l = new MedicationSummaryLayout(medicationSummary);
            results.add(l);
        }
        return results;
    }


    private QuestionnaireResponse createQuestionnaireResponse(boolean yes, boolean no, boolean edit, String comments) {
        QuestionnaireResponse resp = new QuestionnaireResponse();
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+consentSession.getFhirPatientId());
        resp.setAuthor(patientRef);
        resp.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        resp.setAuthored(new Date());
        resp.setId("medrec-"+consentSession.getFhirPatientId()+"-"+sourceType+"-"+sourceIdentifier);
        resp.setSubject(patientRef);
        resp.setQuestionnaire("Questionnaire/medrec-himss2022-demonstration");


        //create main item
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> mainItem = new ArrayList<>();
        mainItem.add(processDomain(yes, no, edit, comments));

        resp.setItem(mainItem);


        return resp;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent processDomain(boolean yes, boolean no, boolean edit, String comments) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent res = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        res.setLinkId("active-medication-reconciliation");
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> resList = new ArrayList<>();

        resList.add(getAnswerBoolean("1", yes, "Yes - I am taking this medication as written"));
        resList.add(getAnswerBoolean("2", no, "No - I am no longer taking this medication"));
        resList.add(getAnswerBoolean("3", edit, "Edit - I need to indicate how I am taking this medication"));
        resList.add(getAnswerString("4", comments, "Changes/Comments"));


        res.setItem(resList);
        return res;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent getAnswerBoolean(String linkId, boolean answerboolean, String question) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent fAnswer = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        fAnswer.setLinkId(linkId);
        fAnswer.setText(question);
        List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> fAnswerArray = new ArrayList<>();
        QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent fAnswerArrayItem = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
        BooleanType b = new BooleanType(answerboolean);
        fAnswerArrayItem.setValue(b);
        fAnswerArray.add(fAnswerArrayItem);
        fAnswer.setAnswer(fAnswerArray);
        return fAnswer;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent getAnswerString(String linkId, String answerString, String question) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent fAnswer = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        fAnswer.setLinkId(linkId);
        fAnswer.setText(question);
        List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> fAnswerArray = new ArrayList<>();
        QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent fAnswerArrayItem = new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent();
        StringType b = new StringType(answerString);
        fAnswerArrayItem.setValue(b);
        fAnswerArray.add(fAnswerArrayItem);
        fAnswer.setAnswer(fAnswerArray);
        return fAnswer;
    }


    private Extension createQuestionnaireResponseExtension(String medRecResponse) {
        Extension extension = new Extension();
        extension.setUrl("https://saperi.io/fhir/extensions/medreconciliation");
        StringType stringType = new StringType(medRecResponse);
        //extension.setProperty("keyMedRecResponse", stringType);
        extension.setValue(new StringType(consentSession.getFhirbase()+"QuestionnaireResponse/medrec-"+consentSession.getFhirPatientId()+"-"+sourceType+"-"+sourceIdentifier));
        return extension;
    }

    private void saveMedRecResponses() {
        int itemCount = dataView.getItemCount();
        int i;
        for (i = 0; i < itemCount; i++) {
            MedicationSummaryLayout layout = dataView.getItem(i);
            boolean yes = false;
            boolean no = false;
            boolean edit = false;
            String comments = "";
            String medRecResponse = layout.getButtonGroup().getValue().toString();
            sourceType = layout.getMedicationSummary().getType();
            sourceIdentifier = layout.getMedicationSummary().getFhirId();
            if(layout.getButtonGroup().getValue().toString().indexOf("Yes") > -1) yes = true;
            if (layout.getButtonGroup().getValue().toString().indexOf("No") > -1) no = true;
            if (layout.getButtonGroup().getValue().toString().indexOf("Edit") > -1) {
                edit = true;
                comments = layout.getCommentsField().getValue();
                medRecResponse = layout.getButtonGroup().getValue().toString()+" - "+comments;
            }
            QuestionnaireResponse response = createQuestionnaireResponse(yes, no, edit, comments);
            fhirQuestionnaireResponse.createQuestionnaireResponse(response);
            MedicationRequest medicationRequest = fhirMedicationRequest.getMedicationRequestByID(sourceIdentifier);
            //Extension ext = medicationRequest.getExtensionByUrl("https://saperi.io/fhir/extensions/medreconciliation");

            List<Extension> extensions = new ArrayList<>();
            extensions.add(createQuestionnaireResponseExtension(medRecResponse));
            medicationRequest.setExtension(extensions);

            fhirMedicationRequest.updateMedicationRequest(medicationRequest);
            System.out.println(layout.getMedicationSummary().getType()+" "+layout.getMedicationSummary().getFhirId()+" "+layout.getButtonGroup().getValue()+" "+layout.getCommentsField().getValue());


        }
        Notification notification = new Notification("Thank you for updating your medication record!");
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.setDuration(5000);
        notification.open();
        UI.getCurrent().navigate("");
    }
}
