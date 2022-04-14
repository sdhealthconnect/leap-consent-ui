package gov.hhs.onc.leap.ui.views.clinical;

import com.github.appreciated.css.grid.GridLayoutComponent;
import com.github.appreciated.css.grid.sizes.Flex;
import com.github.appreciated.css.grid.sizes.Length;
import com.github.appreciated.css.grid.sizes.MinMax;
import com.github.appreciated.css.grid.sizes.Repeat;
import com.github.appreciated.layout.FlexibleGridLayout;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.backend.fhir.client.utils.*;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.views.ViewFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@PageTitle("My Health Record")
@Route(value = "myhealthrecord", layout = MainLayout.class)
public class MyHealthRecord extends ViewFrame {
    private static final Logger log = LoggerFactory.getLogger(MyHealthRecord.class);
    private FlexibleGridLayout gridLayout;

    @Autowired
    private FHIRCondition fhirCondition;

    @Autowired
    private FHIRAllergyIntolerance fhirAllergyIntolerance;

    @Autowired
    private FHIRMedicationRequest fhirMedicationRequest;

    @Autowired
    private FHIRProcedure fhirProcedure;

    @Autowired
    private FHIRImmunization fhirImmunization;

    @Autowired
    private FHIRObservation fhirObservation;

    @Autowired FHIRServiceRequest fhirServiceRequest;

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        initComponents();
        setViewContent(gridLayout);
    }

    private void initComponents() {
            gridLayout = new FlexibleGridLayout()
                    .withColumns(Repeat.RepeatMode.AUTO_FILL, new MinMax(new Length("360px"), new Flex(1)))
                    .withAutoRows(new Length("360px"))
                    .withItems(
                            new ConditionCard(fhirCondition),
                            new AllergyIntoleranceCard(fhirAllergyIntolerance),
                            new MedicationsCard(fhirMedicationRequest),
                            new ImmunizationCard(fhirImmunization),
                            new ProcedureCard(fhirProcedure),
                            new VitalsCard(fhirObservation),
                            new LaboratoryCard(fhirObservation),
                            new ReferralCard(fhirServiceRequest),
                            new ClinicalLayoutControl()
                    )
                    .withPadding(false)
                    .withSpacing(true)
                    .withAutoFlow(GridLayoutComponent.AutoFlow.ROW_DENSE)
                    .withOverflow(GridLayoutComponent.Overflow.AUTO);
            gridLayout.setSizeFull();
    }

}
