package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Immunization;
import org.hl7.fhir.r4.model.Procedure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIRImmunization {

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Immunization createImmunization(Immunization immunization) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(immunization);
        Immunization res = (Immunization) bundle.getEntry().get(0).getResource();
        return res;
    }

    public Collection<Immunization> getPatientImmunizations(String fhirPatientId) {
        Collection<Immunization> results = new ArrayList<>();
        List<IBaseResource> res = hapiFhirServer.getAllPatientImmunizations(fhirPatientId);
        Iterator iter = res.iterator();
        while (iter.hasNext()) {
            Immunization cond = (Immunization) iter.next();
            results.add(cond);
        }
        return results;
    }
}
