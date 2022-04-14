package gov.hhs.onc.leap.ui.views.clinical;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRImmunization;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRProcedure;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.layout.size.*;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Procedure;

import java.util.Collection;

public class ImmunizationCard extends VerticalLayout {

    private FlexBoxLayout coreLayout;
    private Grid<Immunization> grid;
    private ListDataProvider<Immunization> dataProvider;


    private FHIRImmunization fhirImmunization;

    public ImmunizationCard(FHIRImmunization fhirImmunization) {
        this.fhirImmunization = fhirImmunization;
        init();
        add(coreLayout);
    }

    private void setGrid() {
        dataProvider = DataProvider.ofCollection(getAllPatientImmunizations());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();

        grid.addColumn(new ComponentRenderer<>(this::createImmunization))
                .setWidth("300px");

    }

    private Component createImmunization(Immunization immunization) {
        String immunizationName = immunization.getVaccineCode().getCoding().get(0).getDisplay();
        String locationName = immunization.getLocation().getDisplay();
        String completedDate = immunization.getOccurrenceDateTimeType().getValueAsString();
        if (locationName != null && locationName.length() > 25) locationName = locationName.substring(0, 25);
        ListItem item = new ListItem(immunizationName, locationName +" - "+completedDate);
        item.setPadding(Vertical.XS);
        return item;
    }

    private Collection<Immunization> getAllPatientImmunizations() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        System.out.println("PATIENT "+consentSession.getFhirPatientId());
        Collection<Immunization> collection = fhirImmunization.getPatientImmunizations(consentSession.getFhirPatientId());
        return collection;
    }

    private void init() {
        setGrid();
        coreLayout = new FlexBoxLayout(createHeader("Immunizations"), grid);
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
        header.setAlignItems(Alignment.STRETCH);
        header.setSpacing(Right.S);
        return header;
    }
}
