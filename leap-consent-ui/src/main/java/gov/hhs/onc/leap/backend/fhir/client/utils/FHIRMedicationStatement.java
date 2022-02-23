package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.MedicationStatement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

@Service
public class FHIRMedicationStatement {

    @Autowired
    private HapiFhirServer hapiFhirServer;


    public Collection<MedicationStatement> getMedicationStatementsByPatientId(String patientId) {
        Collection<MedicationStatement> results = new ArrayList<>();
        return results;
    }
}
