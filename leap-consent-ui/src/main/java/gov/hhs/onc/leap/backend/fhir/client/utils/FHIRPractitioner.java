package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.instance.model.api.IBaseResource;
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
        Collection<Practitioner> practitionerCollection = new ArrayList<>();
        List<IBaseResource> practitioners = hapiFhirServer.getAllPractitioners(state);
        Iterator iter = practitioners.iterator();
        while(iter.hasNext()) {
            Practitioner c = (Practitioner) iter.next();
            practitionerCollection.add(c);
        }
        return practitionerCollection;
    }

    public Practitioner createPractitioner(Practitioner practitioner) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(practitioner);
        Practitioner provider = (Practitioner)bundle.getEntry().get(0).getResource();
        return provider;
    }
}
