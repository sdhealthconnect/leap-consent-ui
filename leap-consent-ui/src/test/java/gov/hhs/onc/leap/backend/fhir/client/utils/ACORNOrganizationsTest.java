package gov.hhs.onc.leap.backend.fhir.client.utils;

import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class ACORNOrganizationsTest {

    @Autowired
    private FHIROrganization fhirOrganization;

    @BeforeEach
    void setup() {

    }

    @Test
    void deptOfVeteransAffairs() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-va");
        org.setName("US Dept. of Veterans Affairs");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.1");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Tucson");
        address.setState("AZ");
        address.setDistrict("Pima");
        address.addLine("3601 South Sixth Avenue");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-792-1450");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void azDeptOfHousing() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-azdoh");
        org.setName("Arizona Dept. of Housing");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.2");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Phoenix");
        address.setState("AZ");
        address.setDistrict("Maricopa");
        address.addLine("1110 W. Washington #280");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("602-771-1000");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void azDeptOfEconomicSecurity() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-azdes");
        org.setName("Arizona Dept. of Economic Security");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.3");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Nogales");
        address.setState("AZ");
        address.setDistrict("Santa Cruz");
        address.addLine("1843 N State Dr");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("855-432-7587");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void santaCruzCountyHousingAuthority() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-sccha");
        org.setName("Santa Cruz County Housing Authority");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.4");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Nogales");
        address.setState("AZ");
        address.setDistrict("Santa Cruz");
        address.addLine("2150 North Congress Drive");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-375-7800");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void southeasternArizonCommunityActionProgram() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-seacap");
        org.setName("Southeastern Arizona Community Action Program");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.5");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Stafford");
        address.setState("AZ");
        address.setDistrict("Santa Cruz");
        address.addLine("283 West 5th Street");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("928-428-4653");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void communityPartners() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-cp");
        org.setName("Community Partners");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.6");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Tucson");
        address.setState("AZ");
        address.setDistrict("Pima");
        address.addLine("401 N. Bonita Ave");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-721-1887");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void southerArizonaLegalAid() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-sala");
        org.setName("Southern Arizona Legal Aid");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.7");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Tucson");
        address.setState("AZ");
        address.setDistrict("Pima");
        address.addLine("2343 E Broadway Blvd #200");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-623-9461");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void arizonaDeptOfHealthServices() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-azdhs");
        org.setName("Arizona Department of Health Services");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.8");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Tucson");
        address.setState("AZ");
        address.setDistrict("Pima");
        address.addLine("1109 W Prince Road #111");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-887-2643");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void arizonaDeptOfEducation() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-azed");
        org.setName("Arizona Department of Education");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.9");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Phoenix");
        address.setState("AZ");
        address.setDistrict("Maricopa");
        address.addLine("1109 W Prince Road");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("602-542-5393");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void arizonaDeptOfVeteransServices() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-azdvs");
        org.setName("Arizona Department of Veterans Services");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.10");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Green Valley");
        address.setState("AZ");
        address.setDistrict("Santa Cruz");
        address.addLine("380 W Vista Hermosa Dr #140");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-629-4900");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }

    @Test
    void santaCruzCounty() {
        Organization org = new Organization();
        org.setId("acorn-sdoh-org-scc");
        org.setName("Santa Cruz Count");
        Identifier id = new Identifier();
        id.setSystem("urn:ietf:rfc:3986");
        id.setValue("urn:oid:9.1.1.1.11");
        List<Identifier> idList = new ArrayList<>();
        idList.add(id);
        org.setIdentifier(idList);
        org.setActive(true);
        Address address = new Address();
        address.setCity("Nogales");
        address.setState("AZ");
        address.setDistrict("Santa Cruz");
        address.addLine("2150 North Congress Drive");
        List<Address> addressList = new ArrayList<>();
        addressList.add(address);
        org.setAddress(addressList);
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactPoint.setValue("520-375-7800");
        Organization.OrganizationContactComponent organizationContactComponent = new Organization.OrganizationContactComponent();
        organizationContactComponent.addTelecom(contactPoint);
        List<Organization.OrganizationContactComponent> organizationContactComponentList = new ArrayList<>();
        organizationContactComponentList.add(organizationContactComponent);
        org.setContact(organizationContactComponentList);

        fhirOrganization.createOrganization(org);
    }
}
