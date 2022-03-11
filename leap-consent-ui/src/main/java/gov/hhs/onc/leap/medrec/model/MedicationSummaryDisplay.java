package gov.hhs.onc.leap.medrec.model;

public class MedicationSummaryDisplay {
    private MedicationSummary medicationSummary;
    private boolean takingAsPrescribed;
    private boolean notTaking;
    private boolean takingButNotAsPrescribed;
    private String comments;

    public MedicationSummary getMedicationSummary() {
        return medicationSummary;
    }

    public void setMedicationSummary(MedicationSummary medicationSummary) {
        this.medicationSummary = medicationSummary;
    }

    public boolean isTakingAsPrescribed() {
        return takingAsPrescribed;
    }

    public void setTakingAsPrescribed(boolean takingAsPrescribed) {
        this.takingAsPrescribed = takingAsPrescribed;
    }

    public boolean isNotTaking() {
        return notTaking;
    }

    public void setNotTaking(boolean notTaking) {
        this.notTaking = notTaking;
    }

    public boolean isTakingButNotAsPrescribed() {
        return takingButNotAsPrescribed;
    }

    public void setTakingButNotAsPrescribed(boolean takingButNotAsPrescribed) {
        this.takingButNotAsPrescribed = takingButNotAsPrescribed;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
