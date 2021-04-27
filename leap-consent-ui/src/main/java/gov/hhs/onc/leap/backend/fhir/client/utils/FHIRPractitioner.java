package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRPractitioner {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Collection<Practitioner> getAllPractitionersWithinState() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String state = consentSession.getPrimaryState();
        Collection<Practitioner> consentCollection = new ArrayList<>();
        Bundle bundle = hapiFhirServer.getAllPractitioners(state);
        List<Bundle.BundleEntryComponent> resourceList = bundle.getEntry();
        Iterator iter = resourceList.iterator();
        while(iter.hasNext()) {
            Bundle.BundleEntryComponent b = (Bundle.BundleEntryComponent)iter.next();
            Practitioner c = (Practitioner) b.getResource();
            consentCollection.add(c);
        }
        return consentCollection;
    }

    public Practitioner createPractitioner(Practitioner practitioner) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(practitioner);
        Practitioner provider = (Practitioner)bundle.getEntry().get(0).getResource();
        return provider;
    }
}
