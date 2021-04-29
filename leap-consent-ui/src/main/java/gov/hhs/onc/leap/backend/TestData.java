package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class TestData {
    private static final Random random = new Random(1);

    private static final Map<Long, ConsentDocument> CONSENT = new HashMap<>();

    private static final Map<Long, ConsentLog> CONSENT_LOG_MAP = new HashMap<>();

    //Injected in setter
    private static String fhirBase;

    public static Collection<ConsentDocument> getConsents() {
        return CONSENT.values();
    }

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
        ConsentSession sessionInfo = new ConsentSession(fhirBase);
        sessionInfo.setFhirCustodian(getPrimaryOrganization());
        // sessionInfo.setFhirPatient(getPatient()); Set in Fhir Decorator
        // sessionInfo.setLanguagePreference(getLanguagePreference()); // Set in DB Decorator
        //sessionInfo.setUserId(getUserId()); Set in DB Decorator
        //sessionInfo.setUsername(getUserName()); Set in DB Decorator
        //sessionInfo.setPrimaryState("Arizona"); Set in DB Decorator
        sessionInfo.setPrimaryPhysician(getPrimaryPractioner());
        //sessionInfo.setConsentUser(getConsentUser()); Set in DB Decorator
        return sessionInfo;
    }

    @Value("${hapi-fhir.url:http://34.94.253.50:8080/hapi-fhir-jpaserver/fhir/}")
    public void setFhirBase(String fhirBase) {
        TestData.fhirBase = fhirBase;
    }


}
