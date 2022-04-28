package gov.hhs.onc.leap.sdoh.model;

import gov.hhs.onc.leap.backend.model.SDOHOrganization;
import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SDOHReferral {

    private ServiceRequest baseServiceRequest;

    public SDOHReferral() {

    }

    private void createBaseServiceRequest() {
        baseServiceRequest = new ServiceRequest();
        Meta meta = new Meta();
        CanonicalType canonicalType = new CanonicalType();
        canonicalType.setValue("http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-ServiceRequest");
        meta.getProfile().add(canonicalType);
        Coding security = new Coding();
        security.setDisplay("Restricted");
        security.setCode("R");
        security.setSystem("http://terminology.hl7.org/CodeSystem/v3-Confidentiality");
        List<Coding> securityList = new ArrayList<>();
        securityList.add(security);
        meta.setSecurity(securityList);
        baseServiceRequest.setMeta(meta);

        baseServiceRequest.setAuthoredOn(new Date());

        baseServiceRequest.setStatus(ServiceRequest.ServiceRequestStatus.ACTIVE);

        baseServiceRequest.setIntent(ServiceRequest.ServiceRequestIntent.PROPOSAL);

        baseServiceRequest.setPriority(ServiceRequest.ServiceRequestPriority.ASAP);

    }

    public ServiceRequest createHousingInstabilityReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHHousingInstability-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createHousingInstabilityCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code

        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);


        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHHousingInstability-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);

        return baseServiceRequest;
    }

    public ServiceRequest createFoodInsecurityReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHFoodInsecurity-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createFoodInsecurityCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code

        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);


        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHFoodInsecurity-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);
        return baseServiceRequest;
    }

    public ServiceRequest createPersonalSafetyReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHPersonalSafety-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createIntimatePartnerViolenceCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code

        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);


        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHPersonalSafety-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);
        return baseServiceRequest;
    }

    public ServiceRequest createUtilityAccessReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHUtilityAccess-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createUtilityAccessCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code

        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);


        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHUtilityAccess-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);
        return baseServiceRequest;
    }

    public ServiceRequest createTransportationAccessReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHTransportationAccess-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createTransportationAccessCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);

        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHTransportationAccess-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);

        return baseServiceRequest;
    }

    public ServiceRequest createSocialSupportReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHSocialSupport-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createSocialSupportCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);

        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHSocialSupport-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);
        return baseServiceRequest;
    }

    public ServiceRequest createEmploymentAndEducationReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHEmploymentAndEducation-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createEmploymentAndEducationCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);

        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHEmploymentAndEducation-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);
        return baseServiceRequest;
    }

    public ServiceRequest createLegalSupportReferralRequest(String fhirPatientId, String patientName, String questionnaireRef, SDOHOrganization org) {
        createBaseServiceRequest();
        baseServiceRequest.setId("SDOHLegalSupport-Referral-"+fhirPatientId);

        //category
        List<CodeableConcept> ccList = new ArrayList<>();
        CodeableConcept cc = new CodeableConcept();
        List<Coding> catList = new ArrayList<>();
        Coding coding = createLegalSupportCategoryCoding();
        catList.add(coding);
        cc.setCoding(catList);
        ccList.add(cc);
        baseServiceRequest.setCategory(ccList);

        //code
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding referralCode = new Coding();
        referralCode.setSystem("http://snomed.info/sct");
        referralCode.setCode("306206005");
        referralCode.setDisplay("Referral to service");
        codeableConcept.addCoding(referralCode);
        baseServiceRequest.setCode(codeableConcept);

        //Authored by
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientName);

        baseServiceRequest.setRequester(patientRef);
        baseServiceRequest.setSubject(patientRef);

        //Reason of Condition Patient Report
        List<Reference> reasonList = new ArrayList<>();
        Reference reason = new Reference();
        reason.setReference("Condition/SDOHLegalSupport-"+fhirPatientId);
        reasonList.add(reason);
        baseServiceRequest.setReasonReference(reasonList);

        //set who performs
        List<Reference> orgRefList = new ArrayList<>();
        Reference performer = new Reference();
        performer.setReference("Organization/"+org.getParentorganizationid());
        performer.setDisplay(org.getParentorganizationname());
        orgRefList.add(performer);
        baseServiceRequest.setPerformer(orgRefList);
        return baseServiceRequest;
    }

    private Coding createFoodInsecurityCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("food-insecurity");
        coding.setDisplay("Food Insecurity");
        return coding;
    }

    private Coding createHousingInstabilityCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("housing-instability");
        coding.setDisplay("Housing Instability");
        return coding;
    }

    private Coding createIntimatePartnerViolenceCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("personal-safety");
        coding.setDisplay("Personal Safety");
        return coding;
    }

    private Coding createUtilityAccessCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("utility-needs");
        coding.setDisplay("Utility Needs");
        return coding;
    }

    private Coding createTransportationAccessCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("transportation-access");
        coding.setDisplay("Transportation Access");
        return coding;
    }

    private Coding createSocialSupportCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("social-support");
        coding.setDisplay("Social Support");
        return coding;
    }

    private Coding createEmploymentAndEducationCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("employment-and-education");
        coding.setDisplay("Employment and Education");
        return coding;
    }

    private Coding createLegalSupportCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/sdoh-clinicalcare/CodeSystem/SDOHCC-CodeSystemTemporaryCodes");
        coding.setCode("legal-support");
        coding.setDisplay("Legal Support");
        return coding;
    }
}
