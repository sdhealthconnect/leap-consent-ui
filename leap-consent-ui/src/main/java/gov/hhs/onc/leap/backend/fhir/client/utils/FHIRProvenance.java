package gov.hhs.onc.leap.backend.fhir.client.utils;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class FHIRProvenance {
    private final String PROVENANCE_AGENT = "LEAP Consent Management Service";
    private final String PROVENANCE_AGENT_SYSTEM = "http://terminology.hl7.org/CodeSystem/provenance-participant-type";
    private final String PROVENANCE_AGENT_CODE = "assembler";
    private final String PROVENANCE_AGENT_DISPLAY = "Assember";

    private final String PROVENANCE_ACTIVITY_SYSTEM = "http://terminology.hl7.org/CodeSystem/v3-DataOperation";
    private final String PROVENANCE_ACTIVITY_CODE= "CREATE";
    private final String PROVENANCE_ACTIVITY_DISPLAY = "create";

    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Provenance createProvenance(String consentReference, Date recorded, String questionnaireReference) {
        Provenance provenance = new Provenance();
        provenance.setId(UUID.randomUUID().toString());
        //reference to consent resource
        Reference targetReference = new Reference();
        targetReference.setReference(consentReference);
        provenance.getTarget().add(targetReference);
        //recorded date
        provenance.setRecorded(recorded);
        //activity
        Coding activityCoding = new Coding();
        activityCoding.setSystem(PROVENANCE_ACTIVITY_SYSTEM);
        activityCoding.setCode(PROVENANCE_ACTIVITY_CODE);
        activityCoding.setDisplay(PROVENANCE_ACTIVITY_DISPLAY);
        provenance.getActivity().addCoding(activityCoding);
        //agent
        Coding agentCoding = new Coding();
        agentCoding.setSystem(PROVENANCE_AGENT_SYSTEM);
        agentCoding.setCode(PROVENANCE_AGENT_CODE);
        agentCoding.setDisplay(PROVENANCE_AGENT_DISPLAY);

        Provenance.ProvenanceAgentComponent agentComponent = new Provenance.ProvenanceAgentComponent();
        agentComponent.getType().addCoding(agentCoding);
        agentComponent.getWho().setDisplay(PROVENANCE_AGENT);

        provenance.getAgent().add(agentComponent);
        //entity
        Provenance.ProvenanceEntityComponent entityComponent = new Provenance.ProvenanceEntityComponent();
        entityComponent.setRole(Provenance.ProvenanceEntityRole.SOURCE);
        Reference entityReference = new Reference();
        entityReference.setReference(questionnaireReference);
        entityComponent.setWhat(entityReference);

        provenance.getEntity().add(entityComponent);


        Bundle bundle = hapiFhirServer.createAndExecuteBundle(provenance);
        Provenance provenanceRes = (Provenance) bundle.getEntry().get(0).getResource();
        return provenanceRes;
    }
}
