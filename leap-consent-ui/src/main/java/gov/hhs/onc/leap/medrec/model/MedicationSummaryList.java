package gov.hhs.onc.leap.medrec.model;

import java.util.List;

public class MedicationSummaryList {
    private List<MedicationSummary> activeMedications;
    private List<MedicationSummary> inactiveMedications;

    public List<MedicationSummary> getActiveMedications() {
        return activeMedications;
    }

    public void setActiveMedications(List<MedicationSummary> activeMedications) {
        this.activeMedications = activeMedications;
    }

    public List<MedicationSummary> getInactiveMedications() {
        return inactiveMedications;
    }

    public void setInactiveMedications(List<MedicationSummary> inactiveMedications) {
        this.inactiveMedications = inactiveMedications;
    }
}
