package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class AuditEventService {

    @Autowired
    HapiFhirServer hapiFhirServer;


    public Collection<ConsentLog> getConsentLogs(final String fhirPatientId) {
        List<ConsentLog> logs = new ArrayList<>();
        try {
            List<IBaseResource> entries = hapiFhirServer.getAuditEvents(fhirPatientId);
            if (entries != null) {
                Iterator iter = entries.iterator();
                while (iter.hasNext()) {
                    try {
                        AuditEvent aEvent = (AuditEvent) iter.next();
                        String decision = aEvent.getOutcomeDesc();
                        LocalDateTime dateTime = aEvent.getMeta().getLastUpdated().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                        String purposeOfUse = aEvent.getAgent().get(0).getPurposeOfUse().get(0).getCoding().get(0).getCode();
                        String action = aEvent.getType().getDisplay();
                        Identifier destinationIdentifier = aEvent.getAgentFirstRep().getWho().getIdentifier();
                        String destination = "Unknown";
                        // it may be a organization or practitioner
                        try {
                            destination = findOrganization(destinationIdentifier);
                        } catch (Exception ex) {
                            log.warn("Error searching for organization " + ex.getMessage());
                        }
                        if (destination == null || destination.equals("Unknown")) {
                            //try practitioner
                            try {
                                destination = findPractitioner(destinationIdentifier);
                            } catch (Exception ex) {
                                log.warn("Error searching for practitioner " + ex.getMessage());
                            }
                        }
                        if (destination == null) destination = "Unknown";
                        ConsentLog consentLog = new ConsentLog(decision, dateTime, destination, purposeOfUse, action, aEvent);
                        logs.add(consentLog);
                    }
                    catch (Exception ex) {
                        log.error("Failed to process auditevent "+ex.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Could not retrieve Audit log Events from the Fhir Server", e);
        }
        return logs;
    }

    private String findOrganization(Identifier destinationIdentifier ) {
        try {
                Bundle bundleOrg = hapiFhirServer.getOrganization(destinationIdentifier);
                if (bundleOrg.getEntry() != null) {
                    return ((Organization) bundleOrg.getEntry().get(0).getResource()).getName();
                }
        } catch (Exception e) {
            log.info("No matching organization", e);
            return null;
        }
        return null;
    }

    private String findPractitioner(Identifier destinationIdentifier) {
        try {
            Bundle bundlePractitioner = hapiFhirServer.getPractitioner(destinationIdentifier);
            if (bundlePractitioner.getEntry() != null) {
                return ((Practitioner) bundlePractitioner.getEntry().get(0).getResource()).getName().get(0).getNameAsSingleString();
            }
        } catch (Exception e) {
            log.info("No matching practitioner", e);
            return null;
        }
        return null;
    }
}
