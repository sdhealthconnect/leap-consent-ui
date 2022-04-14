package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRProcedure {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Procedure createCondition(Procedure procedure) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(procedure);
        Procedure res = (Procedure) bundle.getEntry().get(0).getResource();
        return res;
    }

    public Collection<Procedure> getPatientProcedures(String fhirPatientId) {
        Collection<Procedure> results = new ArrayList<>();
        List<IBaseResource> res = hapiFhirServer.getAllPatientProcedures(fhirPatientId);
        Iterator iter = res.iterator();
        while (iter.hasNext()) {
            Procedure cond = (Procedure) iter.next();
            results.add(cond);
        }
        return results;
    }
}
