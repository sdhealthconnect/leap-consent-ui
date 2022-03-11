package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
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
        List<IBaseResource> resourceList = hapiFhirServer.getMedicationRequests(patientId);
        Iterator iter = resourceList.iterator();
        while(iter.hasNext()) {
            MedicationRequest c = (MedicationRequest)iter.next();
            medicationRequestCollection.add(c);
        }
        return medicationRequestCollection;
    }

    public MedicationRequest getMedicationRequestByID(String url) {
        MedicationRequest res = new MedicationRequest();
        Bundle bundle = hapiFhirServer.getMedicationRequestById(url);
        Bundle.BundleEntryComponent b = bundle.getEntryFirstRep();
        res = (MedicationRequest) b.getResource();
        return res;
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

    public boolean consentRevoked(MedicationRequest medRequest) {
        boolean res = false;
        medRequest.setStatus(MedicationRequest.MedicationRequestStatus.STOPPED);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(medRequest);
        return res;
    }

    public Bundle updateMedicationRequest(MedicationRequest medicationRequest) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(medicationRequest);
        return bundle;
    }
}
