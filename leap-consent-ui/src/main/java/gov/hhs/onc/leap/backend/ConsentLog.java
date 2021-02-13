package gov.hhs.onc.leap.backend;

import java.time.LocalDate;


public class ConsentLog {
    private String decision;
    private LocalDate decisionDate;
    private String requestor;
    private String custodian;

    public ConsentLog(String decision, LocalDate decisionDate, String requestor, String custodian) {
        this.decision = decision;
        this.decisionDate = decisionDate;
        this.requestor = requestor;
        this.custodian = custodian;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(LocalDate decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getCustodian() {
        return custodian;
    }

    public void setCustodian(String custodian) {
        this.custodian = custodian;
    }
}
