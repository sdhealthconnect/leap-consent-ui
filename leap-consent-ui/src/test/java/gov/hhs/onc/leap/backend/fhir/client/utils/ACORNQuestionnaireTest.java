package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.sdoh.data.ACORNDisplayData;
import org.hl7.fhir.r4.model.Questionnaire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class ACORNQuestionnaireTest {
    private ACORNDisplayData displayData;

    @Autowired
    private FHIRQuestionnaire fhirQuestionnaire;

    @BeforeEach
    void setup() {
        displayData = new ACORNDisplayData();
    }

    @Test
    void createACORNQuestionnaire() {

        Questionnaire q = displayData.createFHIRQuestionnaire();
        fhirQuestionnaire.createQuestionnaire(q);

    }

}
