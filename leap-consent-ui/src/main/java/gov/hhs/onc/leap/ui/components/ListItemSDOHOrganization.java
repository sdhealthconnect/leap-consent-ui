package gov.hhs.onc.leap.ui.components;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import gov.hhs.onc.leap.ui.layout.size.Right;
import gov.hhs.onc.leap.ui.layout.size.Wide;
import gov.hhs.onc.leap.ui.util.FontSize;
import gov.hhs.onc.leap.ui.util.TextColor;
import gov.hhs.onc.leap.ui.util.UIUtils;

@CssImport("./styles/components/list-item-sdoh.css")
public class ListItemSDOHOrganization extends FlexBoxLayout {
    private final String CLASS_NAME = "list-item-sdoh";

    private FlexBoxLayout content;

    private Label program;
    private Label organization;
    private Label address;
    private Label phoneAndOpHours;
    private Label website;

    public ListItemSDOHOrganization(String program, String organization, String address, String phoneAndOpHours, String website) {
        addClassName(CLASS_NAME);

        setAlignItems(FlexComponent.Alignment.CENTER);
        setPadding(Wide.RESPONSIVE_L);
        setSpacing(Right.L);

        this.program = new Label(program);
        this.organization = UIUtils.createLabel(FontSize.S, TextColor.SECONDARY,
                organization);
        this.address = UIUtils.createLabel(FontSize.XS, TextColor.SECONDARY, address);
        this.phoneAndOpHours = UIUtils.createLabel(FontSize.XS, TextColor.SECONDARY, phoneAndOpHours);
        this.website = UIUtils.createLabel(FontSize.XS, TextColor.SECONDARY, website);

        this.program.setClassName(CLASS_NAME + "__program");
        this.organization.setClassName(CLASS_NAME + "__organization");
        this.address.setClassName(CLASS_NAME + "__address");
        this.phoneAndOpHours.setClassName(CLASS_NAME + "__phoneAndOpHours");
        this.website.setClassName(CLASS_NAME + "__website");

        content = new FlexBoxLayout(this.program, this.organization, this.address, this.phoneAndOpHours, this.website);
        content.setClassName(CLASS_NAME + "__content");
        content.setFlexDirection(FlexDirection.COLUMN);
        add(content);
    }
}
