package gov.hhs.onc.leap.sdoh.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hl7.fhir.r4.model.HealthcareService;
import org.hl7.fhir.r4.model.Patient;

@Getter
@Setter
@NoArgsConstructor
public class SDOHConsent {

    private Patient patient;
    private boolean foodSecurity;
    private HealthcareService selectedFoodSecurityService;
    private boolean housingInsecurty;
    private HealthcareService selectedHousingInsecurityService;
    private boolean utilityNeeds;
    private HealthcareService selectedUtilityNeedsService;
    private boolean transportationAccess;
    private HealthcareService selectedTransportationAccessService;
    private boolean personalSafety;
    private HealthcareService selectedPersonalSafetyService;
    private boolean socialSupport;
    private HealthcareService selectedSocialSupportService;
    private boolean employmentEducation;
    private HealthcareService selectedEmploymentEducationService;
    private boolean legalSupport;
    private HealthcareService selectLegalSupportService;
}
