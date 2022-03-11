package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.medrec.client.MedicationSummaryClient;
import gov.hhs.onc.leap.medrec.model.MedicationSummary;
import gov.hhs.onc.leap.medrec.model.MedicationSummaryList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Iterator;
import java.util.List;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class MedRecSummaryTest {

    @Value("${sh.url:http://localhost:8081}")
    private String shHost;

    @Test
    void getMedicationSummaryTest() {
        MedicationSummaryClient client = new MedicationSummaryClient(shHost);
        MedicationSummaryList medicationSummaryList = client.getMedicationSummary("3351");
        List<MedicationSummary> activeMeds = medicationSummaryList.getActiveMedications();
        Iterator iter = activeMeds.iterator();
        while(iter.hasNext()) {
            MedicationSummary summary = (MedicationSummary) iter.next();
            System.out.println("Medication Name "+summary.getMedication());
        }
    }
}
