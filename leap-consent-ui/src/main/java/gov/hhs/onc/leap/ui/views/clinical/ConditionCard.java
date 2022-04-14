package gov.hhs.onc.leap.ui.views.clinical;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRCondition;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.layout.size.*;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.BorderRadius;
import gov.hhs.onc.leap.ui.util.css.BoxSizing;
import gov.hhs.onc.leap.ui.util.css.Shadow;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ConditionCard extends VerticalLayout {

    private FlexBoxLayout coreLayout;
    private Grid<Condition> grid;
    private Button lockData;
    private Button unlockData;
    private PasswordField passwordField;
    private ListDataProvider<Condition> dataProvider;
    private boolean dataSensitive = false;


    private FHIRCondition fhirCondition;

    public ConditionCard(FHIRCondition fhirCondition) {
        this.fhirCondition = fhirCondition;
        init();
        add(coreLayout);
    }

    private void setGrid() {
        dataProvider = DataProvider.ofCollection(getAllPatientConditions());

        grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setDataProvider(dataProvider);
        grid.setHeightFull();

        grid.addColumn(new ComponentRenderer<>(this::createCondition))
                .setWidth("300px");

    }

    private Component createCondition(Condition condition) {
        ListItem item = new ListItem(condition.getCode().getCoding().get(0).getDisplay().toString(), condition.getRecordedDate().toString());
        item.setPadding(Vertical.XS);
        return item;
    }

    private Collection<Condition> getAllPatientConditions() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        Collection<Condition> collection = fhirCondition.getPatientConditions(consentSession.getFhirPatientId());
        //determine if sensitive data exists
        Iterator iter = collection.iterator();
        while (iter.hasNext()) {
            Condition cond = (Condition) iter.next();
            try {
                //test for existence and get label
                List<Coding> codeList = cond.getMeta().getSecurity();
                if (codeList != null && codeList.size() > 0) {
                    Iterator codeIter = codeList.iterator();
                    while (codeIter.hasNext()) {
                        Coding coding = (Coding)codeIter.next();
                        String system = coding.getSystem();
                        String code = coding.getCode();
                        if (system != null && system.equals("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")) {
                            if (code.equals("R")) {
                                dataSensitive = true;
                                break;
                            }
                        }
                    }
                }
            }
            catch (Exception ex) {
                //nothing to do here
            }
            if (dataSensitive) break;
        }
        return collection;
    }

    private void init() {
        setGrid();
        lockData = new Button("Lock Sensitive Data");
        lockData.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.UNLOCK));
        lockData.setWidthFull();
        lockData.addClickListener(buttonClickEvent -> {
            grid.setVisible(false);
            lockData.setVisible(false);
            unlockData.setVisible(true);
            passwordField.setVisible(false);
        });
        unlockData = new Button("Unlock Sensitive Data");
        unlockData.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.LOCK));
        unlockData.setWidthFull();
        unlockData.addClickListener(buttonClickEvent -> {
            passwordField.setVisible(true);
        });
        passwordField = new PasswordField();
        passwordField.setLabel("Enter Password:");
        passwordField.setWidthFull();
        passwordField.addValueChangeListener(valueChangeEvent -> {
            String pass = passwordField.getValue();
            //for testing only
            if (pass.equals("password")) {
                grid.setVisible(true);
                lockData.setVisible(true);
                unlockData.setVisible(false);
                passwordField.setVisible(false);
                passwordField.setValue("");
            }
        });
        if (dataSensitive) {
            grid.setVisible(false);
            lockData.setVisible(false);
            unlockData.setVisible(true);
            passwordField.setVisible(false);
        }
        else {
            grid.setVisible(true);
            lockData.setVisible(false);
            unlockData.setVisible(false);
            passwordField.setVisible(false);
        }

        coreLayout = new FlexBoxLayout(createHeader("Conditions"), grid, lockData, unlockData, passwordField);
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
