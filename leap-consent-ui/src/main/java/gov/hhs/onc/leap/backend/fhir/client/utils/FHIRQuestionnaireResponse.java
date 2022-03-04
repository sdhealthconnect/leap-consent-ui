package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FHIRQuestionnaireResponse {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public QuestionnaireResponse createQuestionnaireResponse(QuestionnaireResponse questionnaireResponse) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(questionnaireResponse);
        QuestionnaireResponse questionnaireResponseResult = (QuestionnaireResponse) bundle.getEntry().get(0).getResource();
        return questionnaireResponseResult;
    }

    public QuestionnaireResponse getQuestionnaireResponse(String questionnaireId, String patientId) {
        Bundle bundle = hapiFhirServer.getQuestionnaireResponse(questionnaireId, patientId);
        QuestionnaireResponse questionnaireResponseResult = (QuestionnaireResponse) bundle.getEntry().get(0).getResource();
        return questionnaireResponseResult;
    }

    public QuestionnaireResponse getQuestionnaireResponseById(String questionnaireResponseId) {
        QuestionnaireResponse resp = hapiFhirServer.getQuestionnaireResponseById(questionnaireResponseId);
        return resp;
    }
}
