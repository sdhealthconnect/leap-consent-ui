package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Questionnaire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FHIRQuestionnaire {
    //class utilized for loading various LEAP consent questionnaires

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Questionnaire createQuestionnaire(Questionnaire questionnaire) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(questionnaire);
        Questionnaire questionnaireRes = (Questionnaire) bundle.getEntry().get(0).getResource();
        return questionnaireRes;
    }
}
