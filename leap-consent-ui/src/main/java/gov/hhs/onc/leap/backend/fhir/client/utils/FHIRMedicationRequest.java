package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRMedicationRequest {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Collection<MedicationRequest> getPatientMedicationRequests() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientId = consentSession.getFhirPatientId();
        Collection<MedicationRequest> medicationRequestCollection = new ArrayList<>();
        Bundle bundle = hapiFhirServer.getMedicationRequests(patientId);
        List<Bundle.BundleEntryComponent> resourceList = bundle.getEntry();
        Iterator iter = resourceList.iterator();
        while(iter.hasNext()) {
            Bundle.BundleEntryComponent b = (Bundle.BundleEntryComponent)iter.next();
            MedicationRequest c = (MedicationRequest) b.getResource();
            medicationRequestCollection.add(c);
        }
        return medicationRequestCollection;
    }

    public boolean consentDeclined(MedicationRequest medRequest) {
        boolean res = false;
        medRequest.setStatus(MedicationRequest.MedicationRequestStatus.CANCELLED);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(medRequest);
        return res;
    }

    public boolean consentGranted(MedicationRequest medRequest) {
        boolean res = false;
        medRequest.setStatus(MedicationRequest.MedicationRequestStatus.ACTIVE);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(medRequest);
        return res;
    }
}
