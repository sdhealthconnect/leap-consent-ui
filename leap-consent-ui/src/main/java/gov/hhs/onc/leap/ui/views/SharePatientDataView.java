package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.adr.model.QuestionnaireError;
import gov.hhs.onc.leap.backend.fhir.client.utils.*;
import gov.hhs.onc.leap.privacy.model.PatientPrivacy;
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
import gov.hhs.onc.leap.ui.util.pdf.PDFPatientPrivacyHandler;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.alejandro.PdfBrowserViewer;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;



@PageTitle("Share My Data")
@Route(value = "sharepatientdataview", layout = MainLayout.class)
public class SharePatientDataView extends ViewFrame {

    private static final Logger log = LoggerFactory.getLogger(SharePatientDataView.class);
    private RadioButtonGroup<String> consentDefaultPeriod;

    private DateTimePicker startDateTime;
    private DateTimePicker endDateTime;
    private ComboBox<Practitioner> practitionerComboBoxSource;
    private ComboBox<Practitioner> practitionerComboBoxDestination;
    private ComboBox<Organization> organizationComboBoxSource;
    private MultiSelectListBox<String> dataClassComboBox;
    private ComboBox<Organization> organizationComboBoxDestination;
    private ListDataProvider<Practitioner> practitionerListDataProvider;
    private ListDataProvider<Organization> organizationListDataProvider;
    private CheckboxGroup<String> sensitivityOptions;
    private Checkbox allSensitivityOptions;
    private FlexBoxLayout dateRequirements;
    private FlexBoxLayout dataClassRequirements;
    private FlexBoxLayout sourceRequirements;
    private FlexBoxLayout destinationRequirements;
    private FlexBoxLayout privacyRequirements;
    private FlexBoxLayout signatureRequirements;
    private int questionPosition = 0;
    private Button returnButton;
    private Button forwardButton;
    private Dialog dialog;
    private Dialog docDialog;
    private Dialog errorDialog;
    private byte[] base64Signature;
    private RadioButtonGroup<String> timeSettings;
    private RadioButtonGroup<String> constrainDataClass;
    private RadioButtonGroup<String> custodianType;
    private RadioButtonGroup<String> destinationType;
    private RadioButtonGroup<String> sensConstraints;

    private SignaturePad signature;
    private LocalDateTime provisionStartDateTime;
    private LocalDateTime provisionEndDateTime;
    private byte[] consentPDFAsByteArray;
    private List<QuestionnaireError> errorList;

    private Consent consentCompleted;
    private ConsentSession consentSession;
    private String consentProvenance;
    private String questionnaireProvenance;
    private Date dateRecordedProvenance;

    private QuestionnaireResponse questionnaireResponse;

    private List<QuestionnaireResponse.QuestionnaireResponseItemComponent> responseList;

    private String fhirBase;

    private PatientPrivacy privacy;

    @Autowired
    private FHIROrganization fhirOrganization;

    @Autowired
    private FHIRPractitioner fhirPractitioner;

    @Autowired
    private FHIRConsent fhirConsentClient;

    @Autowired
    private PDFSigningService pdfSigningService;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @Autowired
    private FHIRProvenance fhirProvenanceClient;


    @PostConstruct
    public void setup() {
        setId("sharepatientdataView");
        this.consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        this.responseList = new ArrayList<>();
        setViewContent(createViewContent());
        setViewFooter(getFooter());
        errorList = new ArrayList<>();
    }


    private Component createViewContent() {
        Html intro = new Html(getTranslation("sharePatient-intro"));

        createDateRequirements();
        createDataClassRequirements();
        createDataSourceRequirements();
        createDataDestinationRequirements();
        createPrivacyRequirements();
        createSignatureRequirements();

        FlexBoxLayout content = new FlexBoxLayout(intro, dateRequirements, dataClassRequirements, sourceRequirements,
                destinationRequirements, privacyRequirements, signatureRequirements);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;

    }

    private void createDateRequirements() {
        timeSettings = new RadioButtonGroup<>();
        timeSettings.setLabel(getTranslation("sharePatient-set_the_dates"));
        timeSettings.setItems(getTranslation("sharePatient-use_default_option"), getTranslation("sharePatient-custom_date_option"));
        timeSettings.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        timeSettings.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            } else {
                if (event.getValue().equals(getTranslation("sharePatient-use_default_option"))) {
                    consentDefaultPeriod.setVisible(true);
                    startDateTime.setVisible(false);
                    endDateTime.setVisible(false);
                }
                else if (event.getValue().equals(getTranslation("sharePatient-custom_date_option"))) {
                    consentDefaultPeriod.setVisible(false);
                    startDateTime.setVisible(true);
                    endDateTime.setVisible(true);
                }
                else {
                    consentDefaultPeriod.setVisible(false);
                    startDateTime.setVisible(false);
                    endDateTime.setVisible(false);
                }
            }
        });

        consentDefaultPeriod = new RadioButtonGroup<>();
        consentDefaultPeriod.setLabel(getTranslation("sharePatient-default_date_options"));
        consentDefaultPeriod.setItems(getTranslation("sharePatient-24_hours"), getTranslation("sharePatient-1_year"), getTranslation("sharePatient-5_years"), getTranslation("sharePatient-10_years"));
        consentDefaultPeriod.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        consentDefaultPeriod.setVisible(false);

        startDateTime = new DateTimePicker();
        startDateTime.setLabel(getTranslation("sharePatient-begin_enforcing"));
        startDateTime.setDatePlaceholder(getTranslation("sharePatient-date"));
        startDateTime.setTimePlaceholder(getTranslation("sharePatient-time"));
        startDateTime.setVisible(false);

        endDateTime = new DateTimePicker();
        endDateTime.setLabel(getTranslation("sharePatient-this_consent_will_no_longer_be_valid"));
        endDateTime.setDatePlaceholder(getTranslation("sharePatient-date"));
        endDateTime.setTimePlaceholder(getTranslation("sharePatient-time"));
        endDateTime.setVisible(false);

        dateRequirements = new FlexBoxLayout(createHeader(VaadinIcon.CALENDAR, getTranslation("sharePatient-date_requirements")),timeSettings, new BasicDivider(), consentDefaultPeriod, startDateTime, endDateTime);
        dateRequirements.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        dateRequirements.setBoxSizing(BoxSizing.BORDER_BOX);
        dateRequirements.setHeightFull();
        dateRequirements.setBackgroundColor("white");
        dateRequirements.setShadow(Shadow.S);
        dateRequirements.setBorderRadius(BorderRadius.S);
        dateRequirements.getStyle().set("margin-bottom", "10px");
        dateRequirements.getStyle().set("margin-right", "10px");
        dateRequirements.getStyle().set("margin-left", "10px");
        dateRequirements.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
    }

    private void createDataClassRequirements() {
        constrainDataClass = new RadioButtonGroup<>();
        constrainDataClass.setLabel(getTranslation("sharePatient-control_what_types_of_clinical_info_are_exchanged"));
        constrainDataClass.setItems(getTranslation("sharePatient-deny_access_to_following"), getTranslation("sharePatient-allow_all_types_of_data_to_be_exchanged"));
        constrainDataClass.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        constrainDataClass.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals(getTranslation("sharePatient-deny_access_to_following"))) {
                    dataClassComboBox.setVisible(true);
                }
                else {
                    dataClassComboBox.setVisible(false);
                }
            }
        });

        // todo dataClassComboBox make this a multiselect combo
        dataClassComboBox = new MultiSelectListBox<>();

        dataClassComboBox.setItems(getTranslation("sharePatient-adverseEvent"),
                getTranslation("sharePatient-allergyIntolerance"),
                getTranslation("sharePatient-appointment"),
                getTranslation("sharePatient-bodyStructure"),
                getTranslation("sharePatient-carePlan"),
                getTranslation("sharePatient-condition"),
                getTranslation("sharePatient-coverage"),
                getTranslation("sharePatient-diagnosticReport"),
                getTranslation("sharePatient-encounter"),
                getTranslation("sharePatient-episodeOfCare"),
                getTranslation("sharePatient-familyMemberHistory"),
                getTranslation("sharePatient-goal"),
                getTranslation("sharePatient-imagingStudy"),
                getTranslation("sharePatient-immunization"),
                getTranslation("sharePatient-insurancePlan"),
                getTranslation("sharePatient-measureReport"),
                getTranslation("sharePatient-medicationStatement"),
                getTranslation("sharePatient-observation"),
                getTranslation("sharePatient-patient"),
                getTranslation("sharePatient-relatedPerson"),
                getTranslation("sharePatient-researchSubject"),
                getTranslation("sharePatient-riskAssessment"),
                getTranslation("sharePatient-serviceRequest"),
                getTranslation("sharePatient-specimen"));
        WebBrowser browser = VaadinSession.getCurrent().getBrowser();
        if (browser.isAndroid() || browser.isIPhone() || browser.isWindowsPhone()) {
            dataClassComboBox.setHeight("150px");
        }
        else {
            dataClassComboBox.setHeight("400px");
        }
        dataClassComboBox.setVisible(false);

        dataClassRequirements = new FlexBoxLayout(createHeader(VaadinIcon.RECORDS, getTranslation("sharePatient-data_class_requirements")), constrainDataClass, new BasicDivider(), dataClassComboBox);
        dataClassRequirements.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        dataClassRequirements.setBoxSizing(BoxSizing.BORDER_BOX);
        dataClassRequirements.setHeightFull();
        dataClassRequirements.setBackgroundColor("white");
        dataClassRequirements.setShadow(Shadow.S);
        dataClassRequirements.setBorderRadius(BorderRadius.S);
        dataClassRequirements.getStyle().set("margin-bottom", "10px");
        dataClassRequirements.getStyle().set("margin-right", "10px");
        dataClassRequirements.getStyle().set("margin-left", "10px");
        dataClassRequirements.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        dataClassRequirements.setVisible(false);
    }

    private void createDataSourceRequirements() {
        Html practitionerLimitation = new Html(getTranslation("sharePatient-practitionerLimitation"));
        custodianType = new RadioButtonGroup<>();
        custodianType.setLabel(getTranslation("sharePatient-the_source_of_info_being_exchanged"));
        custodianType.setItems(getTranslation("sharePatient-practitioner"), getTranslation("sharePatient-organization"));
        custodianType.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        custodianType.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals(getTranslation("sharePatient-practitioner"))) {
                    practitionerComboBoxSource.setVisible(true);
                    practitionerComboBoxSource.setEnabled(false);
                    practitionerLimitation.setVisible(true);
                    organizationComboBoxSource.setVisible(false);
                }
                else if (event.getValue().equals(getTranslation("sharePatient-organization"))) {
                    practitionerComboBoxSource.setVisible(false);
                    practitionerComboBoxSource.setEnabled(false);
                    practitionerLimitation.setVisible(false);
                    organizationComboBoxSource.setVisible(true);
                }
                else {
                    practitionerComboBoxSource.setVisible(false);
                    practitionerComboBoxSource.setEnabled(false);
                    practitionerLimitation.setVisible(false);
                    organizationComboBoxSource.setVisible(false);
                }
            }
        });

        practitionerListDataProvider = DataProvider.ofCollection(getPractitioners());
        organizationListDataProvider = DataProvider.ofCollection(getOrganizations());

        practitionerComboBoxSource = new ComboBox<>();
        practitionerComboBoxSource.setLabel(getTranslation("sharePatient-practitioner_custodian"));
        practitionerComboBoxSource.setItemLabelGenerator(practitioner -> practitioner.getName().get(0).getNameAsSingleString());
        practitionerComboBoxSource.setItems(practitionerListDataProvider);
        practitionerComboBoxSource.setVisible(false);

        organizationComboBoxSource = new ComboBox<>();
        organizationComboBoxSource.setLabel(getTranslation("sharePatient-organization_custodian"));
        organizationComboBoxSource.setItemLabelGenerator(organization -> organization.getName());
        organizationComboBoxSource.setItems(organizationListDataProvider);
        organizationComboBoxSource.setVisible(false);

        practitionerLimitation.setVisible(false);

        sourceRequirements = new FlexBoxLayout(createHeader(VaadinIcon.DOCTOR, getTranslation("sharePatient-data_source_custodian")),custodianType, new BasicDivider(), practitionerComboBoxSource, organizationComboBoxSource, practitionerLimitation);
        sourceRequirements.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        sourceRequirements.setBoxSizing(BoxSizing.BORDER_BOX);
        sourceRequirements.setHeightFull();
        sourceRequirements.setBackgroundColor("white");
        sourceRequirements.setShadow(Shadow.S);
        sourceRequirements.setBorderRadius(BorderRadius.S);
        sourceRequirements.getStyle().set("margin-bottom", "10px");
        sourceRequirements.getStyle().set("margin-right", "10px");
        sourceRequirements.getStyle().set("margin-left", "10px");
        sourceRequirements.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        sourceRequirements.setVisible(false);
    }

    private void createDataDestinationRequirements() {
        destinationType = new RadioButtonGroup<>();
        destinationType.setLabel(getTranslation("sharePatient-the_person_or_org_requesting_your_info"));
        destinationType.setItems(getTranslation("sharePatient-practitioner"), getTranslation("sharePatient-organization"));
        destinationType.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        destinationType.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals(getTranslation("sharePatient-practitioner"))) {
                    practitionerComboBoxDestination.setVisible(true);
                    organizationComboBoxDestination.setVisible(false);
                }
                else if (event.getValue().equals(getTranslation("sharePatient-organization"))) {
                    practitionerComboBoxDestination.setVisible(false);
                    organizationComboBoxDestination.setVisible(true);
                }
                else {
                    practitionerComboBoxDestination.setVisible(false);
                    organizationComboBoxDestination.setVisible(false);
                }
            }
        });

        practitionerComboBoxDestination = new ComboBox<>();
        practitionerComboBoxDestination.setLabel(getTranslation("sharePatient-practitioner_recipient"));
        practitionerComboBoxDestination.setItemLabelGenerator(practitioner -> practitioner.getName().get(0).getNameAsSingleString());
        practitionerComboBoxDestination.setItems(practitionerListDataProvider);
        practitionerComboBoxDestination.setVisible(false);

        organizationComboBoxDestination = new ComboBox<>();
        organizationComboBoxDestination.setLabel(getTranslation("sharePatient-organization_recipient"));
        organizationComboBoxDestination.setItemLabelGenerator(organization -> organization.getName());
        organizationComboBoxDestination.setItems(organizationListDataProvider);
        organizationComboBoxDestination.setVisible(false);

        destinationRequirements = new FlexBoxLayout(createHeader(VaadinIcon.HOSPITAL, getTranslation("sharePatient-destination_recipient")),destinationType, new BasicDivider(), practitionerComboBoxDestination, organizationComboBoxDestination);
        destinationRequirements.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        destinationRequirements.setBoxSizing(BoxSizing.BORDER_BOX);
        destinationRequirements.setHeightFull();
        destinationRequirements.setBackgroundColor("white");
        destinationRequirements.setShadow(Shadow.S);
        destinationRequirements.setBorderRadius(BorderRadius.S);
        destinationRequirements.getStyle().set("margin-bottom", "10px");
        destinationRequirements.getStyle().set("margin-right", "10px");
        destinationRequirements.getStyle().set("margin-left", "10px");
        destinationRequirements.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        destinationRequirements.setVisible(false);
    }

    private void createPrivacyRequirements() {
        Html enforceLimitation = new Html(getTranslation("sharePatient-enforceLimitation"));
        sensConstraints = new RadioButtonGroup<>();
        sensConstraints.setLabel(getTranslation("sharePatient-sensConstraints"));
        sensConstraints.setItems(getTranslation("sharePatient-remove_them"), getTranslation("sharePatient-i_do_not_have_privacy_concerns"));
        sensConstraints.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        sensConstraints.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //nothing
            }
            else {
                if (event.getValue().equals(getTranslation("sharePatient-remove_them"))) {
                    allSensitivityOptions.setVisible(true);
                    sensitivityOptions.setVisible(true);
                    enforceLimitation.setVisible(true);
                }
                else if(event.getValue().equals(getTranslation("sharePatient-i_do_not_have_privacy_concerns"))){
                    allSensitivityOptions.setVisible(false);
                    sensitivityOptions.setVisible(false);
                    enforceLimitation.setVisible(false);
                }
                else {
                    allSensitivityOptions.setVisible(false);
                    sensitivityOptions.setVisible(false);
                    enforceLimitation.setVisible(false);
                }
            }
        });

        allSensitivityOptions = new Checkbox();
        allSensitivityOptions.setLabel(getTranslation("sharePatient-sensitivity_options"));
        Set<String> items = new LinkedHashSet<>(
                Arrays.asList(getTranslation("sharePatient-eth"),
                        getTranslation("sharePatient-hiv"),
                        getTranslation("sharePatient-psy"),
                        getTranslation("sharePatient-sickle"),
                        getTranslation("sharePatient-std")));
        sensitivityOptions = new CheckboxGroup<>();
        sensitivityOptions.setItems(items);
        sensitivityOptions.addThemeVariants(CheckboxGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        sensitivityOptions.addValueChangeListener(event -> {
            if (event.getValue().size() == items.size()) {
                allSensitivityOptions.setValue(true);
                allSensitivityOptions.setIndeterminate(false);
            } else if (event.getValue().size() == 0) {
                allSensitivityOptions.setValue(false);
                allSensitivityOptions.setIndeterminate(false);
            } else
                allSensitivityOptions.setIndeterminate(true);

        });
        allSensitivityOptions.addValueChangeListener(event -> {

            if (allSensitivityOptions.getValue()) {
                sensitivityOptions.setValue(items);
            } else {
                sensitivityOptions.deselectAll();
            }
        });
        allSensitivityOptions.setVisible(false);
        allSensitivityOptions.setValue(true);
        sensitivityOptions.setVisible(false);
        enforceLimitation.setVisible(false);
        allSensitivityOptions.setEnabled(false);
        sensitivityOptions.setEnabled(false);

        privacyRequirements = new FlexBoxLayout(createHeader(VaadinIcon.GLASSES, getTranslation("sharePatient-privacy_concerns")),sensConstraints, new BasicDivider(), allSensitivityOptions, sensitivityOptions, enforceLimitation);
        privacyRequirements.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        privacyRequirements.setBoxSizing(BoxSizing.BORDER_BOX);
        privacyRequirements.setHeightFull();
        privacyRequirements.setBackgroundColor("white");
        privacyRequirements.setShadow(Shadow.S);
        privacyRequirements.setBorderRadius(BorderRadius.S);
        privacyRequirements.getStyle().set("margin-bottom", "10px");
        privacyRequirements.getStyle().set("margin-right", "10px");
        privacyRequirements.getStyle().set("margin-left", "10px");
        privacyRequirements.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        privacyRequirements.setVisible(false);
    }

    private void createSignatureRequirements() {
        Html eSignLabel = new Html(getTranslation("sharePatient-eSignLabel"));
        Button eSignButton = new Button(getTranslation("sharePatient-eSign_consent_and_submit"));
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

    private Collection<Practitioner> getPractitioners() {
        Collection<Practitioner> cPract = fhirPractitioner.getAllPractitionersWithinState();

        return cPract;
    }

    private Collection<Organization> getOrganizations() {
        Collection<Organization> cOrg = fhirOrganization.getAllOrganizationsWithinState();

        return cOrg;
    }

    private Component getFooter() {
        returnButton = new Button(getTranslation("sharePatient-back"), new Icon(VaadinIcon.BACKWARDS));
        returnButton.setEnabled(false);
        returnButton.addClickListener(event -> {
            questionPosition--;
            evalNavigation();
        });
        forwardButton = new Button(getTranslation("sharePatient-next"), new Icon(VaadinIcon.FORWARD));
        forwardButton.setIconAfterText(true);
        forwardButton.addClickListener(event -> {
           questionPosition++;
           evalNavigation();
        });


        HorizontalLayout footer = new HorizontalLayout(returnButton, forwardButton);
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(true);
        footer.setSpacing(true);
        return footer;
    }

    private void evalNavigation() {
        switch(questionPosition) {
            case 0:
                returnButton.setEnabled(false);
                forwardButton.setEnabled(true);
                dateRequirements.setVisible(true);
                dataClassRequirements.setVisible(false);
                sourceRequirements.setVisible(false);
                destinationRequirements.setVisible(false);
                privacyRequirements.setVisible(false);
                signatureRequirements.setVisible(false);
                break;
            case 1:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                dateRequirements.setVisible(false);
                dataClassRequirements.setVisible(true);
                sourceRequirements.setVisible(false);
                destinationRequirements.setVisible(false);
                privacyRequirements.setVisible(false);
                signatureRequirements.setVisible(false);
                break;
            case 2:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                dateRequirements.setVisible(false);
                dataClassRequirements.setVisible(false);
                sourceRequirements.setVisible(true);
                destinationRequirements.setVisible(false);
                privacyRequirements.setVisible(false);
                signatureRequirements.setVisible(false);
                break;
            case 3:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                dateRequirements.setVisible(false);
                dataClassRequirements.setVisible(false);
                sourceRequirements.setVisible(false);
                destinationRequirements.setVisible(true);
                privacyRequirements.setVisible(false);
                signatureRequirements.setVisible(false);
                break;
            case 4:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(true);
                dateRequirements.setVisible(false);
                dataClassRequirements.setVisible(false);
                sourceRequirements.setVisible(false);
                destinationRequirements.setVisible(false);
                privacyRequirements.setVisible(true);
                signatureRequirements.setVisible(false);
                break;
            case 5:
                returnButton.setEnabled(true);
                forwardButton.setEnabled(false);
                dateRequirements.setVisible(false);
                dataClassRequirements.setVisible(false);
                sourceRequirements.setVisible(false);
                destinationRequirements.setVisible(false);
                privacyRequirements.setVisible(false);
                signatureRequirements.setVisible(true);
                break;
            default:
                break;
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

        Button saveSig = new Button(getTranslation("sharePatient-done"));
        saveSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveSig.addClickListener(event -> {
            base64Signature = signature.getImageBase64();
            //todo create fhir consent resource and pdf for review in flow and final submittal of consent
            dialog.close();
            getHumanReadable();
            docDialog.open();

        });
        Button cancelSign = new Button(getTranslation("sharePatient-cancel"));
        cancelSign.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CLOSE));
        cancelSign.addClickListener(event -> {
            dialog.close();
        });
        Html signHere = new Html(getTranslation("sharePatient-sign_here"));
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

    private StreamResource setFieldsCreatePDF() {

            //get consent period
            privacy = new PatientPrivacy();  //capture for questionnaire response
            String sDate = "";
            String eDate = "";
            LocalDateTime defDate = LocalDateTime.now();
            String dataDomainConstraintlist = getTranslation("sharePatient-deny_access_to_following");
            String custodian = "";
            String recipient = "";
            String sensitivities = getTranslation("sharePatient-remove_following_sensitivity_types_if_found_in_my_record");
            try {
                if (timeSettings.getValue().equals(getTranslation("sharePatient-use_default_option"))) {
                    privacy.setUseDefaultOptions(true);
                    if (consentDefaultPeriod.getValue().equals(getTranslation("sharePatient-24_hours"))) {
                        defDate = LocalDateTime.now().plusDays(1);
                        privacy.setOneDay(true);
                    } else if (consentDefaultPeriod.getValue().equals(getTranslation("sharePatient-1_year"))) {
                        defDate = LocalDateTime.now().plusYears(1);
                        privacy.setOneYear(true);
                    } else if (consentDefaultPeriod.getValue().equals(getTranslation("sharePatient-5_years"))) {
                        defDate = LocalDateTime.now().plusYears(5);
                        privacy.setFiveYears(true);
                    } else if (consentDefaultPeriod.getValue().equals(getTranslation("sharePatient-10_years"))) {
                        defDate = LocalDateTime.now().plusYears(10);
                        privacy.setTenYears(true);
                    } else {
                        errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_default_date_selected"), 0));
                        defDate = null;
                    }
                    sDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    eDate = defDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    //for use later
                    provisionStartDateTime = LocalDateTime.now();
                    provisionEndDateTime = defDate;
                    privacy.setDefaultOptionStartDate(provisionStartDateTime);
                    privacy.setDefaultOptionEndDate(provisionEndDateTime);
                } else if (timeSettings.getValue().equals(getTranslation("sharePatient-custom_date_option"))) {
                    privacy.setUseCustomDateOption(true);
                    sDate = startDateTime.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    eDate = endDateTime.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    //for use later
                    provisionStartDateTime = startDateTime.getValue();
                    provisionEndDateTime = endDateTime.getValue();
                    if (provisionStartDateTime == null || provisionEndDateTime == null) {
                        errorList.add(new QuestionnaireError(getTranslation("sharePatient-custom_date_can_not_be_blank"), 0));
                    }
                    if (provisionEndDateTime.isBefore(provisionStartDateTime)) {
                        errorList.add(new QuestionnaireError(getTranslation("sharePatient-custom_date_can_not_be_before_start_date"), 0));
                    }
                    if (provisionEndDateTime.isBefore(defDate)) {
                        errorList.add(new QuestionnaireError(getTranslation("sharePatient-custom_date_can_not_be_before_today"), 0));
                    }
                    privacy.setCustomEndDate(provisionEndDateTime);
                    privacy.setCustomStartDate(provisionStartDateTime);
                } else {
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_date_range_seleccion_made"), 0));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_date_range_seleccion_made"), 0));
            }
            //get domain constraints
            try {
                if (constrainDataClass.getValue().equals(getTranslation("sharePatient-deny_access_to_following"))) {
                    privacy.setLimitDataClasses(true);
                    Set<String> classList = dataClassComboBox.getSelectedItems();
                    privacy.setDataClassList(classList);
                    Iterator iterClass = classList.iterator();
                    while (iterClass.hasNext()) {
                        String s = (String) iterClass.next();
                        dataDomainConstraintlist = dataDomainConstraintlist + s + " ";
                    }
                    if (classList.isEmpty()) {
                        errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_data_class_exceptions_found_in_list"), 1));
                    }
                } else {
                    //this is the default when none selected
                    privacy.setNoDataClassConstraints(true);
                    dataDomainConstraintlist = getTranslation("sharePatient-allow_all_types_of_data_to_be_exchanged");
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_data_class_selection_made"), 1));
            }
            //set custodian
            try {
                if (custodianType.getValue().equals(getTranslation("sharePatient-practitioner"))) {
                    custodian = practitionerComboBoxSource.getValue().getName().get(0).getNameAsSingleString();
                    privacy.setPractitionerDataSource(true);
                    privacy.setPractitionerName(custodian);
                } else if (custodianType.getValue().equals(getTranslation("sharePatient-organization"))) {
                    custodian = organizationComboBoxSource.getValue().getName();
                    privacy.setOrganizationDataSource(true);
                    privacy.setOrganizationName(custodian);
                } else {
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_custodian_selection_made"), 2));
                }
                if (custodian.equals("") || custodian.isEmpty()) {
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_custodian_selection_made"), 2));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_custodian_selection_made"), 2));
            }
            //set recipient
            try {

                if (destinationType.getValue().equals(getTranslation("sharePatient-practitioner"))) {
                    recipient = practitionerComboBoxDestination.getValue().getName().get(0).getNameAsSingleString();
                } else if (destinationType.getValue().equals(getTranslation("sharePatient-organization"))) {
                    recipient = organizationComboBoxDestination.getValue().getName();
                } else {
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_recipient_destination_selection_made"), 3));
                }
                if (recipient.equals("") || recipient.isEmpty()) {
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_recipient_destination_selection_made"), 3));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_recipient_destination_selection_made"), 3));
            }

            //set sensitivity constraints
            try {

                if (sensConstraints.getValue().equals(getTranslation("sharePatient-remove_them"))) {
                    privacy.setRemoveThem(true);
                    Set<String> sensSet = sensitivityOptions.getSelectedItems();
                    privacy.setRemoveAllLabeledConfidential(true);
                    Iterator sensIter = sensSet.iterator();
                    while (sensIter.hasNext()) {
                        String s = (String) sensIter.next();
                        sensitivities = sensitivities + s + " ";
                    }
                } else if (sensConstraints.getValue().equals(getTranslation("sharePatient-i_do_not_have_privacy_concerns"))) {
                    privacy.setNoPrivacyConcerns(true);
                    sensitivities = getTranslation("sharePatient-i_do_not_have_privacy_concerns");
                } else {
                    //default if none selected
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_privacy_concern_selected"), 4));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError(getTranslation("sharePatient-no_privacy_concern_selection_made"), 4));
            }
            try {
                if (base64Signature.length == 0) {
                    errorList.add(new QuestionnaireError(getTranslation("sharePatient-user_signature_can_not_be_blank"), 5));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError(getTranslation("sharePatient-user_signature_can_not_be_blank"), 5));
            }
            if (errorList.size() > 0) {
                return null;
            }
            if (base64Signature.length > 0) {
                privacy.setAcceptedAndSignaturedCaptured(true);
            }
            PDFPatientPrivacyHandler pdfHandler = new PDFPatientPrivacyHandler(pdfSigningService);
            StreamResource res = pdfHandler.retrievePDFForm(sDate, eDate, dataDomainConstraintlist, custodian,
                                recipient, sensitivities, base64Signature);
            consentPDFAsByteArray = pdfHandler.getPdfAsByteArray();
            return  res;
    }

    private void getHumanReadable() {
        StreamResource streamResource = setFieldsCreatePDF();
        if (streamResource == null) {
            return;
        }
        docDialog = new Dialog();

        streamResource.setContentType("application/pdf");

        PdfBrowserViewer viewer = new PdfBrowserViewer(streamResource);
        viewer.setHeight("800px");
        viewer.setWidth("840px");


        Button closeButton = new Button(getTranslation("sharePatient-cancel"), e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button(getTranslation("sharePatient-accept_and_submit"));
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            docDialog.close();
            createQuestionnaireResponse();
            createFHIRConsent();
            createFHIRProvenance();
            successNotification();
            //todo test for fhir consent create success
            resetQuestionNavigation();
            evalNavigation();
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

    private void createFHIRConsent() {
        fhirBase = consentSession.getFhirbase();
        Patient patient = consentSession.getFhirPatient();

        Consent patientPrivacyConsent = new Consent();
        patientPrivacyConsent.setId(UUID.randomUUID().toString());
        consentProvenance="Consent/"+patientPrivacyConsent.getId();
        patientPrivacyConsent.setStatus(Consent.ConsentState.ACTIVE);

        patientPrivacyConsent.setDateTime(new Date());

        //set consent categories [0] is core that UI relies on for display purposes
        List<CodeableConcept> cList = new ArrayList<>();
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentcategorycodes");
        coding.setCode("patient-privacy");
        cConcept.addCoding(coding);
        cList.add(cConcept);

        CodeableConcept cConceptCat = new CodeableConcept();
        Coding codingCat = new Coding();
        codingCat.setSystem("http://loinc.org");
        codingCat.setCode("59284-6");
        cConceptCat.addCoding(codingCat);
        cList.add(cConceptCat);
        patientPrivacyConsent.setCategory(cList);

        //set patient ref
        Reference patientRef = new Reference();
        patientRef.setReference(patient.getId().replace(fhirBase, ""));
        patientRef.setDisplay(patient.getName().get(0).getFamily()+", "+patient.getName().get(0).getGiven().get(0).toString());
        patientPrivacyConsent.setPatient(patientRef);

        //set custodian of this consent
        String custodian = "";
        String custodianRef = "";
        if (custodianType.getValue().equals(getTranslation("sharePatient-practitioner"))) {
            custodian = practitionerComboBoxSource.getValue().getName().get(0).getNameAsSingleString();
            custodianRef = practitionerComboBoxSource.getValue().getId().replace(fhirBase, "");
        }
        else if (custodianType.getValue().equals(getTranslation("sharePatient-organization"))) {
            custodian = organizationComboBoxSource.getValue().getName();
            custodianRef = organizationComboBoxSource.getValue().getId().replace(fhirBase, "");
        }

        //this does not accept
        List<Reference> refList = new ArrayList<>();
        Reference orgRef = new Reference();
        orgRef.setReference(custodianRef);
        orgRef.setDisplay(custodian);
        refList.add(orgRef);
        patientPrivacyConsent.setOrganization(refList);

        //set dates
        Consent.provisionComponent provision = new Consent.provisionComponent();
        Period period = new Period();
        Date startDate = Date.from(provisionStartDateTime.atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(provisionEndDateTime.atZone(ZoneId.systemDefault()).toInstant());
        period.setStart(startDate);
        period.setEnd(endDate);
        provision.setPeriod(period);

        List<Coding> purposeList = new ArrayList<>();
        Coding purposeCoding = new Coding();
        purposeCoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActReason");
        purposeCoding.setCode("TREAT");
        purposeList.add(purposeCoding);

        provision.setPurpose(purposeList);

        //set default rule provision[0]
        Consent.provisionComponent ruleProvision = new Consent.provisionComponent();
        ruleProvision.setType(Consent.ConsentProvisionType.PERMIT);
        provision.addProvision(ruleProvision);

        //create provisions for classes
        if (constrainDataClass.getValue().equals(getTranslation("sharePatient-deny_access_to_following"))) {
            Consent.provisionComponent dataClassProvision = new Consent.provisionComponent();
            dataClassProvision.setType(Consent.ConsentProvisionType.DENY);
            String dataClassSystem = "http://hl7.org/fhir/resource-types";
            List<Coding> classList = new ArrayList<>();
            Set<String> itemList = dataClassComboBox.getSelectedItems();
            Iterator iter = itemList.iterator();
            while (iter.hasNext()) {
                String classCode = (String)iter.next();
                Coding classcoding = new Coding();
                classcoding.setSystem(dataClassSystem);
                classcoding.setCode(classCode);
                classList.add(classcoding);
            }
            dataClassProvision.setClass_(classList);

            //set actor
            Consent.provisionActorComponent actor = new Consent.provisionActorComponent();
            CodeableConcept roleConcept = new CodeableConcept();
            Coding rolecoding = new Coding();
            rolecoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            rolecoding.setCode("IRCP");
            roleConcept.addCoding(rolecoding);
            actor.setRole(roleConcept);

            Reference actorRef = new Reference();
            if (destinationType.getValue().equals(getTranslation("sharePatient-practitioner"))) {
                actorRef.setReference(practitionerComboBoxDestination.getValue().getId().replace(fhirBase, ""));
                actorRef.setDisplay(practitionerComboBoxDestination.getValue().getName().get(0).getNameAsSingleString());
            }
            else if (destinationType.getValue().equals(getTranslation("sharePatient-organization"))) {
                actorRef.setReference(organizationComboBoxDestination.getValue().getId().replace(fhirBase, ""));
                actorRef.setDisplay(organizationComboBoxDestination.getValue().getName());
            }

            actor.setReference(actorRef);

            List<Consent.provisionActorComponent> actorList = new ArrayList<>();
            actorList.add(actor);

            dataClassProvision.setActor(actorList);

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

            dataClassProvision.setAction(actionCodeList);

            provision.addProvision(dataClassProvision);
        }

        //create provision for sensitivity
        if (sensConstraints.getValue().equals(getTranslation("sharePatient-remove_them"))) {
            Consent.provisionComponent sensitivityProvision = new Consent.provisionComponent();
            sensitivityProvision.setType(Consent.ConsentProvisionType.DENY);

            //security label
            Coding senscoding = new Coding();
            senscoding.setCode("R");
            senscoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality");
            sensitivityProvision.addSecurityLabel(senscoding);

            //actor
            Consent.provisionActorComponent sensActor = new Consent.provisionActorComponent();
            CodeableConcept sensRoleConcept = new CodeableConcept();
            Coding sensRolecoding = new Coding();
            sensRolecoding.setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType");
            sensRolecoding.setCode("IRCP");
            sensRoleConcept.addCoding(sensRolecoding);
            sensActor.setRole(sensRoleConcept);

            Reference sensActorRef = new Reference();
            if (destinationType.getValue().equals(getTranslation("sharePatient-practitioner"))) {
                sensActorRef.setReference(practitionerComboBoxDestination.getValue().getId().replace(fhirBase, ""));
                sensActorRef.setDisplay(practitionerComboBoxDestination.getValue().getName().get(0).getNameAsSingleString());
            }
            else if (destinationType.getValue().equals(getTranslation("sharePatient-organization"))) {
                sensActorRef.setReference(organizationComboBoxDestination.getValue().getId().replace(fhirBase, ""));
                sensActorRef.setDisplay(organizationComboBoxDestination.getValue().getName());
            }

            sensActor.setReference(sensActorRef);

            List<Consent.provisionActorComponent> sensActorList = new ArrayList<>();
            sensActorList.add(sensActor);

            sensitivityProvision.setActor(sensActorList);

            //set action
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

            sensitivityProvision.setAction(sensActionCodeList);

            provision.addProvision(sensitivityProvision);
        }
        patientPrivacyConsent.setProvision(provision);

        //create attachment
        Attachment attachment = new Attachment();
        attachment.setContentType("application/pdf");
        dateRecordedProvenance = new Date();
        attachment.setCreation(dateRecordedProvenance);
        attachment.setTitle(getTranslation("sharePatient-patient_privacy"));

        ByteArrayInputStream bais = null;
        byte[] bArray = null;
        try {
            bais = new ByteArrayInputStream(consentPDFAsByteArray);
            bArray = IOUtils.toByteArray(bais);
        }
        catch (Exception ex) {
            //blah blah
        }
        String encodedString = Base64.getEncoder().encodeToString(bArray);
        attachment.setSize(encodedString.length());
        attachment.setData(encodedString.getBytes());

        patientPrivacyConsent.setSource(attachment);


        Extension extension = createPatientPrivacyQuestionnaireResponseExtension();
        patientPrivacyConsent.getExtension().add(extension);

        consentCompleted = fhirConsentClient.createConsent(patientPrivacyConsent);
    }

    private void createQuestionnaireResponse() {
        questionnaireResponse = new QuestionnaireResponse();
        questionnaireResponse.setId(consentSession.getFhirPatient().getIdElement().getIdPart() + "-" + UUID.randomUUID().toString());
        Reference refpatient = new Reference();
        refpatient.setReference("Patient/"+consentSession.getFhirPatient().getIdElement().getIdPart());
        questionnaireResponse.setAuthor(refpatient);
        questionnaireResponse.setAuthored(new Date());
        questionnaireResponse.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        questionnaireResponse.setSubject(refpatient);
        questionnaireResponse.setQuestionnaire("Questionnaire/leap-patient-privacy");

        enforcementDatesResponse();
        dataClassRestrictionsResponse();
        dataCustodianResponse();
        dataRecipientResponse();
        dataSensitivityRestrictionsResponse();
        acceptAndSignResponse();

        questionnaireResponse.setItem(responseList);
        QuestionnaireResponse completedQuestionnaireResponse = fhirQuestionnaireResponse.createQuestionnaireResponse(questionnaireResponse);
        questionnaireProvenance = "QuestionnaireResponse/"+questionnaireResponse.getId();
    }

    private void enforcementDatesResponse() {
        String defaultSDate = "";
        String defaultEDate = "";
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_1 = createItemBooleanType("1.1", getTranslation("PatientPrivacy-questionnaire_response_item_1_1"), privacy.isUseDefaultOptions());
        responseList.add(item1_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_2 = createItemBooleanType("1.2", getTranslation("PatientPrivacy-questionnaire_response_item_1_2"), privacy.isUseCustomDateOption());
        responseList.add(item1_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_1 = createItemBooleanType("1.3.1", getTranslation("PatientPrivacy-questionnaire_response_item_1_3_1"), privacy.isOneDay());
        responseList.add(item1_3_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_2 = createItemBooleanType("1.3.2", getTranslation("PatientPrivacy-questionnaire_response_item_1_3_2"), privacy.isOneYear());
        responseList.add(item1_3_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_3 = createItemBooleanType("1.3.3", getTranslation("PatientPrivacy-questionnaire_response_item_1_3_3"), privacy.isFiveYears());
        responseList.add(item1_3_3);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_4 = createItemBooleanType("1.3.4", getTranslation("PatientPrivacy-questionnaire_response_item_1_3_4"), privacy.isTenYears());
        responseList.add(item1_3_4);
        if (privacy.isUseDefaultOptions()) {
            defaultSDate = privacy.getDefaultOptionStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            defaultEDate = privacy.getDefaultOptionEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_5 = createItemStringType("1.3.5", getTranslation("PatientPrivacy-questionnaire_response_item_1_3_5"), defaultSDate);
        responseList.add(item1_3_5);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_3_6 = createItemStringType("1.3.6", getTranslation("PatientPrivacy-questionnaire_response_item_1_3_6"), defaultEDate);
        responseList.add(item1_3_6);
        String customSDate = "";
        String customEDate = "";
        if (privacy.isUseCustomDateOption()) {
            customSDate = privacy.getCustomStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            customEDate = privacy.getCustomEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_4_1 = createItemStringType("1.4.1", getTranslation("PatientPrivacy-questionnaire_response_item_1_4_1"), customSDate);
        responseList.add(item1_4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item1_4_2 = createItemStringType("1.4.2", getTranslation("PatientPrivacy-questionnaire_response_item_1_4_2"), customEDate);
        responseList.add(item1_4_2);
    }

    private void dataClassRestrictionsResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1 = createItemBooleanType("2.1", getTranslation("PatientPrivacy-questionnaire_response_item_2_1"), privacy.isLimitDataClasses());
        responseList.add(item2_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_1_1 = createItemStringArrayType("2.1.1", getTranslation("PatientPrivacy-questionnaire_response_item_2_1_1"), privacy.getDataClassList());
        responseList.add(item2_1_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item2_2 = createItemBooleanType("2.2", getTranslation("PatientPrivacy-questionnaire_response_item_2_2"), privacy.isNoDataClassConstraints());
        responseList.add(item2_2);
    }

    private void dataCustodianResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_1 = createItemBooleanType("3.1", getTranslation("PatientPrivacy-questionnaire_response_item_3_1"), privacy.isPractitionerDataSource());
        responseList.add(item3_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_1_1 = createItemStringType("3.1.1", getTranslation("PatientPrivacy-questionnaire_response_item_3_1_1"), privacy.getPractitionerName());
        responseList.add(item3_1_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2 = createItemBooleanType("3.2", getTranslation("PatientPrivacy-questionnaire_response_item_3_2"), privacy.isOrganizationDataSource());
        responseList.add(item3_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item3_2_1 = createItemStringType("3.2.1", getTranslation("PatientPrivacy-questionnaire_response_item_3_2_1"), privacy.getOrganizationName());
        responseList.add(item3_2_1);
    }

    private void dataRecipientResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1 = createItemBooleanType("4.1", getTranslation("PatientPrivacy-questionnaire_response_item_4_1"), privacy.isPractitionerDataRecipient());
        responseList.add(item4_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_1_1 = createItemStringType("4.1.1", getTranslation("PatientPrivacy-questionnaire_response_item_4_1_1"), privacy.getPractitionerRecipientName());
        responseList.add(item4_1_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2 = createItemBooleanType("4.2", getTranslation("PatientPrivacy-questionnaire_response_item_4_2"), privacy.isOrganizationDataRecipient());
        responseList.add(item4_2);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item4_2_1 = createItemStringType("4.2.1", getTranslation("PatientPrivacy-questionnaire_response_item_4_2_1"), privacy.getOrganizationRecipientName());
        responseList.add(item4_2_1);
    }

    private void dataSensitivityRestrictionsResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_1 = createItemBooleanType("5.1", getTranslation("PatientPrivacy-questionnaire_response_item_5_1"), privacy.isRemoveThem());
        responseList.add(item5_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_1_1 = createItemBooleanType("5.1.1", getTranslation("PatientPrivacy-questionnaire_response_item_5_1_1"), privacy.isRemoveAllLabeledConfidential());
        responseList.add(item5_1_1);
        QuestionnaireResponse.QuestionnaireResponseItemComponent item5_2 = createItemBooleanType("5.2", getTranslation("PatientPrivacy-questionnaire_response_item_5_2"), privacy.isNoPrivacyConcerns());
        responseList.add(item5_2);
    }

    private void acceptAndSignResponse() {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item6 = createItemBooleanType("6", getTranslation("PatientPrivacy-questionnaire_response_item_6"), privacy.isAcceptedAndSignaturedCaptured());
        responseList.add(item6);
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createItemBooleanType(String linkId, String definition, boolean bool) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        item.setLinkId(linkId);
        item.getAnswer().add((new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()).setValue(new BooleanType(bool)));
        item.setDefinition(definition);
        return item;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createItemStringType(String linkId, String definition,String string) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        item.setLinkId(linkId);
        item.getAnswer().add((new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()).setValue(new StringType(string)));
        item.setDefinition(definition);
        return item;
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent createItemStringArrayType(String linkId, String definition, Set<String> strings) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent item = new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        item.setLinkId(linkId);
        Iterator iter = strings.iterator();
        while(iter.hasNext()) {
            String string = (String)iter.next();
            item.getAnswer().add((new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()).setValue(new StringType(string)));
        }
        item.setDefinition(definition);
        return item;
    }

    private Extension createPatientPrivacyQuestionnaireResponseExtension() {
        Extension extension = new Extension();
        extension.setUrl("http://sdhealthconnect.com/leap/patient-privacy");
        extension.setValue(new StringType(consentSession.getFhirbase()+questionnaireProvenance));
        return extension;
    }

    private void createFHIRProvenance() {
        try {
            fhirProvenanceClient.createProvenance(consentProvenance, dateRecordedProvenance, questionnaireProvenance);
        }
        catch (Exception ex) {
            log.warn("Error creating provenance resource. "+ex.getMessage());
        }
    }

    private void resetQuestionNavigation() {
        questionPosition = 0;
        timeSettings.clear();
        consentDefaultPeriod.clear();
        startDateTime.clear();
        endDateTime.clear();
        constrainDataClass.clear();
        dataClassComboBox.clear();
        practitionerComboBoxSource.clear();
        practitionerComboBoxDestination.clear();
        organizationComboBoxDestination.clear();
        organizationComboBoxSource.clear();
        sensitivityOptions.clear();
        allSensitivityOptions.clear();
        custodianType.clear();
        destinationType.clear();
        sensConstraints.clear();
        //hide fields not in flow at start
        consentDefaultPeriod.setVisible(false);
        startDateTime.setVisible(false);
        endDateTime.setVisible(false);
        dataClassComboBox.setVisible(false);
        practitionerComboBoxDestination.setVisible(false);
        practitionerComboBoxSource.setVisible(false);
        organizationComboBoxSource.setVisible(false);
        organizationComboBoxDestination.setVisible(false);
        allSensitivityOptions.setVisible(false);
        sensitivityOptions.setVisible(false);
    }

    private void successNotification() {
        Span content = new Span(getTranslation("sharePatient-fhir_patient_privacy_consent_successfully_created"));

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }

    private void createErrorDialog() {
        Html errorIntro = new Html(getTranslation("sharePatient-error_intro"));
        Button errorBTN = new Button(getTranslation("sharePatient-correct_errors"));
        errorBTN.setWidthFull();
        errorBTN.addClickListener(event -> {
           questionPosition = errorList.get(0).getQuestionnaireIndex();
           errorDialog.close();
           evalNavigation();
        });

        FlexBoxLayout verticalLayout = new FlexBoxLayout();

        verticalLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        verticalLayout.setBoxSizing(BoxSizing.BORDER_BOX);
        verticalLayout.setHeight("420px");
        verticalLayout.setBackgroundColor("white");
        verticalLayout.setShadow(Shadow.S);
        verticalLayout.setBorderRadius(BorderRadius.S);
        verticalLayout.getStyle().set("margin-bottom", "10px");
        verticalLayout.getStyle().set("margin-right", "10px");
        verticalLayout.getStyle().set("margin-left", "10px");
        verticalLayout.getStyle().set("overflow", "auto");
        verticalLayout.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        Iterator iter = errorList.iterator();
        while (iter.hasNext()) {
            QuestionnaireError q = (QuestionnaireError)iter.next();
            verticalLayout.add(new Html("<p style=\"color:#259AC9\">"+q.getErrorMessage()+"</p>"));
        }

        errorDialog = new Dialog();
        errorDialog.setHeight("600px");
        errorDialog.setWidth("600px");
        errorDialog.setModal(true);
        errorDialog.setCloseOnOutsideClick(false);
        errorDialog.setCloseOnEsc(false);
        errorDialog.setResizable(true);
        errorDialog.add(createHeader(VaadinIcon.WARNING, getTranslation("sharePatient-failed_verification")),errorIntro, verticalLayout, errorBTN);
    }

    private String getA() {
        return null;
    }
}
