package gov.hhs.onc.leap.medrec.model;

public class MedicationSummary {
    private String type;
    private String fhirId;
    private String status;
    private String medication;
    private String dosages;
    private String requestedBy;
    private String reasons;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFhirId() {
        return fhirId;
    }

    public void setFhirId(String fhirId) {
        this.fhirId = fhirId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMedication() {
        return medication;
    }

    public void setMedication(String medication) {
        this.medication = medication;
    }

    public String getDosages() {
        return dosages;
    }

    public void setDosages(String dosages) {
        this.dosages = dosages;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public String getReasons() {
        return reasons;
    }

    public void setReasons(String reasons) {
        this.reasons = reasons;
    }
}
