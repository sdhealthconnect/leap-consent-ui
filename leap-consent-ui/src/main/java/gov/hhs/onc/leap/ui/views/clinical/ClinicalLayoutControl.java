package gov.hhs.onc.leap.ui.views.clinical;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ClinicalLayoutControl extends VerticalLayout {
    private Icon controlIcon;

    public ClinicalLayoutControl() {
        init();
        setHeight("300px");
        setWidth("360px");
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
        add(controlIcon);
    }

    private void init() {
        controlIcon = new Icon(VaadinIcon.PLUS_CIRCLE_O);
        controlIcon.addClassName("size-l");
        controlIcon.addClickListener(iconClickEvent -> {
            System.out.println("Control Icon Clicked");
        });
    }
}
