package gov.hhs.onc.leap.adr.model;

public class PowerOfAttorneyHealthCare {
    private Principle principle;
    private Agent agent;
    private Alternate alternate;

    //Not authorized treatments
    private String doNotAuthorize1 = "None";
    private String doNotAuthorize2;
    private String doNotAuthorize3;

    //Autopsy
    private boolean denyAutopsy = false;
    private boolean permitAutopsy = false;
    private boolean agentDecidesAutopsy = false;

    //Organ Tissue Donation
    private boolean denyOrganTissueDonation = false;
    private boolean haveExistingOrganTissueCardOrAgreement = false;
    private String organTissueCardOrAgreementInstitution;
    private boolean permitOrganTissueDonation = false;

    //what parts or organs
    private boolean wholeBodyDonation = false;
    private boolean anyPartOrOrganNeeded = false;
    private boolean specificPartsOrOrgans = false;
    private String specificPartsOrOrgansList;

    //Organ Tissue Donation purposes
    private boolean anyLegalPurpose = false;
    private boolean transplantOrTherapeutic = false;
    private boolean researchOnly = false;
    private boolean otherPurposes = false;
    private String otherPurposesList;

    //Organ Tissue Donation Destination
    private boolean principleDefined = false;
    private String principleDefinedList;
    private boolean agentDecidedOrganTissueDestination = false;

    //Burial
    private boolean bodyToBeBuried = false;
    private boolean bodyToBeBuriedIn = false;
    private String bodyToBeBuriedInInstructions;
    private boolean bodyToBeCremated = false;
    private boolean bodyToBeCrematedAshesDisposition = false;
    private String bodyToBeCrematedAshesDispositionInstructions;
    private boolean agentDecidesBurial;

    //Living Will Determination
    private boolean signedLivingWill = false;
    private boolean notSignedLivingWill = false;

    //Portabe Medical Order
    private boolean signedPOLST = false;
    private boolean notSignedPOLST = false;

    //DNR
    private boolean signedDNR = false;
    private boolean notSignedDNR = false;

    private PhysicansAffidavit physiciansAffidavit;
    private HipaaWaiver hipaaWaiver;

    private PrincipleSignature principleSignature;

    private PrincipleAlternateSignature principleAlternateSignature;

    private WitnessSignature witnessSignature;


    public Principle getPrinciple() {
        return principle;
    }

    public void setPrinciple(Principle principle) {
        this.principle = principle;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public Alternate getAlternate() {
        return alternate;
    }

    public void setAlternate(Alternate alternate) {
        this.alternate = alternate;
    }

    public String getDoNotAuthorize1() {
        return doNotAuthorize1;
    }

    public void setDoNotAuthorize1(String doNotAuthorize1) {
        this.doNotAuthorize1 = doNotAuthorize1;
    }

    public String getDoNotAuthorize2() {
        return doNotAuthorize2;
    }

    public void setDoNotAuthorize2(String doNotAuthorize2) {
        this.doNotAuthorize2 = doNotAuthorize2;
    }

    public String getDoNotAuthorize3() {
        return doNotAuthorize3;
    }

    public void setDoNotAuthorize3(String doNotAuthorize3) {
        this.doNotAuthorize3 = doNotAuthorize3;
    }

    public boolean isDenyAutopsy() {
        return denyAutopsy;
    }

    public void setDenyAutopsy(boolean denyAutopsy) {
        this.denyAutopsy = denyAutopsy;
    }

    public boolean isPermitAutopsy() {
        return permitAutopsy;
    }

    public void setPermitAutopsy(boolean permitAutopsy) {
        this.permitAutopsy = permitAutopsy;
    }

    public boolean isAgentDecidesAutopsy() {
        return agentDecidesAutopsy;
    }

    public void setAgentDecidesAutopsy(boolean agentDecidesAutopsy) {
        this.agentDecidesAutopsy = agentDecidesAutopsy;
    }

    public boolean isDenyOrganTissueDonation() {
        return denyOrganTissueDonation;
    }

    public void setDenyOrganTissueDonation(boolean denyOrganTissueDonation) {
        this.denyOrganTissueDonation = denyOrganTissueDonation;
    }

    public boolean isHaveExistingOrganTissueCardOrAgreement() {
        return haveExistingOrganTissueCardOrAgreement;
    }

    public void setHaveExistingOrganTissueCardOrAgreement(boolean haveExistingOrganTissueCardOrAgreement) {
        this.haveExistingOrganTissueCardOrAgreement = haveExistingOrganTissueCardOrAgreement;
    }

    public String getOrganTissueCardOrAgreementInstitution() {
        return organTissueCardOrAgreementInstitution;
    }

    public void setOrganTissueCardOrAgreementInstitution(String organTissueCardOrAgreementInstitution) {
        this.organTissueCardOrAgreementInstitution = organTissueCardOrAgreementInstitution;
    }

    public boolean isPermitOrganTissueDonation() {
        return permitOrganTissueDonation;
    }

    public void setPermitOrganTissueDonation(boolean permitOrganTissueDonation) {
        this.permitOrganTissueDonation = permitOrganTissueDonation;
    }

    public boolean isWholeBodyDonation() {
        return wholeBodyDonation;
    }

    public void setWholeBodyDonation(boolean wholeBodyDonation) {
        this.wholeBodyDonation = wholeBodyDonation;
    }

    public boolean isAnyPartOrOrganNeeded() {
        return anyPartOrOrganNeeded;
    }

    public void setAnyPartOrOrganNeeded(boolean anyPartOrOrganNeeded) {
        this.anyPartOrOrganNeeded = anyPartOrOrganNeeded;
    }

    public boolean isSpecificPartsOrOrgans() {
        return specificPartsOrOrgans;
    }

    public void setSpecificPartsOrOrgans(boolean specificPartsOrOrgans) {
        this.specificPartsOrOrgans = specificPartsOrOrgans;
    }

    public String getSpecificPartsOrOrgansList() {
        return specificPartsOrOrgansList;
    }

    public void setSpecificPartsOrOrgansList(String specificPartsOrOrgansList) {
        this.specificPartsOrOrgansList = specificPartsOrOrgansList;
    }

    public boolean isAnyLegalPurpose() {
        return anyLegalPurpose;
    }

    public void setAnyLegalPurpose(boolean anyLegalPurpose) {
        this.anyLegalPurpose = anyLegalPurpose;
    }

    public boolean isTransplantOrTherapeutic() {
        return transplantOrTherapeutic;
    }

    public void setTransplantOrTherapeutic(boolean transplantOrTherapeutic) {
        this.transplantOrTherapeutic = transplantOrTherapeutic;
    }

    public boolean isResearchOnly() {
        return researchOnly;
    }

    public void setResearchOnly(boolean researchOnly) {
        this.researchOnly = researchOnly;
    }

    public boolean isOtherPurposes() {
        return otherPurposes;
    }

    public void setOtherPurposes(boolean otherPurposes) {
        this.otherPurposes = otherPurposes;
    }

    public String getOtherPurposesList() {
        return otherPurposesList;
    }

    public void setOtherPurposesList(String otherPurposesList) {
        this.otherPurposesList = otherPurposesList;
    }

    public boolean isPrincipleDefined() {
        return principleDefined;
    }

    public void setPrincipleDefined(boolean principleDefined) {
        this.principleDefined = principleDefined;
    }

    public String getPrincipleDefinedList() {
        return principleDefinedList;
    }

    public void setPrincipleDefinedList(String principleDefinedList) {
        this.principleDefinedList = principleDefinedList;
    }

    public boolean isAgentDecidedOrganTissueDestination() {
        return agentDecidedOrganTissueDestination;
    }

    public void setAgentDecidedOrganTissueDestination(boolean agentDecidedOrganTissueDestination) {
        this.agentDecidedOrganTissueDestination = agentDecidedOrganTissueDestination;
    }

    public boolean isBodyToBeBuried() {
        return bodyToBeBuried;
    }

    public void setBodyToBeBuried(boolean bodyToBeBuried) {
        this.bodyToBeBuried = bodyToBeBuried;
    }

    public boolean isBodyToBeBuriedIn() {
        return bodyToBeBuriedIn;
    }

    public void setBodyToBeBuriedIn(boolean bodyToBeBuriedIn) {
        this.bodyToBeBuriedIn = bodyToBeBuriedIn;
    }

    public String getBodyToBeBuriedInInstructions() {
        return bodyToBeBuriedInInstructions;
    }

    public void setBodyToBeBuriedInInstructions(String bodyToBeBuriedInInstructions) {
        this.bodyToBeBuriedInInstructions = bodyToBeBuriedInInstructions;
    }

    public boolean isBodyToBeCremated() {
        return bodyToBeCremated;
    }

    public void setBodyToBeCremated(boolean bodyToBeCremated) {
        this.bodyToBeCremated = bodyToBeCremated;
    }

    public boolean isBodyToBeCrematedAshesDisposition() {
        return bodyToBeCrematedAshesDisposition;
    }

    public void setBodyToBeCrematedAshesDisposition(boolean bodyToBeCrematedAshesDisposition) {
        this.bodyToBeCrematedAshesDisposition = bodyToBeCrematedAshesDisposition;
    }

    public String getBodyToBeCrematedAshesDispositionInstructions() {
        return bodyToBeCrematedAshesDispositionInstructions;
    }

    public void setBodyToBeCrematedAshesDispositionInstructions(String bodyToBeCrematedAshesDispositionInstructions) {
        this.bodyToBeCrematedAshesDispositionInstructions = bodyToBeCrematedAshesDispositionInstructions;
    }

    public boolean isAgentDecidesBurial() {
        return agentDecidesBurial;
    }

    public void setAgentDecidesBurial(boolean agentDecidesBurial) {
        this.agentDecidesBurial = agentDecidesBurial;
    }

    public boolean isSignedLivingWill() {
        return signedLivingWill;
    }

    public void setSignedLivingWill(boolean signedLivingWill) {
        this.signedLivingWill = signedLivingWill;
    }

    public boolean isNotSignedLivingWill() {
        return notSignedLivingWill;
    }

    public void setNotSignedLivingWill(boolean notSignedLivingWill) {
        this.notSignedLivingWill = notSignedLivingWill;
    }

    public boolean isSignedPOLST() {
        return signedPOLST;
    }

    public void setSignedPOLST(boolean signedPOLST) {
        this.signedPOLST = signedPOLST;
    }

    public boolean isNotSignedPOLST() {
        return notSignedPOLST;
    }

    public void setNotSignedPOLST(boolean notSignedPOLST) {
        this.notSignedPOLST = notSignedPOLST;
    }

    public boolean isSignedDNR() {
        return signedDNR;
    }

    public void setSignedDNR(boolean signedDNR) {
        this.signedDNR = signedDNR;
    }

    public boolean isNotSignedDNR() {
        return notSignedDNR;
    }

    public void setNotSignedDNR(boolean notSignedDNR) {
        this.notSignedDNR = notSignedDNR;
    }

    public PhysicansAffidavit getPhysiciansAffidavit() {
        return physiciansAffidavit;
    }

    public void setPhysiciansAffidavit(PhysicansAffidavit physiciansAffidavit) {
        this.physiciansAffidavit = physiciansAffidavit;
    }

    public HipaaWaiver getHipaaWaiver() {
        return hipaaWaiver;
    }

    public void setHipaaWaiver(HipaaWaiver hipaaWaiver) {
        this.hipaaWaiver = hipaaWaiver;
    }

    public PrincipleSignature getPrincipleSignature() {
        return principleSignature;
    }

    public void setPrincipleSignature(PrincipleSignature principleSignature) {
        this.principleSignature = principleSignature;
    }

    public PrincipleAlternateSignature getPrincipleAlternateSignature() {
        return principleAlternateSignature;
    }

    public void setPrincipleAlternateSignature(PrincipleAlternateSignature principleAlternateSignature) {
        this.principleAlternateSignature = principleAlternateSignature;
    }

    public WitnessSignature getWitnessSignature() {
        return witnessSignature;
    }

    public void setWitnessSignature(WitnessSignature witnessSignature) {
        this.witnessSignature = witnessSignature;
    }
}
