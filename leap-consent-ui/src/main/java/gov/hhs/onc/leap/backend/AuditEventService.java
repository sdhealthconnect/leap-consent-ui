package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.AuditEvent;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class AuditEventService {

    @Autowired
    HapiFhirServer hapiFhirServer;


    public Collection<ConsentLog> getConsentLogs(final String fhirPatientId) {
        List<ConsentLog> logs = new ArrayList<>();
        try {
            Bundle b = hapiFhirServer.getAuditEvents(fhirPatientId);
            List<Bundle.BundleEntryComponent> entries = b.getEntry();
            if (entries != null) {
                entries.stream().forEach(entry -> logs.add(new ConsentLog(((AuditEvent) entry.getResource()).getOutcomeDesc(),
                        entry.getResource().getMeta().getLastUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                        findOrganization(((AuditEvent) entry.getResource()).getAgent()), "")));
            }
        } catch (Exception e) {
            log.error("Could not retrieve Audit log Events from the Fhir Server", e);
        }
        return logs;

    }

    private String findOrganization(List<AuditEvent.AuditEventAgentComponent> agents) {
        try {
            if (agents != null) {
                //Using only the first element frrm the list
                AuditEvent.AuditEventAgentComponent agent = agents.stream().findFirst().get();
                String organizationId = agent.getWho().getIdentifier().getId();
                Bundle bundleOrg = hapiFhirServer.getOrganization(organizationId);
                if (bundleOrg.getEntry() != null) {
                    return ((Organization) bundleOrg.getEntry().get(0).getResource()).getName();
                }
            }
        } catch (Exception e) {
            log.error("Could not extract the Organization information from the Fhir Server", e);
            return null;
        }
        return null;
    }
}
