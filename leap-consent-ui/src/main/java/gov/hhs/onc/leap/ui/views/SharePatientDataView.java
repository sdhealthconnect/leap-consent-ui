package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.backend.ConsentDocument;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIROrganization;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRPractitioner;
import gov.hhs.onc.leap.ui.MainLayout;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.Component;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Top;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import org.graalvm.compiler.core.common.type.ArithmeticOpTable;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.OrganizationAffiliation;
import org.hl7.fhir.r4.model.Practitioner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@PageTitle("Share My Data")
@Route(value = "sharepatientdataview", layout = MainLayout.class)
public class SharePatientDataView extends ViewFrame {
    private RadioButtonGroup<String> consentDefaultPeriod;
    private DateTimePicker startDateTime;
    private DateTimePicker endDateTime;
    private ComboBox<Practitioner> practitionerComboBoxSource;
    private ComboBox<Practitioner> practitionerComboBoxDestination;
    private ComboBox<Organization> organizationComboBoxSource;
    private ComboBox<Organization> organizationComboBoxDestination;
    private FHIROrganization fhirOrganization = new FHIROrganization();
    private FHIRPractitioner fhirPractitioner = new FHIRPractitioner();
    private ListDataProvider<Practitioner> practitionerListDataProvider;
    private ListDataProvider<Organization> organizationListDataProvider;

    public SharePatientDataView() {
        setId("sharePatientDataView");
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        Html intro = new Html("<p>The following allows you the <b>Patient</b> to create rules to control " +
                "what, when, and to whom your <b>Personal Healthcare Information</b> can be exchanged with.  " +
                "That exchange may be between your Primary Physician, Regional Hospital, Health Information Exchange, and others. " +
                "You may choose not to share information that could be sensitive in nature, or choose not to constrain the exchange at all.  It's your choice. " +
                "You the <b>Patient</b> have the control here.");

        RadioButtonGroup<String> timeSettings = new RadioButtonGroup<>();
        timeSettings.setLabel("Choose which date ranges works best for you");
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






        FlexBoxLayout content = new FlexBoxLayout(intro, timeSettings, consentDefaultPeriod, startDateTime, endDateTime, custodianType, practitionerComboBoxSource, organizationComboBoxSource);
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


}
