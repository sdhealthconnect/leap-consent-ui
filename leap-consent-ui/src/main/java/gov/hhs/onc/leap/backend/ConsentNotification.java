package gov.hhs.onc.leap.backend;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.lumo.BadgeColor;
import org.hl7.fhir.r4.model.Resource;

import java.util.Date;

public class ConsentNotification {
    private Date notificationDate;
    private String actionRequired;
    private Status status;
    private String shortName;
    private String description;
    private String destinationView;
    private Resource fhirResource;

    public ConsentNotification(Date notificationDate,String actionRequired, Status status, String shortName, String description, String destinationView, Resource fhirResource) {
        this.notificationDate = notificationDate;
        this.actionRequired = actionRequired;
        this.status = status;
        this.shortName = shortName;
        this.description = description;
        this.destinationView = destinationView;
        this.fhirResource = fhirResource;
    }

    public String getActionRequired() {
        return actionRequired;
    }

    public Status getStatus() {
        return status;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDescription() {
        return description;
    }

    public String getDestinationView() {
        return destinationView;
    }

    public void setActionRequired(String actionRequired) {
        this.actionRequired = actionRequired;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDestinationView(String destinationView) {
        this.destinationView = destinationView;
    }

    public Resource getFhirResource() { return fhirResource; }

    public void setFhirResource(Resource fhirResource) { this.fhirResource = fhirResource; }

    public Date getNotificationDate() { return notificationDate; }

    public void setNotificationDate(Date notificationDate) { this.notificationDate = notificationDate; }

    public enum Status {
                ACTIVE(VaadinIcon.LOCK, "Active", "This consent is in force.", BadgeColor.CONTRAST),
                EXPIRED(VaadinIcon.CLOCK, "Expired", "Allowed Date Range is nolonger valid.", BadgeColor.NORMAL),
                REVOKED(VaadinIcon.WARNING, "Revoked", "User has set this Consent to Inactive.", BadgeColor.ERROR),
                PENDING(VaadinIcon.FILE_PROCESS, "Action Pending", "User Action Required.", BadgeColor.NORMAL),
                NOTCOMPLETE(VaadinIcon.TIMER, "Not Completed", "May require user action", BadgeColor.NORMAL),
                CANCELLED(VaadinIcon.EXIT, "Cancelled", "This action was cancelled.", BadgeColor.ERROR_PRIMARY),
                ONHOLD(VaadinIcon.PAUSE, "On Hold", "This action is on hold.", BadgeColor.ERROR_PRIMARY),
                DRAFT(VaadinIcon.EDIT, "Draft", "This action is currently in draft form.", BadgeColor.NORMAL),
                ENTEREDINERROR(VaadinIcon.DATE_INPUT, "Input In Error.", "This action is not valid was input in error.", BadgeColor.ERROR),
                COMPLETE(VaadinIcon.SUN_DOWN, "Completed", "This action has completed", BadgeColor.NORMAL),
                STOPPED(VaadinIcon.STOP, "Stopped", "This action has been stopped.", BadgeColor.NORMAL),
                UNKNOWN(VaadinIcon.FIRE, "Unknown", "This action's status is unknown", BadgeColor.ERROR);



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
                case NOTCOMPLETE:
                    icon = UIUtils.createSuccessIcon(this.icon);
                    break;
                case CANCELLED:
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
}
