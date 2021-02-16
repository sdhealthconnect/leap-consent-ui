package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import de.f0rce.signaturepad.SignaturePad;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIROrganization;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRPractitioner;
import gov.hhs.onc.leap.ui.MainLayout;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.Component;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.navigation.BasicDivider;
import gov.hhs.onc.leap.ui.layout.size.*;
import gov.hhs.onc.leap.ui.util.IconSize;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Practitioner;


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
    private FHIROrganization fhirOrganization = new FHIROrganization();
    private FHIRPractitioner fhirPractitioner = new FHIRPractitioner();
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
    private byte[] base64Signature;

    private SignaturePad signature;


    public SharePatientDataView() {
        setId("sharePatientDataView");
        setViewContent(createViewContent());
        setViewFooter(getFooter());
    }

    private Component createViewContent() {
        Html intro = new Html("<p>The following allows you the <b>Patient</b> to create rules to control " +
                "what, when, and to whom your <b>Personal Healthcare Information</b> can be exchanged with.  " +
                "That exchange may be between your Primary Physician, Regional Hospital, Health Information Exchange, and others. " +
                "You may choose not to share information that could be sensitive in nature, or choose not to constrain the exchange at all. "+
                "If you have privacy concerns use the <b>Analyze My Data</b> option to determine if sensitive information exists in your "+
                "clinical record.");
        RadioButtonGroup<String> timeSettings = new RadioButtonGroup<>();
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
        consentDefaultPeriod.setLabel("Default Date Options, beginning Today for;");
        consentDefaultPeriod.setItems("24 Hours", "1 year", "5 years", "10 years");
        consentDefaultPeriod.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        consentDefaultPeriod.setVisible(false);

        startDateTime = new DateTimePicker();
        startDateTime.setLabel("Begin Enforceing this Consent On;");
        startDateTime.setDatePlaceholder("Date");
        startDateTime.setTimePlaceholder("Time");
        startDateTime.setVisible(false);

        endDateTime = new DateTimePicker();
        endDateTime.setLabel("This Consent will nolonger be valid on;");
        endDateTime.setDatePlaceholder("Date");
        endDateTime.setTimePlaceholder("Time");
        endDateTime.setVisible(false);

        RadioButtonGroup<String> constrainDataClass = new RadioButtonGroup<>();
        constrainDataClass.setLabel("Control what types of clinical information are exchanged");
        constrainDataClass.setItems("Limit information to following;", "Allow all types of data to be exchanged");
        constrainDataClass.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        constrainDataClass.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //do nothing
            }
            else {
                if (event.getValue().equals("Limit information to following;")) {
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
        dataClassComboBox.setHeight("100px");
        dataClassComboBox.setVisible(false);


        RadioButtonGroup<String> custodianType = new RadioButtonGroup<>();
        custodianType.setLabel("The source of information being exchanged");
        custodianType.setItems("Practitioner", "Organization");
        custodianType.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        custodianType.addValueChangeListener(event -> {
           if (event.getValue() == null) {
               //do nothing
           }
           else {
               if (event.getValue().equals("Practitioner")) {
                   practitionerComboBoxSource.setVisible(true);
                   organizationComboBoxSource.setVisible(false);
               }
               else if (event.getValue().equals("Organization")) {
                   practitionerComboBoxSource.setVisible(false);
                   organizationComboBoxSource.setVisible(true);
               }
               else {
                   practitionerComboBoxSource.setVisible(false);
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


        RadioButtonGroup<String> destinationType = new RadioButtonGroup<>();
        destinationType.setLabel("The Person or Organization, the destination, requesting your information");
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

        RadioButtonGroup<String> sensConstraints = new RadioButtonGroup<>();
        sensConstraints.setLabel("If portions of my clinical record are privacy sensitive I would like to;");
        sensConstraints.setItems("Remove them", "I do not have a privacy concerns");
        sensConstraints.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        sensConstraints.addValueChangeListener(event -> {
            if (event.getValue() == null) {
                //nothing
            }
            else {
                if (event.getValue().equals("Remove them")) {
                    allSensitivityOptions.setVisible(true);
                    sensitivityOptions.setVisible(true);
                }
                else if(event.getValue().equals("I do not have privacy concerns")){
                    allSensitivityOptions.setVisible(false);
                    sensitivityOptions.setVisible(false);
                }
                else {
                    allSensitivityOptions.setVisible(false);
                    sensitivityOptions.setVisible(false);
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
        sensitivityOptions.setVisible(false);

        Html eSignLabel = new Html("<p>This last step will capture your signature and create a <b>human readible pdf</b> of this consent.</p>");
        Button eSignButton = new Button("eSign Consent and Submit");
        eSignButton.addClickListener(event -> {
            dialog = createSignatureDialog();
            dialog.open();
        });



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

        sourceRequirements = new FlexBoxLayout(createHeader(VaadinIcon.DOCTOR, "Data Source - Custodian"),custodianType, new BasicDivider(), practitionerComboBoxSource, organizationComboBoxSource);
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

        privacyRequirements = new FlexBoxLayout(createHeader(VaadinIcon.GLASSES, "Privacy Concerns"),sensConstraints, new BasicDivider(), allSensitivityOptions, sensitivityOptions);
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


        FlexBoxLayout content = new FlexBoxLayout(intro, dateRequirements, dataClassRequirements, sourceRequirements,
                destinationRequirements, privacyRequirements, signatureRequirements);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();


        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;



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
            forwardButton.setEnabled(true);
            dialog.close();
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

}
