package gov.hhs.onc.leap.backend;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.lumo.BadgeColor;
import org.hl7.fhir.r4.model.Consent;

import java.time.LocalDate;
import java.util.Date;

public class ConsentDocument {
    private Status status;
    private boolean consentState;
    private String policyType;
    private String source;
    private String destination;
    private LocalDate startDate;
    private LocalDate endDate;
    private String constrainSensitivity;
    private String constrainDomains;
    private Consent fhirConsentResource;

    public String getConstrainSensitivity() {
        return constrainSensitivity;
    }

    public void setConstrainSensitivity(String constrainSensitivity) {
        this.constrainSensitivity = constrainSensitivity;
    }

    public String getConstrainDomains() {
        return constrainDomains;
    }

    public void setConstrainDomains(String constrainDomains) {
        this.constrainDomains = constrainDomains;
    }

    public Consent getFhirConsentResource() {
        return fhirConsentResource;
    }

    public void setFhirConsentResource(Consent fhirConsentResource) {
        this.fhirConsentResource = fhirConsentResource;
    }

    public enum Status {
        ACTIVE(VaadinIcon.LOCK, "Active", "This consent is in force.", BadgeColor.CONTRAST),
        EXPIRED(VaadinIcon.CLOCK, "Expired", "Allowed Date Range is nolonger valid.", BadgeColor.ERROR),
        REVOKED(VaadinIcon.WARNING, "Revoked", "Patient has set this Consent to Inactive.", BadgeColor.ERROR),
        PENDING(VaadinIcon.FILE_PROCESS, "Pending", "User Action Required.", BadgeColor.NORMAL);

        private VaadinIcon icon;
        private String name;
        private String desc;
        private BadgeColor theme;

        Status(VaadinIcon icon, String name, String desc, BadgeColor theme) {
            this.icon = icon;
            this.name = name;
            this.desc = desc;
            this.theme = theme;
        }
        public Icon getIcon() {
            Icon icon;
            switch (this) {
                case ACTIVE:
                    icon = UIUtils.createSecondaryIcon(this.icon);
                    break;
                case EXPIRED:
                    icon = UIUtils.createPrimaryIcon(this.icon);
                    break;
                case REVOKED:
                    icon = UIUtils.createSuccessIcon(this.icon);
                    break;
                case PENDING:
                    icon = UIUtils.createSuccessIcon(this.icon);
                    break;
                default:
                    icon = UIUtils.createErrorIcon(this.icon);
                    break;
            }
            return icon;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public BadgeColor getTheme() {
            return theme;
        }
    }

    public ConsentDocument(Status status, boolean consentState, String policyType, String source, String destination, LocalDate startDate, LocalDate endDate, String constrainSensitivity, String constrainDomains, Consent fhirConsentResource) {
        this.status = status;
        this.consentState = consentState;
        this.policyType = policyType;
        this.source = source;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.constrainSensitivity = constrainSensitivity;
        this.constrainDomains = constrainDomains;
        this.fhirConsentResource = fhirConsentResource;
    }

    public Status getStatus() { return status; }

    public boolean isConsentState() {
        return consentState;
    }

    public String getPolicyType() {
        return policyType;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}
