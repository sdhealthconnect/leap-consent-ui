package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.sdoh.data.ACORNDisplayData;
import org.aspectj.weaver.patterns.TypePatternQuestions;
import org.hl7.fhir.r4.model.Questionnaire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class ACORNQuestionnaireTest {
    private ACORNDisplayData displayData;

    private String questionnaireId = "acorn-himss2022-demonstration";

    private Questionnaire acornQuestionnaire;

    @Autowired
    private FHIRQuestionnaire fhirQuestionnaire;

    @BeforeEach
    void setup() {
        displayData = new ACORNDisplayData();
    }

    @Test
    void getFHIRACORNQuestionnaire() {
        acornQuestionnaire = fhirQuestionnaire.getQuestionnaire(questionnaireId);
        assertNotNull(acornQuestionnaire);
    }

    @Test
    void processFHIRQuestionnaire() {
        acornQuestionnaire = fhirQuestionnaire.getQuestionnaire(questionnaireId);
        List<Questionnaire.QuestionnaireItemComponent> qItems =  acornQuestionnaire.getItem();
        Iterator iter = qItems.iterator();
        while (iter.hasNext()) {
            Questionnaire.QuestionnaireItemComponent item = (Questionnaire.QuestionnaireItemComponent) iter.next();
            System.out.println(item.getLinkId() +" "+item.getType().getDisplay());
            if (item.getType().getDisplay().equals("Group")) {
                List<Questionnaire.QuestionnaireItemComponent> sItems = item.getItem();
                Iterator iter2 = sItems.iterator();
                while (iter2.hasNext()) {
                    Questionnaire.QuestionnaireItemComponent item2 = (Questionnaire.QuestionnaireItemComponent) iter2.next();
                    System.out.println("---> "+item2.getLinkId()+" "+item2.getText()+" "+item2.getType().getDisplay());
                    if (item2.getType().getDisplay().equals("Group")) {
                        List<Questionnaire.QuestionnaireItemComponent> answerOptions = item2.getItem();
                        Iterator iter3 = answerOptions.iterator();
                        while(iter3.hasNext()) {
                            Questionnaire.QuestionnaireItemComponent answer = (Questionnaire.QuestionnaireItemComponent) iter3.next();
                            System.out.println("---> ---> "+answer.getLinkId()+" "+answer.getText()+" "+answer.getType().getDisplay());
                        }
                    }
                }
            }
        }
    }

    @Test
    void testGetSpecificLinkId() {
        acornQuestionnaire = fhirQuestionnaire.getQuestionnaire(questionnaireId);
        Questionnaire.QuestionnaireItemComponent item = acornQuestionnaire.getQuestion("1");
        System.out.println(item.getText());
        List<Questionnaire.QuestionnaireItemComponent> itemList = item.getItem();
        Iterator iter2 = itemList.iterator();
        while(iter2.hasNext()) {
            Questionnaire.QuestionnaireItemComponent item2 = (Questionnaire.QuestionnaireItemComponent) iter2.next();
            System.out.println("---> "+item2.getLinkId()+" "+item2.getText()+" "+item2.getType().getDisplay());
            if (item2.getType().getDisplay().equals("Group")) {
                List<Questionnaire.QuestionnaireItemComponent> answerOptions = item2.getItem();
                Iterator iter3 = answerOptions.iterator();
                while(iter3.hasNext()) {
                    Questionnaire.QuestionnaireItemComponent answer = (Questionnaire.QuestionnaireItemComponent) iter3.next();
                    System.out.println("---> ---> "+answer.getLinkId()+" "+answer.getText()+" "+answer.getType().getDisplay());
                }
            }
        }
    }

}
