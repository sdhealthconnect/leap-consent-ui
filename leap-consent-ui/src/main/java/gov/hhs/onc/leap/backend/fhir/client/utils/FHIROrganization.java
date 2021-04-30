package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@Service
public class FHIROrganization {
    @Autowired
    private HapiFhirServer hapiFhirServer;

    public Collection<Organization> getAllOrganizationsWithinState() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String state = consentSession.getPrimaryState();
        Collection<Organization> organizationCollection = new ArrayList<>();
        List<IBaseResource> orgs = hapiFhirServer.getAllOrganizations(state);
        Iterator iter = orgs.iterator();
        while(iter.hasNext()) {
            Organization b = (Organization)iter.next();
            organizationCollection.add(b);
        }
        return organizationCollection;
    }

    public Organization createOrganization(Organization organization) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(organization);
        Organization org = (Organization)bundle.getEntry().get(0).getResource();
        return org;
    }

    public Organization getOrganizationById(String id) {
        Bundle bundle = hapiFhirServer.getOrganizationById(id);
        Organization org = (Organization)bundle.getEntry().get(0).getResource();
        return org;
    }
}
