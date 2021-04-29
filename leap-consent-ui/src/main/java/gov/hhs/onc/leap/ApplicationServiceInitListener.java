package gov.hhs.onc.leap;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.ui.util.UIUtils;
import org.springframework.stereotype.Component;

import static java.lang.System.setProperty;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent e) {
        setProperty("vaadin.i18n.provider", VaadinI18NProvider.class.getName());

        e.getSource().addUIInitListener(uiInitListener-> {
            final VaadinRequest request = VaadinRequest.getCurrent();
            final VaadinSession session = uiInitListener.getUI().getSession();
            UIUtils.setLanguage(request, session);
        } );
    }



}
