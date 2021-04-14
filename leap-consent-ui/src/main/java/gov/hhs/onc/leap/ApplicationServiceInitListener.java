package gov.hhs.onc.leap;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import org.springframework.stereotype.Component;

import java.util.Locale;

import static java.lang.System.setProperty;

@Component
public class ApplicationServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent e) {
        setProperty("vaadin.i18n.provider", VaadinI18NProvider.class.getName());

        e.getSource().addUIInitListener(uiInitListener-> {
            final VaadinRequest request = VaadinRequest.getCurrent();
            final VaadinSession session = uiInitListener.getUI().getSession();
            // Override the session locale with the request locale
            Locale defaultLocale = request.getLocale() == null ? session.getLocale(): request.getLocale();
            session.setLocale(defaultLocale);
        } );
    }

}
