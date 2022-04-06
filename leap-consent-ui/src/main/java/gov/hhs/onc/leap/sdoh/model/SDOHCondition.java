package gov.hhs.onc.leap.sdoh.model;

import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRCondition;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class SDOHCondition {
    //based on FHIR SDOH-Clinical Care

    private Condition baseCondition;

    public SDOHCondition() {

    }

    private void createBaseCondition() {
        baseCondition = new Condition();
        Meta meta = new Meta();
        CanonicalType canonicalType = new CanonicalType();
        canonicalType.setValue("http://hl7.org/fhir/us/sdoh-clinicalcare/StructureDefinition/SDOHCC-Condition");
        meta.getProfile().add(canonicalType);
        baseCondition.setMeta(meta);

        baseCondition.setRecordedDate(new Date());

        //set clinical status
        Coding clinStatusCoding = new Coding();
        clinStatusCoding.setCode("active");
        clinStatusCoding.setDisplay("Active");
        clinStatusCoding.setSystem("http://terminology.hl7.org/CodeSystem/condition-clinical");

        CodeableConcept clinStatusCodeableConcept = new CodeableConcept();
        clinStatusCodeableConcept.addCoding(clinStatusCoding);
        baseCondition.setClinicalStatus(clinStatusCodeableConcept);

        //set verification status to unconfirmed
        Coding verificationStatusCoding = new Coding();
        verificationStatusCoding.setSystem("http://terminology.hl7.org/CodeSystem/condition-ver-status");
        verificationStatusCoding.setCode("unconfirmed");
        verificationStatusCoding.setDisplay("Unconfirmed");

        CodeableConcept verificationCodeableConcept = new CodeableConcept();
        verificationCodeableConcept.addCoding(verificationStatusCoding);
        baseCondition.setVerificationStatus(verificationCodeableConcept);

    }

    private Coding getHealthConcernCategoryCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/us/core/CodeSystem/condition-category");
        coding.setCode("health-concern");
        coding.setDisplay("Health Concern");
        return coding;
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

    private Coding createFoodInsecuritySNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("733423003");
        coding.setDisplay("Food insecurity");
        return coding;
    }

    private Coding createHousingInstabilitySNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("1156191002");
        coding.setDisplay("Housing Instability");
        return coding;
    }

    private Coding createIntimatePartnerViolenceSNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("22791000175108");
        coding.setDisplay("At risk intimate partner violence");
        return coding;
    }

    private Coding createUtilityAccessSNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("1187272007");
        coding.setDisplay("Housing instability due to housing cost burden");
        return coding;
    }

    private Coding createTransportationAccessSNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("551731000124103");
        coding.setDisplay("Inability to access health care due to transportation insecurity");
        return coding;
    }

    private Coding createSocialSupportSNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("425022003");
        coding.setDisplay("Inadequate social support");
        return coding;
    }

    private Coding createEmploymentAndEducationSNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("410287004");
        coding.setDisplay("Employment education, guidance, and counseling");
        return coding;
    }

    private Coding createLegalSupportSNOMEDCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://snomed.info/sct");
        coding.setCode("22268004");
        coding.setDisplay("Legal problem");
        return coding;
    }

    private Coding createFoodInsecurityICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z59.4");
        coding.setDisplay("Lack of adequate food and safe drinking water");
        return coding;
    }

    private Coding createHousingInstabilityICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z59.811");
        coding.setDisplay("Housing instability, housed, with risk of homelessness");
        return coding;
    }

    private Coding createIntimatePartnerViolenceICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z69.11");
        coding.setDisplay("Spouse, or partner abuse");
        return coding;
    }

    private Coding createUtilityAccessICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z59.9");
        coding.setDisplay("Problem related to housing and economic circumstances, unspecified");
        return coding;
    }

    private Coding createTransportationAccessICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z59.8");
        coding.setDisplay("Other problems related to housing and economic circumstances");
        return coding;
    }

    private Coding createSocialSupportICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z63.8");
        coding.setDisplay("Other specified problems related to primary support group");
        return coding;
    }

    private Coding createEmploymentAndEducationICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z56.89");
        coding.setDisplay("Other problems related to employment");
        return coding;
    }

    private Coding createLegalSupportICDZCoding() {
        Coding coding = new Coding();
        coding.setSystem("http://hl7.org/fhir/sid/icd-10-cm");
        coding.setCode("Z65.3");
        coding.setDisplay("Problems related to other legal circumstances");
        return coding;
    }


    public Condition createFoodInsecurityCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHFoodInsecurity-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createFoodInsecurityCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createFoodInsecuritySNOMEDCoding());
        conditionCodeableConcept.addCoding(createFoodInsecurityICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createHousingInstabilityCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHHousingInstability-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createHousingInstabilityCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createHousingInstabilitySNOMEDCoding());
        conditionCodeableConcept.addCoding(createHousingInstabilityICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createPersonalSafetyCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHPersonalSafety-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createIntimatePartnerViolenceCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createIntimatePartnerViolenceSNOMEDCoding());
        conditionCodeableConcept.addCoding(createIntimatePartnerViolenceICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createUtilityAccessCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHUtilityAccess-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createUtilityAccessCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createUtilityAccessSNOMEDCoding());
        conditionCodeableConcept.addCoding(createUtilityAccessICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createTransportationAccessCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHTransportationAccess-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createTransportationAccessCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createTransportationAccessSNOMEDCoding());
        conditionCodeableConcept.addCoding(createTransportationAccessICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createSocialSupportCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHSocialSupport-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createSocialSupportCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createSocialSupportSNOMEDCoding());
        conditionCodeableConcept.addCoding(createSocialSupportICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createEmploymentAndEducationCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHEmploymentAndEducation-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createEmploymentAndEducationCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createEmploymentAndEducationSNOMEDCoding());
        conditionCodeableConcept.addCoding(createEmploymentAndEducationICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }

    public Condition createLegalSupportCondition(String fhirPatientId, String patientDisplayName, String questionnaireResponseRef) {
        createBaseCondition();
        baseCondition.setId("SDOHLegalSupport-"+fhirPatientId);
        CodeableConcept conditionCategory = new CodeableConcept();
        conditionCategory.addCoding(getHealthConcernCategoryCoding());
        conditionCategory.addCoding(createLegalSupportCategoryCoding());
        List<CodeableConcept> categoryList = new ArrayList<>();
        categoryList.add(conditionCategory);
        baseCondition.setCategory(categoryList);
        CodeableConcept conditionCodeableConcept = new CodeableConcept();
        conditionCodeableConcept.addCoding(createLegalSupportSNOMEDCoding());
        conditionCodeableConcept.addCoding(createLegalSupportICDZCoding());
        baseCondition.setCode(conditionCodeableConcept);
        Reference patientRef = new Reference();
        patientRef.setReference("Patient/"+fhirPatientId);
        patientRef.setDisplay(patientDisplayName);
        baseCondition.setSubject(patientRef);
        Period p = new Period();
        p.setStart(new Date());
        baseCondition.setOnset(p);
        Reference evidence = new Reference();
        evidence.setReference(questionnaireResponseRef);
        List<Condition.ConditionEvidenceComponent> evidenceComponentList = new ArrayList<>();
        Condition.ConditionEvidenceComponent evidenceComponent = new Condition.ConditionEvidenceComponent();
        evidenceComponent.getDetail().add(evidence);
        evidenceComponentList.add(evidenceComponent);
        baseCondition.setEvidence(evidenceComponentList);

        return baseCondition;
    }
}
