package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.ConsentNotification;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FHIRResearchSubject {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Bundle createResearchSubject(ResearchSubject researchSubject) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(researchSubject);
        return bundle;
    }

    public List<Reference> getSubjectsWithSpecificCondition(String system, String code) {
        List<Reference> refList = hapiFhirServer.getSubjectsWithSpecificCondition(system, code);
        return refList;
    }

    public List<IBaseResource> getResearchSubjectsForSpecificPatientReference() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        ConsentUser consentUser = consentSession.getConsentUser();
        if (consentUser==null) {
            log.warn("There is no consent user set to retrieve the Patient from Fhir");
            return null;
        }
        String patientId = consentUser.getUser().getFhirPatientId();
        List<IBaseResource> res = hapiFhirServer.getSubjectsForSpecificPatientReference(patientId);
        return res;
    }

    public boolean consentDeclined(ResearchSubject researchSubject) {
        boolean res = false;
        researchSubject.setStatus(ResearchSubject.ResearchSubjectStatus.WITHDRAWN);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(researchSubject);
        return res;
    }

    public boolean consentGranted(ResearchSubject researchSubject) {
        boolean res = false;
        researchSubject.setStatus(ResearchSubject.ResearchSubjectStatus.CANDIDATE);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(researchSubject);
        return res;
    }

    public boolean consentRevoked(ResearchSubject researchSubject) {
        boolean res = false;
        researchSubject.setStatus(ResearchSubject.ResearchSubjectStatus.WITHDRAWN);
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(researchSubject);
        return res;
    }
}
