package gov.hhs.onc.leap.backend.fhir.client.utils;

import org.hl7.fhir.r4.model.*;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestPropertySource("classpath:config/application-test.yaml")
public class FHIRResearchStudyTest {

    private Organization organization;

    private Practitioner practitioner;

    private Bundle resultStudy;

    @Autowired
    private FHIROrganization fhirOrganization;

    @Autowired
    private FHIRPractitioner fhirPractitioner;

    @Autowired
    private FHIRResearchStudy fhirResearchStudy;

    @BeforeEach
    void setup() {
        //create study organization
        Organization org = new Organization();
        org.setId("asu-edu");
        org.setActive(true);
        Address address = new Address();
        address.setCity("Tempe");
        address.setState("Arizona");
        address.setCountry("United States");
        address.setPostalCode("85281");
        List<Address> lAddress = new ArrayList<>();
        lAddress.add(address);
        org.setAddress(lAddress);
        org.setName("Arizona State University");
        Identifier identifier = new Identifier();
        identifier.setSystem("urn:ietf:rfc:3986");
        identifier.setValue("urn:oid:1.1.1.10");
        List<Identifier> identifierList = new ArrayList<>();
        identifierList.add(identifier);
        org.setIdentifier(identifierList);

        try {
            organization = fhirOrganization.createOrganization(org);
        }
        catch (Exception ex) {}

        //create principle investigator
        Practitioner p = new Practitioner();
        p.setActive(true);
        p.setId("asu-edu-buman-matthew");
        Address addressPractitioner = new Address();
        addressPractitioner.setCity("Tempe");
        addressPractitioner.setState("Arizona");
        addressPractitioner.setCountry("United States");
        addressPractitioner.setPostalCode("85281");
        List<Address> addressListPractitioner = new ArrayList<>();
        addressListPractitioner.add(addressPractitioner);
        HumanName hName = new HumanName();
        hName.setFamily("Buman");
        StringType givenName = new StringType("Matthew");
        StringType middleName = new StringType("P");
        List<StringType> nameList = new ArrayList<>();
        nameList.add(givenName);
        nameList.add(middleName);
        hName.setGiven(nameList);
        StringType degree = new StringType("PhD");
        List<StringType> suffixList = new ArrayList<>();
        suffixList.add(degree);
        hName.setSuffix(suffixList);
        List<HumanName> humanNameList = new ArrayList<>();
        humanNameList.add(hName);
        p.setName(humanNameList);

        ContactPoint phoneContactPoint = new ContactPoint();
        ContactPoint.ContactPointSystem cSystem = ContactPoint.ContactPointSystem.PHONE;
        phoneContactPoint.setSystem(cSystem);
        phoneContactPoint.setValue("602-827-2289");

        ContactPoint emailContactPoint = new ContactPoint();
        ContactPoint.ContactPointSystem eSystem = ContactPoint.ContactPointSystem.EMAIL;
        emailContactPoint.setSystem(eSystem);
        emailContactPoint.setValue("matthew.buman@asu.edu");

        List<ContactPoint> telcom = new ArrayList<>();
        telcom.add(phoneContactPoint);
        telcom.add(emailContactPoint);
        p.setTelecom(telcom);

        Identifier pIdentifier = new Identifier();
        pIdentifier.setSystem("https://www.asu.edu");
        pIdentifier.setValue("matthew.buman@asu.edu");
        List<Identifier> identifierPList = new ArrayList<>();
        identifierPList.add(pIdentifier);
        p.setIdentifier(identifierPList);

        try {
            practitioner = fhirPractitioner.createPractitioner(p);
        }
        catch (Exception ex) { }
    }

    @Test
    void createResearchStudy() throws Exception {
        ResearchStudy researchStudy = new ResearchStudy();
        researchStudy.setId("NCT04269070");
        researchStudy.setTitle("WorkWell: A Pre-clinical Pilot Study of Increased Standing and Light-intensity Physical Activity in Prediabetic Sedentary Office Workers");
        researchStudy.setDescription("This pilot study is being conducted to determine whether a range of pre-clinical " +
                "cardiometabolic biomarkers (measured via gut microbiome, blood draw) can be improved via regular intervals " +
                "of standing and light-intensity physical activity (i.e., leisurely walking) in real-world office environments. " +
                "This trial is meant to generate pilot data which will lead to additional clinical trials.\n" +
                "Primary Hypothesis: Increasing both standing and light-intensity physical activity will improve biomarkers " +
                "of metabolic function, as measured by blood metabolites and differential abundance of gut microbiome composition, " +
                "compared to a control condition of normal workplace behavior.");

        researchStudy.setStatus(ResearchStudy.ResearchStudyStatus.ACTIVE);
        CodeableConcept condition = new CodeableConcept();
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("15777000");
        coding.setDisplay("Prediabetes");
        List<Coding> codeList = new ArrayList<>();
        codeList.add(coding);
        condition.setCoding(codeList);
        List<CodeableConcept> conditionList = new ArrayList<>();
        conditionList.add(condition);
        researchStudy.setCondition(conditionList);
        Reference orgReference = new Reference();
        orgReference.setReference("Organization/asu-edu");
        orgReference.setDisplay("Arizona State University");
        researchStudy.setSponsor(orgReference);

        List<ResearchStudy.ResearchStudyArmComponent> armList = new ArrayList<>();
        ResearchStudy.ResearchStudyArmComponent arm1 = new ResearchStudy.ResearchStudyArmComponent();
        arm1.setName("Active Comparator: Move, Stand");
        arm1.setDescription("Usual behavior condition, followed by the standing condition, followed by the LPA condition.");
        ResearchStudy.ResearchStudyArmComponent arm2 = new ResearchStudy.ResearchStudyArmComponent();
        arm2.setName("Active Comparator: Stand, Move");
        arm2.setDescription("Usual behavior condition, followed by the LPA condition, followed by the standing condition.");
        armList.add(arm1);
        armList.add(arm2);
        researchStudy.setArm(armList);
        ContactDetail contactDetail = new ContactDetail();
        contactDetail.setName("Matthew P Buman, PhD");

        ContactPoint phoneContactPoint = new ContactPoint();
        ContactPoint.ContactPointSystem cSystem = ContactPoint.ContactPointSystem.PHONE;
        phoneContactPoint.setSystem(cSystem);
        phoneContactPoint.setValue("602-827-2289");

        ContactPoint emailContactPoint = new ContactPoint();
        ContactPoint.ContactPointSystem eSystem = ContactPoint.ContactPointSystem.EMAIL;
        emailContactPoint.setSystem(eSystem);
        emailContactPoint.setValue("matthew.buman@asu.edu");

        List<ContactPoint> telcom = new ArrayList<>();
        telcom.add(phoneContactPoint);
        telcom.add(emailContactPoint);
        contactDetail.setTelecom(telcom);

        List<ContactDetail> contactDetails = new ArrayList<>();
        contactDetails.add(contactDetail);
        researchStudy.setContact(contactDetails);

        List<Identifier> identifiers = new ArrayList<>();
        Identifier identifier = new Identifier();
        identifier.setValue("NCT04269070");
        identifier.setSystem("https://clinicaltrials.gov");
        researchStudy.setIdentifier(identifiers);

        resultStudy = fhirResearchStudy.createResearchStudy(researchStudy);
        Assert.assertNotNull(resultStudy);
    }


}
