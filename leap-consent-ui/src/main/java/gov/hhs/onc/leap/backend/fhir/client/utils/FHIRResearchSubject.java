package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
}
