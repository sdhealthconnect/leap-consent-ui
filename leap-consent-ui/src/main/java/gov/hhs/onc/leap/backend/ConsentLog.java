package gov.hhs.onc.leap.backend;

import org.hl7.fhir.r4.model.AuditEvent;

import java.time.LocalDateTime;


public class ConsentLog {
    private String decision;
    private LocalDateTime decisionDate;
    private String requestor;
    private String purposeOfUse;
    private String action;
    private AuditEvent auditEvent;

    public ConsentLog(String decision, LocalDateTime decisionDate, String requestor, String purposeOfUse, String action, AuditEvent auditEvent) {
        this.decision = decision;
        this.decisionDate = decisionDate;
        this.requestor = requestor;
        this.purposeOfUse = purposeOfUse;
        this.action = action;
        this.auditEvent = auditEvent;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public LocalDateTime getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDateTime decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }


    public AuditEvent getAuditEvent() { return auditEvent; }

    public void setAuditEvent(AuditEvent auditEvent) { this.auditEvent = auditEvent; }

    public String getPurposeOfUse() { return purposeOfUse; }

    public void setPurposeOfUse(String purposeOfUse) { this.purposeOfUse = purposeOfUse; }

    public String getAction() { return action; }

    public void setAction(String action) { this.action = action; }
}
