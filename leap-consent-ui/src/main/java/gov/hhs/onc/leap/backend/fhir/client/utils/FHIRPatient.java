package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
/**
 * A service to manipulate a Fhir Patient Resource.
 *
 * @author: sgroh@saperi.io
 */
public class FHIRPatient {
    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Patient get() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        ConsentUser consentUser = consentSession.getConsentUser();
        if (consentUser==null) {
            log.warn("There is no consent user set to retrieve the Patient from Fhir");
            return null;
        }
        String patientId = consentUser.getUser().getFhirPatientId();
        Bundle bundle = hapiFhirServer.getPatientBundle(patientId);
        List<Bundle.BundleEntryComponent> resourceList = bundle.getEntry();
        Iterator iter = resourceList.iterator();
        Patient p = null;
        if (iter.hasNext()) {
            Bundle.BundleEntryComponent b = (Bundle.BundleEntryComponent)iter.next();
            p = (Patient) b.getResource();
        }
        return p;
    }
}
