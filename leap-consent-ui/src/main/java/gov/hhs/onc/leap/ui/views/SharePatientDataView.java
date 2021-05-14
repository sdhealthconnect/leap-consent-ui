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
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRConsent;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIROrganization;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRPractitioner;
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

    @Autowired
    private FHIROrganization fhirOrganization;
    @Autowired
    private FHIRPractitioner fhirPractitioner;
    @Autowired
    private FHIRConsent fhirConsentClient;
    @Autowired
    private PDFSigningService pdfSigningService;
    private String fhirBase;



    @PostConstruct
    public void setup() {
        setId("sharepatientdataView");
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }


    private Component createViewContent() {
        Html intro = new Html("<p>The following allows you, the <b>Patient</b>, to create rules to control " +
                "what, when, and to whom your <b>Personal Healthcare Information</b> can be exchanged with.  " +
                "That exchange may be between your Primary Physician, Regional Hospital, Health Information Exchange, and others. " +
                "You may choose not to share information that could be sensitive in nature, or choose not to constrain the exchange at all. "+
                "If you have privacy concerns use the <b>Analyze My Data</b> option to determine if sensitive information exists in your "+
                "clinical record.");

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
        timeSettings.setLabel("Set the dates this consent will be in force.");
        timeSettings.setItems("Use Default Option", "Custom Date Option");
        timeSettings.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        timeSettings.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            } else {
                if (event.getValue().equals("Use Default Option")) {
                    consentDefaultPeriod.setVisible(true);
                    startDateTime.setVisible(false);
                    endDateTime.setVisible(false);
                }
                else if (event.getValue().equals("Custom Date Option")) {
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
        consentDefaultPeriod.setLabel("Default date options, beginning today for:");
        consentDefaultPeriod.setItems("24 hours", "1 year", "5 years", "10 years");
        consentDefaultPeriod.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        consentDefaultPeriod.setVisible(false);

        startDateTime = new DateTimePicker();
        startDateTime.setLabel("Begin enforcing this consent on:");
        startDateTime.setDatePlaceholder("Date");
        startDateTime.setTimePlaceholder("Time");
        startDateTime.setVisible(false);

        endDateTime = new DateTimePicker();
        endDateTime.setLabel("This consent will no longer be valid on:");
        endDateTime.setDatePlaceholder("Date");
        endDateTime.setTimePlaceholder("Time");
        endDateTime.setVisible(false);

        dateRequirements = new FlexBoxLayout(createHeader(VaadinIcon.CALENDAR, "Date Requirements"),timeSettings, new BasicDivider(), consentDefaultPeriod, startDateTime, endDateTime);
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
        constrainDataClass.setLabel("Control what types of clinical information are exchanged.");
        constrainDataClass.setItems("Deny access to following:", "Allow all types of data to be exchanged.");
        constrainDataClass.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        constrainDataClass.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals("Deny access to following:")) {
                    dataClassComboBox.setVisible(true);
                }
                else {
                    dataClassComboBox.setVisible(false);
                }
            }
        });

        // todo dataClassComboBox make this a multiselect combo
        dataClassComboBox = new MultiSelectListBox<>();

        dataClassComboBox.setItems("AdverseEvent","AllergyIntolerance", "Appointment", "BodyStructure",
                "CarePlan", "Condition", "Coverage", "DiagnosticReport", "Encounter", "EpisodeOfCare",
                "FamilyMemberHistory", "Goal", "ImagingStudy", "Immunization", "InsurancePlan", "MeasureReport",
                "MedicationStatement","Observation", "Patient", "RelatedPerson", "ResearchSubject", "RiskAssessment",
                "ServiceRequest", "Specimen");
        WebBrowser browser = VaadinSession.getCurrent().getBrowser();
        if (browser.isAndroid() || browser.isIPhone() || browser.isWindowsPhone()) {
            dataClassComboBox.setHeight("150px");
        }
        else {
            dataClassComboBox.setHeight("400px");
        }
        dataClassComboBox.setVisible(false);

        dataClassRequirements = new FlexBoxLayout(createHeader(VaadinIcon.RECORDS, "Data Class Requirements"), constrainDataClass, new BasicDivider(), dataClassComboBox);
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
        Html practitionerLimitation = new Html("<p><b>Note:</b> Selection of practitioner has been disabled due to limitations of FHIR R4 Consent.  Please select an organization instead.</p>");
        custodianType = new RadioButtonGroup<>();
        custodianType.setLabel("The source of information being exchanged.");
        custodianType.setItems("Practitioner", "Organization");
        custodianType.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        custodianType.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals("Practitioner")) {
                    practitionerComboBoxSource.setVisible(true);
                    practitionerComboBoxSource.setEnabled(false);
                    practitionerLimitation.setVisible(true);
                    organizationComboBoxSource.setVisible(false);
                }
                else if (event.getValue().equals("Organization")) {
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
        practitionerComboBoxSource.setLabel("Practitioner - Custodian");
        practitionerComboBoxSource.setItemLabelGenerator(practitioner -> practitioner.getName().get(0).getNameAsSingleString());
        practitionerComboBoxSource.setItems(practitionerListDataProvider);
        practitionerComboBoxSource.setVisible(false);

        organizationComboBoxSource = new ComboBox<>();
        organizationComboBoxSource.setLabel("Organization - Custodian");
        organizationComboBoxSource.setItemLabelGenerator(organization -> organization.getName());
        organizationComboBoxSource.setItems(organizationListDataProvider);
        organizationComboBoxSource.setVisible(false);

        practitionerLimitation.setVisible(false);

        sourceRequirements = new FlexBoxLayout(createHeader(VaadinIcon.DOCTOR, "Data Source - Custodian"),custodianType, new BasicDivider(), practitionerComboBoxSource, organizationComboBoxSource, practitionerLimitation);
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
        destinationType.setLabel("The Person, or Organization, requesting your information.");
        destinationType.setItems("Practitioner", "Organization");
        destinationType.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        destinationType.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals("Practitioner")) {
                    practitionerComboBoxDestination.setVisible(true);
                    organizationComboBoxDestination.setVisible(false);
                }
                else if (event.getValue().equals("Organization")) {
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
        practitionerComboBoxDestination.setLabel("Practitioner - Recipient");
        practitionerComboBoxDestination.setItemLabelGenerator(practitioner -> practitioner.getName().get(0).getNameAsSingleString());
        practitionerComboBoxDestination.setItems(practitionerListDataProvider);
        practitionerComboBoxDestination.setVisible(false);

        organizationComboBoxDestination = new ComboBox<>();
        organizationComboBoxDestination.setLabel("Organization - Recipient");
        organizationComboBoxDestination.setItemLabelGenerator(organization -> organization.getName());
        organizationComboBoxDestination.setItems(organizationListDataProvider);
        organizationComboBoxDestination.setVisible(false);

        destinationRequirements = new FlexBoxLayout(createHeader(VaadinIcon.HOSPITAL, "Destination - Recipient"),destinationType, new BasicDivider(), practitionerComboBoxDestination, organizationComboBoxDestination);
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
        Html enforceLimitation = new Html("<p><b>Note:</b> Current enforcement is limited to security labels where confidentialy is \"R\" for Restricted or above.  Defaulting selection to \"All\".</p>");
        sensConstraints = new RadioButtonGroup<>();
        sensConstraints.setLabel("If portions of my clinical record are privacy sensitive, I would like to:");
        sensConstraints.setItems("Remove them", "I do not have privacy concerns");
        sensConstraints.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        sensConstraints.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //nothing
            }
            else {
                if (event.getValue().equals("Remove them")) {
                    allSensitivityOptions.setVisible(true);
                    sensitivityOptions.setVisible(true);
                    enforceLimitation.setVisible(true);
                }
                else if(event.getValue().equals("I do not have privacy concerns")){
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
        allSensitivityOptions.setLabel("Select \"All\" - this will remove all information flagged as confidential");
        Set<String> items = new LinkedHashSet<>(
                Arrays.asList("ETH-Substance Abuse", "HIV-HIV/AIDS", "PSY-Psychiatry Disorder",
                        "SICKLE-Sickle Cell Anemia", "STD-Sexually Transmitted Disease"));
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

        privacyRequirements = new FlexBoxLayout(createHeader(VaadinIcon.GLASSES, "Privacy Concerns"),sensConstraints, new BasicDivider(), allSensitivityOptions, sensitivityOptions, enforceLimitation);
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
        Html eSignLabel = new Html("<p>This last step will capture your signature and create a <b>human readable pdf</b> of this consent.</p>");
        Button eSignButton = new Button("eSign Consent and Submit");
        eSignButton.addClickListener(event -> {
            dialog = createSignatureDialog();
            dialog.open();
        });

        signatureRequirements = new FlexBoxLayout(createHeader(VaadinIcon.PENCIL, "Signature"), eSignLabel, eSignButton, new BasicDivider());
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

        Button saveSig = new Button("Done");
        saveSig.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CHECK));
        saveSig.addClickListener(event -> {
            base64Signature = signature.getImageBase64();
            //todo create fhir consent resource and pdf for review in flow and final submittal of consent
            dialog.close();
            getHumanReadable();
            if (errorList.size() == 0) {
                docDialog.open();
            }
            else {
                createErrorDialog();
                errorDialog.open();
            }
        });
        Button cancelSign = new Button("Cancel");
        cancelSign.setIcon(UIUtils.createIcon(IconSize.M, TextColor.TERTIARY, VaadinIcon.CLOSE));
        cancelSign.addClickListener(event -> {
            dialog.close();
        });
        Html signHere = new Html("<p><b>Sign Here</b></p>");
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
            errorList = new ArrayList<>();
            //get consent period
            String sDate = "";
            String eDate = "";
            LocalDateTime defDate = LocalDateTime.now();
            String dataDomainConstraintlist = "Deny access to following: ";
            String custodian = "";
            String recipient = "";
            String sensitivities = "Remove following sensitivity types if found in my record; ";
            try {
                if (timeSettings.getValue().equals("Use Default Option")) {
                    if (consentDefaultPeriod.equals("24 Hours")) {
                        defDate = LocalDateTime.now().plusDays(1);
                    } else if (consentDefaultPeriod.getValue().equals("1 year")) {
                        defDate = LocalDateTime.now().plusYears(1);
                    } else if (consentDefaultPeriod.getValue().equals("5 years")) {
                        defDate = LocalDateTime.now().plusYears(5);
                    } else if (consentDefaultPeriod.getValue().equals("10 years")) {
                        defDate = LocalDateTime.now().plusYears(10);
                    } else {
                        errorList.add(new QuestionnaireError("No default date selected", 0));
                        defDate = null;
                    }
                    sDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    eDate = defDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    //for use later
                    provisionStartDateTime = LocalDateTime.now();
                    provisionEndDateTime = defDate;
                } else if (timeSettings.getValue().equals("Custom Date Option")) {
                    sDate = startDateTime.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    eDate = endDateTime.getValue().format(DateTimeFormatter.ISO_LOCAL_DATE);
                    //for use later
                    provisionStartDateTime = startDateTime.getValue();
                    provisionEndDateTime = endDateTime.getValue();
                    if (provisionStartDateTime == null || provisionEndDateTime == null) {
                        errorList.add(new QuestionnaireError("Custom date can not be blank.", 0));
                    }
                    if (provisionEndDateTime.isBefore(provisionStartDateTime)) {
                        errorList.add(new QuestionnaireError("Custom end date can not be before start date.", 0));
                    }
                    if (provisionEndDateTime.isBefore(defDate)) {
                        errorList.add(new QuestionnaireError("Custom end date can not be before today.", 0));
                    }
                } else {
                    errorList.add(new QuestionnaireError("No date range selection made.", 0));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError("No date range selection made.", 0));
            }
            //get domain constraints
            try {
                if (constrainDataClass.getValue().equals("Deny access to following:")) {
                    Set<String> classList = dataClassComboBox.getSelectedItems();
                    Iterator iterClass = classList.iterator();
                    while (iterClass.hasNext()) {
                        String s = (String) iterClass.next();
                        dataDomainConstraintlist = dataDomainConstraintlist + s + " ";
                    }
                    if (classList.isEmpty()) {
                        errorList.add(new QuestionnaireError("No data class exceptions found in list.", 1));
                    }
                } else {
                    //this is the default when none selected
                    dataDomainConstraintlist = "Allow all types of data to be exchanged";
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError("No data class selection made.", 1));
            }
            //set custodian
            try {
                if (custodianType.getValue().equals("Practitioner")) {
                    custodian = practitionerComboBoxSource.getValue().getName().get(0).getNameAsSingleString();
                } else if (custodianType.getValue().equals("Organization")) {
                    custodian = organizationComboBoxSource.getValue().getName();
                } else {
                    errorList.add(new QuestionnaireError("No custodian selection made.", 2));
                }
                if (custodian.equals("") || custodian.isEmpty()) {
                    errorList.add(new QuestionnaireError("No custodian selection made.", 2));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError("No custodian selection made.", 2));
            }
            //set recipient
            try {

                if (destinationType.getValue().equals("Practitioner")) {
                    recipient = practitionerComboBoxDestination.getValue().getName().get(0).getNameAsSingleString();
                } else if (destinationType.getValue().equals("Organization")) {
                    recipient = organizationComboBoxDestination.getValue().getName();
                } else {
                    errorList.add(new QuestionnaireError("No recipient/destination selection made.", 3));
                }
                if (recipient.equals("") || recipient.isEmpty()) {
                    errorList.add(new QuestionnaireError("No recipient/destination selection made.", 3));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError("No recipient/destination selection made.", 3));
            }

            //set sensitivity constraints
            try {

                if (sensConstraints.getValue().equals("Remove them")) {
                    Set<String> sensSet = sensitivityOptions.getSelectedItems();
                    Iterator sensIter = sensSet.iterator();
                    while (sensIter.hasNext()) {
                        String s = (String) sensIter.next();
                        sensitivities = sensitivities + s + " ";
                    }
                } else if (sensConstraints.getValue().equals("I do not have privacy concerns")) {
                    sensitivities = "I do not have privacy concerns";
                } else {
                    //default if none selected
                    errorList.add(new QuestionnaireError("No privacy concern selected.", 4));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError("No privacy concern selection made.", 4));
            }
            try {
                if (base64Signature.length == 0) {
                    errorList.add(new QuestionnaireError("User signature can not be blank.", 5));
                }
            }
            catch (Exception ex) {
                errorList.add(new QuestionnaireError("User signature can not be blank.", 5));
            }
            if (errorList.size() > 0) {
                return null;
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


        Button closeButton = new Button("Cancel", e -> docDialog.close());
        closeButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.EXIT));

        Button acceptButton = new Button("Accept and Submit");
        acceptButton.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.FILE_PROCESS));
        acceptButton.addClickListener(event -> {
            docDialog.close();
            createFHIRConsent();
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
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        fhirBase = consentSession.getFhirbase();
        Patient patient = consentSession.getFhirPatient();

        Consent patientPrivacyConsent = new Consent();
        patientPrivacyConsent.setId(UUID.randomUUID().toString());
        patientPrivacyConsent.setStatus(Consent.ConsentState.ACTIVE);

        patientPrivacyConsent.setDateTime(new Date());

        //set consent scope
        CodeableConcept cConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://terminology.hl7.org/CodeSystem/consentscope");
        coding.setCode("patient-privacy");
        cConcept.addCoding(coding);
        patientPrivacyConsent.setScope(cConcept);

        List<CodeableConcept> cList = new ArrayList<>();
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
        if (custodianType.getValue().equals("Practitioner")) {
            custodian = practitionerComboBoxSource.getValue().getName().get(0).getNameAsSingleString();
            custodianRef = practitionerComboBoxSource.getValue().getId().replace(fhirBase, "");
        }
        else if (custodianType.getValue().equals("Organization")) {
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

        //set rule
        CodeableConcept policyCode = new CodeableConcept();
        Coding codes = new Coding();
        codes.setCode("OPTIN");
        codes.setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode");
        policyCode.addCoding(codes);
        patientPrivacyConsent.setPolicyRule(policyCode);

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

        //create provisions for classes
        if (constrainDataClass.getValue().equals("Deny access to following;")) {
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
            if (destinationType.getValue().equals("Practitioner")) {
                actorRef.setReference(practitionerComboBoxDestination.getValue().getId().replace(fhirBase, ""));
                actorRef.setDisplay(practitionerComboBoxDestination.getValue().getName().get(0).getNameAsSingleString());
            }
            else if (destinationType.getValue().equals("Organization")) {
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
        if (sensConstraints.getValue().equals("Remove them")) {
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
            if (destinationType.getValue().equals("Practitioner")) {
                sensActorRef.setReference(practitionerComboBoxDestination.getValue().getId().replace(fhirBase, ""));
                sensActorRef.setDisplay(practitionerComboBoxDestination.getValue().getName().get(0).getNameAsSingleString());
            }
            else if (destinationType.getValue().equals("Organization")) {
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
        attachment.setCreation(new Date());
        attachment.setTitle("patient-privacy");

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

        fhirConsentClient.createConsent(patientPrivacyConsent);
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
        Span content = new Span("FHIR patient-privacy consent successfully created!");

        Notification notification = new Notification(content);
        notification.setDuration(3000);

        notification.setPosition(Notification.Position.MIDDLE);

        notification.open();
    }

    private void createErrorDialog() {
        Html errorIntro = new Html("<p><b>The following errors were identified. You will need to correct them before saving this consent document.</b></p>");
        Button errorBTN = new Button("Correct Errors");
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
        errorDialog.add(createHeader(VaadinIcon.WARNING, "Failed Verification"),errorIntro, verticalLayout, errorBTN);
    }


}
