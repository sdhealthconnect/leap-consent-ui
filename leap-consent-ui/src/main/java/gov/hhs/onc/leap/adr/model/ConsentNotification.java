package gov.hhs.onc.leap.adr.model;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import gov.hhs.onc.leap.ui.util.UIUtils;
import gov.hhs.onc.leap.ui.util.css.lumo.BadgeColor;

public class ConsentNotification {
    private String actionRequired;
    private Status status;
    private String shortName;
    private String description;
    private String destinationView;

    public ConsentNotification(String actionRequired, Status status, String shortName, String description, String destinationView) {
        this.actionRequired = actionRequired;
        this.status = status;
        this.shortName = shortName;
        this.description = description;
        this.destinationView = destinationView;
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

    public enum Status {
                ACTIVE(VaadinIcon.LOCK, "Active", "This consent is in force.", BadgeColor.CONTRAST),
                EXPIRED(VaadinIcon.CLOCK, "Expired", "Allowed Date Range is nolonger valid.", BadgeColor.NORMAL),
                REVOKED(VaadinIcon.WARNING, "Revoked", "User has set this Consent to Inactive.", BadgeColor.ERROR),
                PENDING(VaadinIcon.FILE_PROCESS, "Action Pending", "User Action Required.", BadgeColor.NORMAL),
                NOTCOMPLETE(VaadinIcon.TIMER, "Not Completed", "May require user action", BadgeColor.NORMAL);



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
