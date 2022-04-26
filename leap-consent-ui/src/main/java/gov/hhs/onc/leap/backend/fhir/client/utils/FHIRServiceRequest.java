package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRServiceRequest {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Collection<ServiceRequest> getPatientServiceRequests() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientId = consentSession.getFhirPatient().getId();
        Collection<ServiceRequest> serviceRequestCollection = new ArrayList<>();
        Bundle bundle = hapiFhirServer.getServiceRequests(patientId);
        List<Bundle.BundleEntryComponent> resourceList = bundle.getEntry();
        Iterator iter = resourceList.iterator();
        while(iter.hasNext()) {
            Bundle.BundleEntryComponent b = (Bundle.BundleEntryComponent)iter.next();
            ServiceRequest c = (ServiceRequest) b.getResource();
            serviceRequestCollection.add(c);
        }
        return serviceRequestCollection;
    }

    public Collection<ServiceRequest> getPatientSDOHReferrals(String fhirPatientId) {
        Collection<ServiceRequest> serviceRequests = new ArrayList<>();
        Collection<IBaseResource> resources = hapiFhirServer.getAllPatientSDOHReferrals(fhirPatientId);
        Iterator iter = resources.iterator();
        while (iter.hasNext()) {
            ServiceRequest serviceRequest = (ServiceRequest) iter.next();
            serviceRequests.add(serviceRequest);
        }
        return serviceRequests;
    }

    public ServiceRequest createServiceRequest(ServiceRequest serviceRequest) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(serviceRequest);
        ServiceRequest res = (ServiceRequest) bundle.getEntry().get(0).getResource();
        return res;
    }

    public void deleteTestServiceRequestResource(String id) {
        hapiFhirServer.deleteTestServiceRequestResource(id);
    }
}
