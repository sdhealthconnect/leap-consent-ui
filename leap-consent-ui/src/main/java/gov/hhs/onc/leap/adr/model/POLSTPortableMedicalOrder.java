package gov.hhs.onc.leap.adr.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class POLSTPortableMedicalOrder {
    //demographics
    private String patientFirstName;
    private String patientMiddleName;
    private String patientLastName;
    private String patientPreferredName;
    private String patientSuffix;

    private String patientDateOfBirth; //in form of MM/dd/yyyy
    private boolean genderM = false;
    private boolean genderF = false;
    private boolean genderX = false;
    private String patientHomeState;
    private String last4SSN;

    //CardioPulmonaryResuscitation
    private boolean yesCPR = false;
    private boolean noCPR = false;

    //Initial Treatment Orders
    private boolean fullTreatments = false;
    private boolean selectiveTreatments = false;
    private boolean comfortFocusedTreament = false;

    //Additional Orders
    private String additionalTreatments;

    //Nutrition
    private boolean nutritionByArtificialMeans = false;
    private boolean trialNutritionByArtificialMeans = false;
    private boolean noArtificialMeans = false;
    private boolean noNutritionDecisionMade = false;

    //patient or representative signature
    private byte[] base64EncodedSignature;
    private boolean representativeSigning = false;
    private String representativeName;
    private String representativeAuthority;

    //health care provider signature
    private byte[] base64EncodedSignatureHealthcareProvider;
    private String signatureDate; //in form of MM/dd/yyyy
    private String healthcareProviderPhoneNumber;
    private String healthcareProviderFullName;
    private String healthcareProviderLicenseOrCert;

    //supervising physician signature
    private boolean requiredSupervisingPhysicianSignature = false;
    private byte[] base64EncodedSupervisingPhysicianSignature;
    private String supervisingPhysicianLicense;

    //emergency contact information
    private String emergencyContactFullName;
    private boolean patientRepresentative = false;
    private boolean otherEmergencyType = false;
    private String emergencyContactPhoneNumberDay;
    private String emergencyContactPhoneNumberNight;

    //primary provider
    private String primaryPhysicianFullName;
    private String primaryPhysicianPhoneNumber;

    //hospice
    private boolean inHospice = false;
    private String hospiceAgencyName;
    private String hospiceAgencyPhoneNumber;

    //Advance Directive Reviewed
    private boolean advancedDirectiveReviewed = false;
    private String dateAdvancedDirectiveReviewed; //in form of MM/dd/yyyy
    private boolean advanceDirectiveConflictExists = false;
    private boolean advanceDirectiveNotAvailable = false;
    private boolean noAdvanceDirectiveExists = false;

    //who participated in discussion
    private boolean patientWithDecisionMakingCapacity = false;
    private boolean legalSurrogateOrHealthcareAgent = false;
    private boolean courtAppointedGuardian = false;
    private boolean parentOfMinor = false;
    private boolean otherParticipants = false;
    private String otherParticipantsList;

    //professional health care provider assisting
    private String assistingHealthcareProviderFullName;
    private String dateAssistedByHealthcareProvider;
    private String assistingHealthcareProviderPhoneNumber;

    //assisting type related to patient
    private boolean socialWorker = false;
    private boolean nurse = false;
    private boolean clergy = false;
    private boolean assistingOther = false;
    private String assistingOtherList;

}
