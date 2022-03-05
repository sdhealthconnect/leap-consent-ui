package gov.hhs.onc.leap.backend.fhir.client.utils;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;
import java.util.List;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class ACORNQuestionnaireResponseTest {
    private String questionnaireResponseId = "acorn-3351";
    private String questionnaireId = "acorn-himss2022-demonstration";

    private QuestionnaireResponse acornQuestionnaireResponse;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    @BeforeEach
    void setup() {
        acornQuestionnaireResponse = fhirQuestionnaireResponse.getQuestionnaireResponseById(questionnaireResponseId);
    }

    @Test
    void questionnaireResponseExtensionResultTests() {
        List<QuestionnaireResponse.QuestionnaireResponseItemComponent> compList =  acornQuestionnaireResponse.getItem();
        Iterator iter = compList.iterator();
        while (iter.hasNext()) {
            QuestionnaireResponse.QuestionnaireResponseItemComponent itemComponent = (QuestionnaireResponse.QuestionnaireResponseItemComponent) iter.next();
            System.out.println(itemComponent.getLinkId());
            if (itemComponent.getLinkId().indexOf("-need") > -1) {
                String linkId = itemComponent.getLinkId();
                List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answerList = itemComponent.getAnswer();
                Iterator iter2 = answerList.iterator();
                while (iter2.hasNext()) {
                    QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answerComponent = (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent) iter2.next();
                    BooleanType bType = (BooleanType)answerComponent.getValue();
                    System.out.println(linkId+"  "+bType.fhirType().toString());
                }
            }
            else {
                List<QuestionnaireResponse.QuestionnaireResponseItemComponent> compList2 = itemComponent.getItem();
                Iterator iter3 = compList2.iterator();
                while(iter3.hasNext()) {
                    QuestionnaireResponse.QuestionnaireResponseItemComponent itemComponent2 = (QuestionnaireResponse.QuestionnaireResponseItemComponent) iter3.next();
                    String linkId2 = itemComponent2.getLinkId();
                    if (itemComponent2.getLinkId().indexOf("-need") > -1) {
                        List<QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent> answerList2 = itemComponent2.getAnswer();
                        Iterator iter4 = answerList2.iterator();
                        while (iter4.hasNext()) {
                            QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent answerComponent2 = (QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent) iter4.next();
                            BooleanType bType2 = (BooleanType)answerComponent2.getValue();
                            System.out.println(linkId2+"  "+bType2.getValue());
                        }
                    }
                }
            }
        }
    }
}
