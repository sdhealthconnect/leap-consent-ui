package gov.hhs.onc.leap.adr.model;

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


    public String getPatientFirstName() {
        return patientFirstName;
    }

    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    public String getPatientMiddleName() {
        return patientMiddleName;
    }

    public void setPatientMiddleName(String patientMiddleName) {
        this.patientMiddleName = patientMiddleName;
    }

    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    public String getPatientPreferredName() {
        return patientPreferredName;
    }

    public void setPatientPreferredName(String patientPreferredName) {
        this.patientPreferredName = patientPreferredName;
    }

    public String getPatientDateOfBirth() {
        return patientDateOfBirth;
    }

    public void setPatientDateOfBirth(String patientDateOfBirth) {
        this.patientDateOfBirth = patientDateOfBirth;
    }

    public String getPatientHomeState() {
        return patientHomeState;
    }

    public void setPatientHomeState(String patientHomeState) {
        this.patientHomeState = patientHomeState;
    }

    public String getLast4SSN() {
        return last4SSN;
    }

    public void setLast4SSN(String last4SSN) {
        this.last4SSN = last4SSN;
    }

    public boolean isYesCPR() {
        return yesCPR;
    }

    public void setYesCPR(boolean yesCPR) {
        this.yesCPR = yesCPR;
    }

    public boolean isNoCPR() {
        return noCPR;
    }

    public void setNoCPR(boolean noCPR) {
        this.noCPR = noCPR;
    }

    public boolean isFullTreatments() {
        return fullTreatments;
    }

    public void setFullTreatments(boolean fullTreatments) {
        this.fullTreatments = fullTreatments;
    }

    public boolean isSelectiveTreatments() {
        return selectiveTreatments;
    }

    public void setSelectiveTreatments(boolean selectiveTreatments) {
        this.selectiveTreatments = selectiveTreatments;
    }

    public boolean isComfortFocusedTreament() {
        return comfortFocusedTreament;
    }

    public void setComfortFocusedTreament(boolean comfortFocusedTreament) {
        this.comfortFocusedTreament = comfortFocusedTreament;
    }

    public String getAdditionalTreatments() {
        return additionalTreatments;
    }

    public void setAdditionalTreatments(String additionalTreatments) {
        this.additionalTreatments = additionalTreatments;
    }

    public boolean isNutritionByArtificialMeans() {
        return nutritionByArtificialMeans;
    }

    public void setNutritionByArtificialMeans(boolean nutritionByArtificialMeans) {
        this.nutritionByArtificialMeans = nutritionByArtificialMeans;
    }

    public boolean isTrialNutritionByArtificialMeans() {
        return trialNutritionByArtificialMeans;
    }

    public void setTrialNutritionByArtificialMeans(boolean trialNutritionByArtificialMeans) {
        this.trialNutritionByArtificialMeans = trialNutritionByArtificialMeans;
    }

    public boolean isNoArtificialMeans() {
        return noArtificialMeans;
    }

    public void setNoArtificialMeans(boolean noArtificialMeans) {
        this.noArtificialMeans = noArtificialMeans;
    }

    public boolean isNoNutritionDecisionMade() {
        return noNutritionDecisionMade;
    }

    public void setNoNutritionDecisionMade(boolean noNutritionDecisionMade) {
        this.noNutritionDecisionMade = noNutritionDecisionMade;
    }

    public byte[] getBase64EncodedSignature() {
        return base64EncodedSignature;
    }

    public void setBase64EncodedSignature(byte[] base64EncodedSignature) {
        this.base64EncodedSignature = base64EncodedSignature;
    }

    public boolean isRepresentativeSigning() {
        return representativeSigning;
    }

    public void setRepresentativeSigning(boolean representativeSigning) {
        this.representativeSigning = representativeSigning;
    }

    public String getRepresentativeName() {
        return representativeName;
    }

    public void setRepresentativeName(String representativeName) {
        this.representativeName = representativeName;
    }

    public String getRepresentativeAuthority() {
        return representativeAuthority;
    }

    public void setRepresentativeAuthority(String representativeAuthority) {
        this.representativeAuthority = representativeAuthority;
    }

    public byte[] getBase64EncodedSignatureHealthcareProvider() {
        return base64EncodedSignatureHealthcareProvider;
    }

    public void setBase64EncodedSignatureHealthcareProvider(byte[] base64EncodedSignatureHealthcareProvider) {
        this.base64EncodedSignatureHealthcareProvider = base64EncodedSignatureHealthcareProvider;
    }

    public String getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(String signatureDate) {
        this.signatureDate = signatureDate;
    }

    public String getHealthcareProviderPhoneNumber() {
        return healthcareProviderPhoneNumber;
    }

    public void setHealthcareProviderPhoneNumber(String healthcareProviderPhoneNumber) {
        this.healthcareProviderPhoneNumber = healthcareProviderPhoneNumber;
    }

    public String getHealthcareProviderFullName() {
        return healthcareProviderFullName;
    }

    public void setHealthcareProviderFullName(String healthcareProviderFullName) {
        this.healthcareProviderFullName = healthcareProviderFullName;
    }

    public String getHealthcareProviderLicenseOrCert() {
        return healthcareProviderLicenseOrCert;
    }

    public void setHealthcareProviderLicenseOrCert(String healthcareProviderLicenseOrCert) {
        this.healthcareProviderLicenseOrCert = healthcareProviderLicenseOrCert;
    }

    public boolean isRequiredSupervisingPhysicianSignature() {
        return requiredSupervisingPhysicianSignature;
    }

    public void setRequiredSupervisingPhysicianSignature(boolean requiredSupervisingPhysicianSignature) {
        this.requiredSupervisingPhysicianSignature = requiredSupervisingPhysicianSignature;
    }

    public byte[] getBase64EncodedSupervisingPhysicianSignature() {
        return base64EncodedSupervisingPhysicianSignature;
    }

    public void setBase64EncodedSupervisingPhysicianSignature(byte[] base64EncodedSupervisingPhysicianSignature) {
        this.base64EncodedSupervisingPhysicianSignature = base64EncodedSupervisingPhysicianSignature;
    }

    public String getSupervisingPhysicianLicense() {
        return supervisingPhysicianLicense;
    }

    public void setSupervisingPhysicianLicense(String supervisingPhysicianLicense) {
        this.supervisingPhysicianLicense = supervisingPhysicianLicense;
    }

    public String getEmergencyContactFullName() {
        return emergencyContactFullName;
    }

    public void setEmergencyContactFullName(String emergencyContactFullName) {
        this.emergencyContactFullName = emergencyContactFullName;
    }

    public boolean isPatientRepresentative() {
        return patientRepresentative;
    }

    public void setPatientRepresentative(boolean patientRepresentative) {
        this.patientRepresentative = patientRepresentative;
    }

    public boolean isOtherEmergencyType() {
        return otherEmergencyType;
    }

    public void setOtherEmergencyType(boolean otherEmergencyType) {
        this.otherEmergencyType = otherEmergencyType;
    }

    public String getEmergencyContactPhoneNumberDay() {
        return emergencyContactPhoneNumberDay;
    }

    public void setEmergencyContactPhoneNumberDay(String emergencyContactPhoneNumberDay) {
        this.emergencyContactPhoneNumberDay = emergencyContactPhoneNumberDay;
    }

    public String getEmergencyContactPhoneNumberNight() {
        return emergencyContactPhoneNumberNight;
    }

    public void setEmergencyContactPhoneNumberNight(String emergencyContactPhoneNumberNight) {
        this.emergencyContactPhoneNumberNight = emergencyContactPhoneNumberNight;
    }

    public String getPrimaryPhysicianFullName() {
        return primaryPhysicianFullName;
    }

    public void setPrimaryPhysicianFullName(String primaryPhysicianFullName) {
        this.primaryPhysicianFullName = primaryPhysicianFullName;
    }

    public String getPrimaryPhysicianPhoneNumber() {
        return primaryPhysicianPhoneNumber;
    }

    public void setPrimaryPhysicianPhoneNumber(String primaryPhysicianPhoneNumber) {
        this.primaryPhysicianPhoneNumber = primaryPhysicianPhoneNumber;
    }

    public boolean isInHospice() {
        return inHospice;
    }

    public void setInHospice(boolean inHospice) {
        this.inHospice = inHospice;
    }

    public String getHospiceAgencyName() {
        return hospiceAgencyName;
    }

    public void setHospiceAgencyName(String hospiceAgencyName) {
        this.hospiceAgencyName = hospiceAgencyName;
    }

    public String getHospiceAgencyPhoneNumber() {
        return hospiceAgencyPhoneNumber;
    }

    public void setHospiceAgencyPhoneNumber(String hospiceAgencyPhoneNumber) {
        this.hospiceAgencyPhoneNumber = hospiceAgencyPhoneNumber;
    }

    public boolean isAdvancedDirectiveReviewed() {
        return advancedDirectiveReviewed;
    }

    public void setAdvancedDirectiveReviewed(boolean advancedDirectiveReviewed) {
        this.advancedDirectiveReviewed = advancedDirectiveReviewed;
    }

    public String getDateAdvancedDirectiveReviewed() {
        return dateAdvancedDirectiveReviewed;
    }

    public void setDateAdvancedDirectiveReviewed(String dateAdvancedDirectiveReviewed) {
        this.dateAdvancedDirectiveReviewed = dateAdvancedDirectiveReviewed;
    }

    public boolean isAdvanceDirectiveConflictExists() {
        return advanceDirectiveConflictExists;
    }

    public void setAdvanceDirectiveConflictExists(boolean advanceDirectiveConflictExists) {
        this.advanceDirectiveConflictExists = advanceDirectiveConflictExists;
    }

    public boolean isAdvanceDirectiveNotAvailable() {
        return advanceDirectiveNotAvailable;
    }

    public void setAdvanceDirectiveNotAvailable(boolean advanceDirectiveNotAvailable) {
        this.advanceDirectiveNotAvailable = advanceDirectiveNotAvailable;
    }

    public boolean isNoAdvanceDirectiveExists() {
        return noAdvanceDirectiveExists;
    }

    public void setNoAdvanceDirectiveExists(boolean noAdvanceDirectiveExists) {
        this.noAdvanceDirectiveExists = noAdvanceDirectiveExists;
    }

    public boolean isPatientWithDecisionMakingCapacity() {
        return patientWithDecisionMakingCapacity;
    }

    public void setPatientWithDecisionMakingCapacity(boolean patientWithDecisionMakingCapacity) {
        this.patientWithDecisionMakingCapacity = patientWithDecisionMakingCapacity;
    }

    public boolean isLegalSurrogateOrHealthcareAgent() {
        return legalSurrogateOrHealthcareAgent;
    }

    public void setLegalSurrogateOrHealthcareAgent(boolean legalSurrogateOrHealthcareAgent) {
        this.legalSurrogateOrHealthcareAgent = legalSurrogateOrHealthcareAgent;
    }

    public boolean isCourtAppointedGuardian() {
        return courtAppointedGuardian;
    }

    public void setCourtAppointedGuardian(boolean courtAppointedGuardian) {
        this.courtAppointedGuardian = courtAppointedGuardian;
    }

    public boolean isParentOfMinor() {
        return parentOfMinor;
    }

    public void setParentOfMinor(boolean parentOfMinor) {
        this.parentOfMinor = parentOfMinor;
    }

    public boolean isOtherParticipants() {
        return otherParticipants;
    }

    public void setOtherParticipants(boolean otherParticipants) {
        this.otherParticipants = otherParticipants;
    }

    public String getOtherParticipantsList() {
        return otherParticipantsList;
    }

    public void setOtherParticipantsList(String otherParticipantsList) {
        this.otherParticipantsList = otherParticipantsList;
    }

    public String getAssistingHealthcareProviderFullName() {
        return assistingHealthcareProviderFullName;
    }

    public void setAssistingHealthcareProviderFullName(String assistingHealthcareProviderFullName) {
        this.assistingHealthcareProviderFullName = assistingHealthcareProviderFullName;
    }

    public String getDateAssistedByHealthcareProvider() {
        return dateAssistedByHealthcareProvider;
    }

    public void setDateAssistedByHealthcareProvider(String dateAssistedByHealthcareProvider) {
        this.dateAssistedByHealthcareProvider = dateAssistedByHealthcareProvider;
    }

    public String getAssistingHealthcareProviderPhoneNumber() {
        return assistingHealthcareProviderPhoneNumber;
    }

    public void setAssistingHealthcareProviderPhoneNumber(String assistingHealthcareProviderPhoneNumber) {
        this.assistingHealthcareProviderPhoneNumber = assistingHealthcareProviderPhoneNumber;
    }

    public boolean isSocialWorker() {
        return socialWorker;
    }

    public void setSocialWorker(boolean socialWorker) {
        this.socialWorker = socialWorker;
    }

    public boolean isNurse() {
        return nurse;
    }

    public void setNurse(boolean nurse) {
        this.nurse = nurse;
    }

    public boolean isClergy() {
        return clergy;
    }

    public void setClergy(boolean clergy) {
        this.clergy = clergy;
    }

    public boolean isAssistingOther() {
        return assistingOther;
    }

    public void setAssistingOther(boolean assistingOther) {
        this.assistingOther = assistingOther;
    }

    public String getAssistingOtherList() {
        return assistingOtherList;
    }

    public void setAssistingOtherList(String assistingOtherList) {
        this.assistingOtherList = assistingOtherList;
    }

    public boolean isGenderM() {
        return genderM;
    }

    public void setGenderM(boolean genderM) {
        this.genderM = genderM;
    }

    public boolean isGenderF() {
        return genderF;
    }

    public void setGenderF(boolean genderF) {
        this.genderF = genderF;
    }

    public boolean isGenderX() {
        return genderX;
    }

    public void setGenderX(boolean genderX) {
        this.genderX = genderX;
    }

    public String getPatientSuffix() {
        return patientSuffix;
    }

    public void setPatientSuffix(String patientSuffix) {
        this.patientSuffix = patientSuffix;
    }
}
