package gov.hhs.onc.leap.medrec.component;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import gov.hhs.onc.leap.medrec.model.MedicationSummary;
import gov.hhs.onc.leap.ui.components.ListItem;
import gov.hhs.onc.leap.ui.layout.size.Vertical;

public class MedicationSummaryLayout extends VerticalLayout {
    private TextField commentsField;
    private RadioButtonGroup buttonGroup;
    private MedicationSummary medicationSummary;

    public MedicationSummaryLayout(MedicationSummary medicationSummary) {
        this.setMedicationSummary(medicationSummary);
        setCommentsField(new TextField());
        getCommentsField().setLabel("Change/Comments");
        getCommentsField().addThemeVariants(TextFieldVariant.LUMO_SMALL);
        getCommentsField().setWidthFull();
        getCommentsField().setVisible(false);

        this.setWidthFull();
        ListItem listItem = new ListItem(medicationSummary.getMedication(), medicationSummary.getDosages());
        listItem.setPadding(Vertical.XS);
        setButtonGroup(new RadioButtonGroup());
        getButtonGroup().setItems("Yes - I am taking this medication as written", "No - I am no longer taking this medication",
                "Edit - I need to indicate how I am taking this medication");
        getButtonGroup().addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        getButtonGroup().addValueChangeListener(event -> {
            if (event.getValue().equals("Edit - I need to indicate how I am taking this medication")) {
                getCommentsField().setVisible(true);
            }
            else {
                getCommentsField().setValue("");
                getCommentsField().setVisible(false);
            }
        });

        this.add(listItem, getButtonGroup(), getCommentsField());
    }

    public TextField getCommentsField() {
        return commentsField;
    }

    public void setCommentsField(TextField commentsField) {
        this.commentsField = commentsField;
    }

    public RadioButtonGroup getButtonGroup() {
        return buttonGroup;
    }

    public void setButtonGroup(RadioButtonGroup buttonGroup) {
        this.buttonGroup = buttonGroup;
    }

    public MedicationSummary getMedicationSummary() {
        return medicationSummary;
    }

    public void setMedicationSummary(MedicationSummary medicationSummary) {
        this.medicationSummary = medicationSummary;
    }
}
