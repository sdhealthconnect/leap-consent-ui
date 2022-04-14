package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRAllergyIntolerance {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public AllergyIntolerance createCondition(AllergyIntolerance allergyIntolerance) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(allergyIntolerance);
        AllergyIntolerance res = (AllergyIntolerance) bundle.getEntry().get(0).getResource();
        return res;
    }

    public Collection<AllergyIntolerance> getPatientAllergyIntolerances(String fhirPatientId) {
        Collection<AllergyIntolerance> results = new ArrayList<>();
        List<IBaseResource> res = hapiFhirServer.getAllAllergyIntolerancesForPatient(fhirPatientId);
        Iterator iter = res.iterator();
        while (iter.hasNext()) {
            AllergyIntolerance cond = (AllergyIntolerance) iter.next();
            results.add(cond);
        }
        return results;
    }
}
