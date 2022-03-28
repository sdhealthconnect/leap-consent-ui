package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FHIRCondition {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Condition createCondition(Condition condition) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(condition);
        Condition conditionRes = (Condition)bundle.getEntry().get(0).getResource();
        return conditionRes;
    }
}
