package gov.hhs.onc.leap.backend.fhir.client.utils;

import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.fhir.client.HapiFhirServer;
import gov.hhs.onc.leap.session.ConsentSession;
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
        Collection<Organization> consentCollection = new ArrayList<>();
        Bundle bundle = hapiFhirServer.getAllOrganizations(state);
        List<Bundle.BundleEntryComponent> resourceList = bundle.getEntry();
        Iterator iter = resourceList.iterator();
        while(iter.hasNext()) {
            Bundle.BundleEntryComponent b = (Bundle.BundleEntryComponent)iter.next();
            Organization c = (Organization) b.getResource();
            consentCollection.add(c);
        }
        return consentCollection;
    }

    public Organization createOrganization(Organization organization) {
        Bundle bundle = hapiFhirServer.createAndExecuteBundle(organization);
        Organization org = (Organization)bundle.getEntry().get(0).getResource();
        return org;
    }
}
