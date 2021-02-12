package gov.hhs.onc.leap.ui.components.navigation;

import com.vaadin.flow.component.html.Span;

public class BasicDivider extends Span {

    public BasicDivider() {
        getStyle().set("background-color", "#5F9EA0");
        getStyle().set("flex", "0 0 2px");
        getStyle().set("align-self", "stretch");
    }
}
