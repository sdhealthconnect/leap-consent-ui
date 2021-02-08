package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Bundle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FHIRAudit {
    private HapiFhirServer client;

    public Collection<AuditEvent> getPatientAuditEvents() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientId = consentSession.getFhirPatient().getId();
        Collection<AuditEvent> consentCollection = new ArrayList<>();
        Bundle bundle = getClient().getAllPatientAuditEvents(patientId);
        List<Bundle.BundleEntryComponent> resourceList = bundle.getEntry();
        Iterator iter = resourceList.iterator();
        while(iter.hasNext()) {
            Bundle.BundleEntryComponent b = (Bundle.BundleEntryComponent)iter.next();
            AuditEvent c = (AuditEvent) b.getResource();
            consentCollection.add(c);
        }
        return consentCollection;
    }

    private HapiFhirServer getClient() {
        if (client == null) {
            client = new HapiFhirServer();
            client.setUp();
        }
        return client;
    }
}
