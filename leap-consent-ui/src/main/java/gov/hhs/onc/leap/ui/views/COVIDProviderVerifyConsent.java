package gov.hhs.onc.leap.ui.views;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import gov.hhs.onc.leap.ui.MainLayout;
import gov.hhs.onc.leap.ui.components.FlexBoxLayout;
import gov.hhs.onc.leap.ui.layout.size.Horizontal;
import gov.hhs.onc.leap.ui.layout.size.Uniform;
import gov.hhs.onc.leap.ui.util.UIUtils;

@PageTitle("Verify Consent")
@Route(value = "verifyconsentview", layout = MainLayout.class)
public class COVIDProviderVerifyConsent extends ViewFrame {

    public COVIDProviderVerifyConsent() {
        setId("allergyintoleranceview");
        setViewContent(createViewContent());
    }

    private Component createViewContent() {
        TextField lastname = new TextField("Lastname");
        TextField firstname = new TextField("Firstname");
        TextField mi = new TextField("MI");
        mi.setWidth("60px");
        TextField birthdate = new TextField("Birthdate");
        TextField city = new TextField("City");
        Button search = new Button();
        search.setIcon(UIUtils.createTertiaryIcon(VaadinIcon.SEARCH));

        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.setWidth("840px");
        layout.setMargin(true);
        layout.setPadding(false);

        layout.add(lastname, firstname, mi, birthdate, city, search);

        Label advancedSearch = new Label("Advanced Search");
        advancedSearch.getStyle().set("font-size", "12px");
        advancedSearch.getStyle().set("color", "#00ccff");
        advancedSearch.getStyle().set("text-align", "right");


        Label availabledocuments = new Label("Consents Granted to Provider");

        Grid grid = new Grid();
        grid.setWidth("100%");
        grid.addColumn(new ComponentRenderer<>(this::createDate))
                .setHeader("Date")
                .setWidth("80px");
        grid.addColumn(new ComponentRenderer<>(this::createPolicyType))
                .setHeader("Policy Type");

        FlexBoxLayout content = new FlexBoxLayout(layout, advancedSearch, availabledocuments, grid);
        content.setAlignItems(FlexComponent.Alignment.AUTO);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.setMargin(Horizontal.AUTO);
        content.setMaxWidth("840px");
        content.setPadding(Uniform.RESPONSIVE_L);
        return content;
    }

    private Component createDate() {
        return new Label();
    }
    private Component createPolicyType() {
        return new Label();
    }

}
