package gov.hhs.onc.leap.ui.views.clinical;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRCondition;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRProcedure;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.layout.size.*;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Procedure;

import java.util.Collection;

public class ProcedureCard extends VerticalLayout {

    private FlexBoxLayout coreLayout;
    private Grid<Procedure> grid;
    private ListDataProvider<Procedure> dataProvider;


    private FHIRProcedure fhirProcedure;

    public ProcedureCard(FHIRProcedure fhirProcedure) {
        this.fhirProcedure = fhirProcedure;
        init();
        add(coreLayout);
    }

    private void setGrid() {
        dataProvider = DataProvider.ofCollection(getAllPatientProcedures());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();

        grid.addColumn(new ComponentRenderer<>(this::createProcedure))
                .setWidth("300px");

    }

    private Component createProcedure(Procedure procedure) {
        String procedureName = procedure.getCode().getCoding().get(0).getDisplay();
        String locationName = procedure.getLocation().getDisplay();
        String completedDate = procedure.getPerformedPeriod().getEnd().toString();
        if (locationName != null && locationName.length() > 25) locationName = locationName.substring(0, 25);
        ListItem item = new ListItem(procedureName, locationName+" - "+completedDate);
        item.setPadding(Vertical.XS);
        return item;
    }

    private Collection<Procedure> getAllPatientProcedures() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        System.out.println("PATIENT "+consentSession.getFhirPatientId());
        Collection<Procedure> collection = fhirProcedure.getPatientProcedures(consentSession.getFhirPatientId());
        return collection;
    }

    private void init() {
        setGrid();
        coreLayout = new FlexBoxLayout(createHeader("Procedures"), grid);
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
