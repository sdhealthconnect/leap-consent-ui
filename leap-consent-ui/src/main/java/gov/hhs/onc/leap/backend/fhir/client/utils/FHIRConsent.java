package gov.hhs.onc.leap.backend.fhir.client.utils;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FHIRConsent {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Consent createConsent() {
        ConsentSession consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(consentSession.getCurrentConsentObject());
        Consent consent = (Consent)bundle.getEntry().get(0).getResource();
        return consent;
    }

    public Consent createConsent(Consent consent) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(consent);
        Consent consentRes = (Consent)bundle.getEntry().get(0).getResource();
        return consentRes;
    }


    public Consent getConsent(String consentId) {
        Bundle bundle = hapiFhirServer.getConsentById(consentId);
        Bundle.BundleEntryComponent comp = bundle.getEntry().get(0);
        Consent consent = (Consent)comp.getResource();
        return consent;
    }

    public Collection<Consent> getPatientConsents() {
        ConsentSession consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        //get just the id portion
        String patientId = consentSession.getFhirPatientId();
        Collection<Consent> consentCollection = new ArrayList<>();
        List<IBaseResource> consents = hapiFhirServer.getAllConsentsForPatient(patientId);
        Iterator iter = consents.iterator();
        while(iter.hasNext()) {
            Consent b = (Consent)iter.next();
            consentCollection.add(b);
        }
        return consentCollection;
    }

    public boolean revokeConsent(Consent consent) {
        boolean res = false;
        consent.setStatus(Consent.ConsentState.REJECTED);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(consent);
        return res;
    }

    public boolean reinstateConsent(Consent consent) {
        boolean res = false;
        consent.setStatus(Consent.ConsentState.ACTIVE);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(consent);
        return res;
    }
}
