package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRObservation {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Observation createObservation(Observation observation) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(observation);
        Observation res = (Observation) bundle.getEntry().get(0).getResource();
        return res;
    }

    public Collection<Observation> getPatientObservationsByType(String fhirPatientId, String categorycode) {
        Collection<Observation> results = new ArrayList<>();
        List<IBaseResource> res = hapiFhirServer.getAllPatientObservations(fhirPatientId, categorycode);
        Iterator iter = res.iterator();
        while (iter.hasNext()) {
            Observation cond = (Observation) iter.next();
            results.add(cond);
        }
        return results;
    }
}
