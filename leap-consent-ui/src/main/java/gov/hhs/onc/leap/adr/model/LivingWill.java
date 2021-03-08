package gov.hhs.onc.leap.adr.model;

public class LivingWill {

    private Principle principle;

    // if terminal in coma or vegetative state
    private boolean comfortCareOnly = false;
    private boolean comfortCareOnlyButNot = false;
    private boolean noCardioPulmonaryRecusitation = false;
    private boolean noArtificalFluidsFoods = false;
    private boolean avoidTakingToHospital = false;

    private boolean pregnantSaveFetus = false;
    private boolean careUntilDoctorsConcludeNoHope = false;
    private boolean prolongLifeToGreatestExtentPossible = false;

    private boolean additionalInstructions = false;
    private boolean noAdditionalInstructions = false;

    private PrincipleSignature principleSignature;
    private PrincipleAlternateSignature principleAlternateSignature;
    private WitnessSignature witnessSignature;

    public Principle getPrinciple() {
        return principle;
    }

    public void setPrinciple(Principle principle) {
        this.principle = principle;
    }

    public boolean isComfortCareOnly() {
        return comfortCareOnly;
    }

    public void setComfortCareOnly(boolean comfortCareOnly) {
        this.comfortCareOnly = comfortCareOnly;
    }

    public boolean isComfortCareOnlyButNot() {
        return comfortCareOnlyButNot;
    }

    public void setComfortCareOnlyButNot(boolean comfortCareOnlyButNot) {
        this.comfortCareOnlyButNot = comfortCareOnlyButNot;
    }

    public boolean isNoCardioPulmonaryRecusitation() {
        return noCardioPulmonaryRecusitation;
    }

    public void setNoCardioPulmonaryRecusitation(boolean noCardioPulmonaryRecusitation) {
        this.noCardioPulmonaryRecusitation = noCardioPulmonaryRecusitation;
    }

    public boolean isNoArtificalFluidsFoods() {
        return noArtificalFluidsFoods;
    }

    public void setNoArtificalFluidsFoods(boolean noArtificalFluidsFoods) {
        this.noArtificalFluidsFoods = noArtificalFluidsFoods;
    }

    public boolean isAvoidTakingToHospital() {
        return avoidTakingToHospital;
    }

    public void setAvoidTakingToHospital(boolean avoidTakingToHospital) {
        this.avoidTakingToHospital = avoidTakingToHospital;
    }

    public boolean isPregnantSaveFetus() {
        return pregnantSaveFetus;
    }

    public void setPregnantSaveFetus(boolean pregnantSaveFetus) {
        this.pregnantSaveFetus = pregnantSaveFetus;
    }

    public boolean isCareUntilDoctorsConcludeNoHope() {
        return careUntilDoctorsConcludeNoHope;
    }

    public void setCareUntilDoctorsConcludeNoHope(boolean careUntilDoctorsConcludeNoHope) {
        this.careUntilDoctorsConcludeNoHope = careUntilDoctorsConcludeNoHope;
    }

    public boolean isProlongLifeToGreatestExtentPossible() {
        return prolongLifeToGreatestExtentPossible;
    }

    public void setProlongLifeToGreatestExtentPossible(boolean prolongLifeToGreatestExtentPossible) {
        this.prolongLifeToGreatestExtentPossible = prolongLifeToGreatestExtentPossible;
    }

    public boolean isAdditionalInstructions() {
        return additionalInstructions;
    }

    public void setAdditionalInstructions(boolean additionalInstructions) {
        this.additionalInstructions = additionalInstructions;
    }

    public boolean isNoAdditionalInstructions() {
        return noAdditionalInstructions;
    }

    public void setNoAdditionalInstructions(boolean noAdditionalInstructions) {
        this.noAdditionalInstructions = noAdditionalInstructions;
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
