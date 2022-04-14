package gov.hhs.onc.leap.ui.views.clinical;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRAllergyIntolerance;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRMedicationRequest;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.layout.size.*;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.MedicationRequest;

import java.util.Collection;

public class MedicationsCard extends VerticalLayout {

    private FlexBoxLayout coreLayout;
    private Grid<MedicationRequest> grid;
    private ListDataProvider<MedicationRequest> dataProvider;


    private FHIRMedicationRequest fhirMedicationRequest;

    public MedicationsCard(FHIRMedicationRequest fhirMedicationRequest) {
        this.fhirMedicationRequest = fhirMedicationRequest;
        init();
        add(coreLayout);
    }

    private void setGrid() {
        dataProvider = DataProvider.ofCollection(getAllPatientMedications());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setItems(dataProvider);
        grid.setHeightFull();

        grid.addColumn(new ComponentRenderer<>(this::createMedications))
                .setWidth("300px");

    }

    private Component createMedications(MedicationRequest medication) {
        String medName = "Synthea Error in use of Reference";
        String medDosage = "No information available";
        try {
            medName = medication.getMedicationCodeableConcept().getCoding().get(0).getDisplay();
            String medQuantity = "Quantity: ";
            String medFrequency = "Frequency: ";
            String medPeriod = "Period: ";
            boolean asNeeded = medication.getDosageInstruction().get(0).getAsNeededBooleanType().booleanValue();
            if (!asNeeded) {
                try {
                    medQuantity = " Quantity: " + medication.getDosageInstruction().get(0).getDoseAndRate().get(0).getDoseQuantity().getValue().toString();
                    medFrequency = " Frequency: " + medication.getDosageInstruction().get(0).getTiming().getRepeat().getFrequency();
                    medPeriod = " Period: "+ medication.getDosageInstruction().get(0).getTiming().getRepeat().getPeriodUnit().getDisplay();
                    medDosage = medQuantity + medFrequency + medPeriod;
                } catch (Exception ex) {

                }
            }
            else {
                medDosage = "Dosage: As Needed";
            }
        }
        catch (Exception ex) {
            System.out.println("Synthea medication request using Reference ");
        }
        ListItem item = new ListItem(medName, medDosage);
        item.setPadding(Vertical.XS);
        return item;
    }

    private Collection<MedicationRequest> getAllPatientMedications() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        System.out.println("PATIENT "+consentSession.getFhirPatientId());
        Collection<MedicationRequest> collection = fhirMedicationRequest.getPatientMedicationRequestsById(consentSession.getFhirPatientId());
        System.out.println("SIZE OF MEDICATIONS "+collection.size());
        return collection;
    }

    private void init() {
        setGrid();
        coreLayout = new FlexBoxLayout(createHeader("Active Medications"), grid);
        coreLayout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        coreLayout.setBoxSizing(BoxSizing.CONTENT_BOX);
        coreLayout.setHeight("300px");
        coreLayout.setWidth("360px");
        coreLayout.setBackgroundColor("white");
        coreLayout.setShadow(Shadow.S);
        coreLayout.setBorderRadius(BorderRadius.S);
        coreLayout.getStyle().set("margin-bottom", "2px");
        coreLayout.getStyle().set("margin-right", "2px");
        coreLayout.getStyle().set("margin-left", "2px");
        coreLayout.setPadding(Horizontal.XS, Top.XS, Bottom.XS);
        coreLayout.setVisible(true);
    }

    private FlexBoxLayout createHeader(String title) {
        FlexBoxLayout header = new FlexBoxLayout(
                UIUtils.createH4Label(title));
        header.getStyle().set("background-color", "#94B0C5");
        header.getStyle().set("foreground-color", "#FFFFFF");
        header.setAlignItems(Alignment.CENTER);
        header.setSpacing(Right.L);
        return header;
    }
}
