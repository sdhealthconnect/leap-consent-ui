package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FHIRResearchStudy {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Bundle createResearchStudy(ResearchStudy researchStudy) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(researchStudy);
        return bundle;
    }
}
