package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRCondition {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Condition createCondition(Condition condition) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(condition);
        Condition conditionRes = (Condition)bundle.getEntry().get(0).getResource();
        return conditionRes;
    }

    public Collection<Condition> getPatientConditions(String fhirPatientId) {
        Collection<Condition> results = new ArrayList<>();
        List<IBaseResource> res = hapiFhirServer.getAllConditionsForPatient(fhirPatientId);
        Iterator iter = res.iterator();
        while (iter.hasNext()) {
            Condition cond = (Condition) iter.next();
            results.add(cond);
        }
        return results;
    }

    public void deleteTestConditionResource(String id) {
        hapiFhirServer.deleteTestConditionResource(id);
    }
}
