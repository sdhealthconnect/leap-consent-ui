package gov.hhs.onc.leap.adr.model;

public class PowerOfAttorneyMentalHealth {
    private Principle principle;
    private Agent agent;
    private Alternate alternate;

    private boolean authorizeReleaseOfRecords = false;
    private boolean authorizeMedicationAdminstration = false;
    private boolean authorizeCommitIfNecessary = false;
    private boolean authorizeOtherMentalHealthActions = false;

    private String mentalHealthActionsList1;
    private String mentalHealthActionsList2;
    private String mentalHealthActionsList3;

    private String doNotAuthorizeActionList1;
    private String doNotAuthorizeActionList2;

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

    public boolean isAuthorizeReleaseOfRecords() {
        return authorizeReleaseOfRecords;
    }

    public void setAuthorizeReleaseOfRecords(boolean authorizeReleaseOfRecords) {
        this.authorizeReleaseOfRecords = authorizeReleaseOfRecords;
    }

    public boolean isAuthorizeMedicationAdminstration() {
        return authorizeMedicationAdminstration;
    }

    public void setAuthorizeMedicationAdminstration(boolean authorizeMedicationAdminstration) {
        this.authorizeMedicationAdminstration = authorizeMedicationAdminstration;
    }

    public boolean isAuthorizeCommitIfNecessary() {
        return authorizeCommitIfNecessary;
    }

    public void setAuthorizeCommitIfNecessary(boolean authorizeCommitIfNecessary) {
        this.authorizeCommitIfNecessary = authorizeCommitIfNecessary;
    }

    public boolean isAuthorizeOtherMentalHealthActions() {
        return authorizeOtherMentalHealthActions;
    }

    public void setAuthorizeOtherMentalHealthActions(boolean authorizeOtherMentalHealthActions) {
        this.authorizeOtherMentalHealthActions = authorizeOtherMentalHealthActions;
    }

    public String getMentalHealthActionsList1() {
        return mentalHealthActionsList1;
    }

    public void setMentalHealthActionsList1(String mentalHealthActionsList1) {
        this.mentalHealthActionsList1 = mentalHealthActionsList1;
    }

    public String getMentalHealthActionsList2() {
        return mentalHealthActionsList2;
    }

    public void setMentalHealthActionsList2(String mentalHealthActionsList2) {
        this.mentalHealthActionsList2 = mentalHealthActionsList2;
    }

    public String getMentalHealthActionsList3() {
        return mentalHealthActionsList3;
    }

    public void setMentalHealthActionsList3(String mentalHealthActionsList3) {
        this.mentalHealthActionsList3 = mentalHealthActionsList3;
    }

    public String getDoNotAuthorizeActionList1() {
        return doNotAuthorizeActionList1;
    }

    public void setDoNotAuthorizeActionList1(String doNotAuthorizeActionList1) {
        this.doNotAuthorizeActionList1 = doNotAuthorizeActionList1;
    }

    public String getDoNotAuthorizeActionList2() {
        return doNotAuthorizeActionList2;
    }

    public void setDoNotAuthorizeActionList2(String doNotAuthorizeActionList2) {
        this.doNotAuthorizeActionList2 = doNotAuthorizeActionList2;
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
