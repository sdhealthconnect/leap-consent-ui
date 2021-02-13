package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

public class TestData {
    private static final Random random = new Random(1);

    private static final Map<Long, ConsentDocument> CONSENT = new HashMap<>();

    private static final Map<Long, ConsentLog> CONSENT_LOG_MAP = new HashMap<>();

    public static Collection<ConsentDocument> getConsents() {
        return CONSENT.values();
    }

    public static Collection<ConsentLog> getConsentLogs() { return CONSENT_LOG_MAP.values(); }

    public static LocalDate getTodaysDate() {return LocalDate.now();}

    public static LocalDate getPastDate(int bound) {
        return LocalDate.now().minusDays(random.nextInt(bound));
    }

    public static LocalDate getFutureDate(int bound) {
        return LocalDate.now().plusDays(random.nextInt(bound));
    }

    static {
        long i = 0;
        CONSENT.put(i , new ConsentDocument(ConsentDocument.Status.ACTIVE, true, "CONSENT TO TREAT", "MARIPOSA Community Health Center", "MARIPOSA Community Health Center", getPastDate(1), getFutureDate(1), "N/A", "N/A", new Consent()));
        i = 2;
        CONSENT.put(i , new ConsentDocument(ConsentDocument.Status.ACTIVE, true, "CONSENT TO SHARE", "MARIPOSA Community Health Center", "Health Current", getPastDate(2), getFutureDate(300), "Active", "N/A", new Consent()));
        i = 3;
        CONSENT.put(i , new ConsentDocument(ConsentDocument.Status.EXPIRED, true, "CONSENT TO SHARE", "MARIPOSA Community Health Center", "Health Current", getPastDate(600), getPastDate(3), "Active", "N/A", new Consent()));
        i = 4;
        CONSENT.put(i , new ConsentDocument(ConsentDocument.Status.REVOKED, true, "CONSENT TO SHARE", "MARIPOSA Community Health Center", "Health Current", getPastDate(2), getFutureDate(300), "N/A", "N/A", new Consent()));

        long x = 0;
        CONSENT_LOG_MAP.put(x, new ConsentLog("Permit", LocalDate.now(), "Mariposa Community Clinic", "HealthCurrent"));
        x = 1;
        CONSENT_LOG_MAP.put(x, new ConsentLog("Deny", LocalDate.now(), "Dr. Bob", "HealthCurrent"));
        x = 3;
        CONSENT_LOG_MAP.put(x , new ConsentLog("No Consent", LocalDate.now(), "Kaiser Permanente", "HealthCurrent"));
    }

    public static String getUserId() {
        return "D123408";
    }

    public static String getPrimaryState() {
        return "Arizona";
    }

    public static String getLanguagePreference() {
        return "English";
    }

    public static String getUserName() {
        return "duane.decouteau@gmail.com";
    }


    public static Patient getPatient() {
        Patient patient = new Patient();
        patient.setId("privacy-consent-scenario-H-UI");
        List<Identifier> ids = new ArrayList<>();
        Identifier id1 = new Identifier();
        CodeableConcept concept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode("SS");
        coding.setSystem("http://terminology.hl7.org/CodeSystem/v2-0203");
        concept.addCoding(coding);
        id1.setType(concept);


        id1.setSystem("urn:oid:2.2");
        id1.setValue("D123408");

        Identifier id2 = new Identifier();
        id2.setSystem("http://sdhealthconnect.github.io/leap/samples/ids");
        id2.setValue("privacy-consent-scenario-H-UI");
        ids.add(id1);
        ids.add(id2);
        patient.setIdentifier(ids);

        patient.setActive(true);

        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("DeCouteau");
        List<StringType> givenList = new ArrayList<>();
        StringType sType = new StringType();
        sType.setValueAsString("Duane");
        givenList.add(sType);
        name.setGiven(givenList);
        patient.getName().add(name);

        List<Address> addressList = new ArrayList<>();
        Address address = new Address();
        address.setCity("Tumacacori");
        address.setState("Arizona");
        address.setPostalCode("85640");
        List<StringType> streetList = new ArrayList<>();
        StringType line1 = new StringType();
        line1.setValueAsString("P.O. Box 359");
        streetList.add(line1);
        address.setLine(streetList);
        addressList.add(address);
        patient.setAddress(addressList);

        List<ContactPoint> contactList = new ArrayList<>();
        ContactPoint contact = new ContactPoint();
        contact.setUse(ContactPoint.ContactPointUse.WORK);
        contact.setValue("406-410-0894");
        contact.setSystem(ContactPoint.ContactPointSystem.PHONE);
        contactList.add(contact);
        patient.setTelecom(contactList);

        Date bdate = new Date();
        try {
            bdate = new SimpleDateFormat("dd/MM/yyyy").parse("15/11/1959");
        }
        catch (Exception ex) {
            //blah blah
        }
        patient.setBirthDate(bdate);

        patient.setLanguage("English");

        return patient;
    }

    public static Organization getPrimaryOrganization() {
        Organization org = new Organization();
        org.setId("privacy-consent-scenario-H-healthcurrent");
        org.setName("HealthCurrent FHIR Connectathon");

        List<Identifier> ids = new ArrayList<>();
        Identifier id1 = new Identifier();
        id1.setSystem("urn:ietf:rfc:3986");
        id1.setValue("urn:oid:1.1.8");

        Identifier id2 = new Identifier();
        id2.setSystem("http://sdhealthconnect.github.io/leap/samples/ids");
        id2.setValue("privacy-consent-scenario-H-healthcurrent");
        ids.add(id1);
        ids.add(id2);

        org.setIdentifier(ids);

        return org;
    }

    public static Practitioner getPrimaryPractioner() {
        Practitioner practitioner = new Practitioner();
        practitioner.setId("privacy-consent-scenario-H-drwatkins");
        practitioner.setActive(true);

        List<Identifier> ids = new ArrayList<>();
        Identifier id1 = new Identifier();
        id1.setSystem("http://www.acme.org/practitioners");
        id1.setValue("6777");

        Identifier id2 = new Identifier();
        id2.setSystem("http://sdhealthconnect.github.io/leap/samples/ids");
        id2.setValue("privacy-consent-scenario-H-drwatkins");
        ids.add(id1);
        ids.add(id2);

        practitioner.setIdentifier(ids);

        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("Watkins");
        List<StringType> givenList = new ArrayList<>();
        StringType sType = new StringType();
        sType.setValueAsString("Bob");
        givenList.add(sType);
        name.setGiven(givenList);
        List<HumanName> nameList = new ArrayList<>();
        nameList.add(name);
        practitioner.setName(nameList);

        return practitioner;
    }

    public static ConsentSession getConsentSession() {
        ConsentSession sessionInfo = new ConsentSession();
        sessionInfo.setFhirCustodian(getPrimaryOrganization());
        sessionInfo.setFhirPatient(getPatient());
        sessionInfo.setLanguagePreference(getLanguagePreference());
        sessionInfo.setUserId(getUserId());
        sessionInfo.setUsername(getUserName());
        sessionInfo.setPrimaryState("Arizona");
        sessionInfo.setPrimaryPhysician(getPrimaryPractioner());
        sessionInfo.setConsentUser(getConsentUser());
        return sessionInfo;
    }

    private static ConsentUser getConsentUser() {
        ConsentUser cUser = new ConsentUser();
        cUser.setCity("Tumacacori");
        Date bdate = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            bdate = sdf.parse("15/11/1959");
        }
        catch (Exception ex) {
            //blah blah
        }
        cUser.setDateOfBirth(bdate);
        cUser.setEmailAddress("duane.decouteau@gmail.com");
        cUser.setStreetAddress1("1670 W Frontage Rd");
        cUser.setStreetAddress2("");
        cUser.setEmergencyContact("Lori Hall");
        cUser.setGetEmergencyContactPhone("425-555-5555");
        cUser.setRelationship("Partner");
        cUser.setEthnicity("White");
        cUser.setEyeColor("BLU");
        cUser.setHairColor("BRN");
        cUser.setState("Arizona");
        cUser.setZipCode("85640");
        cUser.setFirstName("Duane");
        cUser.setLastName("DeCouteau");
        cUser.setMiddleName("Andrew");
        cUser.setMaritalStatus("Single");
        cUser.setGender("M");
        cUser.setHeight("6-3");
        cUser.setWeight("195");
        cUser.setPrimaryPhysician("Dr. Bob Smith");
        cUser.setPrimaryPhysicianPhoneNumber("520-555-5555");
        cUser.setPhone("406-555-5555");
        cUser.setMobile("406-555-5555");

        return cUser;
    }


}
