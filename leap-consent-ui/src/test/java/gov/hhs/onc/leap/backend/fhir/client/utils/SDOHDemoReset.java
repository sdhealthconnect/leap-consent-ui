package gov.hhs.onc.leap.backend.fhir.client.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;
import java.util.List;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class SDOHDemoReset {

    @Autowired
    private FHIRCondition fhirCondition;

    @Autowired
    private FHIRServiceRequest fhirServiceRequest;

    @Autowired
    private FHIRConsent fhirConsent;

    @Autowired
    private FHIRQuestionnaireResponse fhirQuestionnaireResponse;

    //for demo6@gmail.com user
    private String patientId = "2047";
    private List<String> sdohList = List.of("SDOHHousingInstability", "SDOHFoodInsecurity",
                                                 "SDOHUtilityAccess", "SDOHTransportationAccess",
                                                 "SDOHPersonalSafety", "SDOHSocialSupport",
                                                  "SDOHEmploymentAndEducation", "SDOHLegalSupport");

    @Test
    void removeConditions() {
        Iterator iter = sdohList.iterator();
        while (iter.hasNext()) {
            String sdoh = (String)iter.next();
            try {
                fhirCondition.deleteTestConditionResource(sdoh + "-" + patientId);
            }
            catch (Exception ex) {}
        }
    }



    @Test
    void removeServiceRequests() {
        Iterator iter = sdohList.iterator();
        while (iter.hasNext()) {
            String sdoh = (String)iter.next();
            try {
                fhirServiceRequest.deleteTestServiceRequestResource(sdoh + "-Referral-" + patientId);
            }
            catch (Exception ex) {
            }
        }
    }

    @Test
    void removeQuestionnaireResponse() {
        try {
            fhirQuestionnaireResponse.deleteTestResource("QuestionnaireResponse/acorn-"+patientId);
        }
        catch (Exception ex) {

        }
    }

    @Test
    void removeConsent() {
        try {
            fhirConsent.deleteTestResource("Consent/acorn-sdoh-"+patientId);
        }
        catch (Exception ex) {

        }
    }
}
