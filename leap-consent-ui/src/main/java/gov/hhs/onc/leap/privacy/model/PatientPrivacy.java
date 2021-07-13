package gov.hhs.onc.leap.privacy.model;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

public class PatientPrivacy {
    //date of enforcement
    private boolean useDefaultOptions = false;
    private boolean oneDay = false;
    private boolean oneYear = false;
    private boolean fiveYears = false;
    private boolean tenYears = false;
    private LocalDateTime defaultOptionStartDate;
    private LocalDateTime defaultOptionEndDate;

    private boolean useCustomDateOption = false;
    private LocalDateTime customStartDate;
    private LocalDateTime customEndDate;

    //Data Class Constraints;
    private boolean limitDataClasses = false;
    private Set<String> dataClassList;

    private boolean noDataClassConstraints = false;

    //custodian source
    private boolean practitionerDataSource = false;
    private String practitionerName;

    private boolean organizationDataSource = false;
    private String organizationName;

    //recipient destination
    private boolean practitionerDataRecipient = false;
    private String practitionerRecipientName;

    private boolean organizationDataRecipient = false;
    private String organizationRecipientName;

    //data sensitivity constraints
    private boolean removeThem = false;
    private boolean removeAllLabeledConfidential = false;

    private boolean noPrivacyConcerns = false;

    //acceptance
    private boolean acceptedAndSignaturedCaptured = false;


    public boolean isUseDefaultOptions() {
        return useDefaultOptions;
    }

    public void setUseDefaultOptions(boolean useDefaultOptions) {
        this.useDefaultOptions = useDefaultOptions;
    }

    public boolean isOneDay() {
        return oneDay;
    }

    public void setOneDay(boolean oneDay) {
        this.oneDay = oneDay;
    }

    public boolean isOneYear() {
        return oneYear;
    }

    public void setOneYear(boolean oneYear) {
        this.oneYear = oneYear;
    }

    public boolean isFiveYears() {
        return fiveYears;
    }

    public void setFiveYears(boolean fiveYears) {
        this.fiveYears = fiveYears;
    }

    public boolean isTenYears() {
        return tenYears;
    }

    public void setTenYears(boolean tenYears) {
        this.tenYears = tenYears;
    }

    public boolean isUseCustomDateOption() {
        return useCustomDateOption;
    }

    public void setUseCustomDateOption(boolean useCustomDateOption) {
        this.useCustomDateOption = useCustomDateOption;
    }

    public boolean isLimitDataClasses() {
        return limitDataClasses;
    }

    public void setLimitDataClasses(boolean limitDataClasses) {
        this.limitDataClasses = limitDataClasses;
    }

    public Set<String> getDataClassList() {
        return dataClassList;
    }

    public void setDataClassList(Set<String> dataClassList) {
        this.dataClassList = dataClassList;
    }

    public boolean isNoDataClassConstraints() {
        return noDataClassConstraints;
    }

    public void setNoDataClassConstraints(boolean noDataClassConstraints) {
        this.noDataClassConstraints = noDataClassConstraints;
    }

    public boolean isPractitionerDataSource() {
        return practitionerDataSource;
    }

    public void setPractitionerDataSource(boolean practitionerDataSource) {
        this.practitionerDataSource = practitionerDataSource;
    }

    public String getPractitionerName() {
        return practitionerName;
    }

    public void setPractitionerName(String practitionerName) {
        this.practitionerName = practitionerName;
    }

    public boolean isOrganizationDataSource() {
        return organizationDataSource;
    }

    public void setOrganizationDataSource(boolean organizationDataSource) {
        this.organizationDataSource = organizationDataSource;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public boolean isPractitionerDataRecipient() {
        return practitionerDataRecipient;
    }

    public void setPractitionerDataRecipient(boolean practitionerDataRecipient) {
        this.practitionerDataRecipient = practitionerDataRecipient;
    }

    public String getPractitionerRecipientName() {
        return practitionerRecipientName;
    }

    public void setPractitionerRecipientName(String practitionerRecipientName) {
        this.practitionerRecipientName = practitionerRecipientName;
    }

    public boolean isOrganizationDataRecipient() {
        return organizationDataRecipient;
    }

    public void setOrganizationDataRecipient(boolean organizationDataRecipient) {
        this.organizationDataRecipient = organizationDataRecipient;
    }

    public String getOrganizationRecipientName() {
        return organizationRecipientName;
    }

    public void setOrganizationRecipientName(String organizationRecipientName) {
        this.organizationRecipientName = organizationRecipientName;
    }

    public boolean isRemoveThem() {
        return removeThem;
    }

    public void setRemoveThem(boolean removeThem) {
        this.removeThem = removeThem;
    }

    public boolean isRemoveAllLabeledConfidential() {
        return removeAllLabeledConfidential;
    }

    public void setRemoveAllLabeledConfidential(boolean removeAllLabeledConfidential) {
        this.removeAllLabeledConfidential = removeAllLabeledConfidential;
    }

    public boolean isNoPrivacyConcerns() {
        return noPrivacyConcerns;
    }

    public void setNoPrivacyConcerns(boolean noPrivacyConcerns) {
        this.noPrivacyConcerns = noPrivacyConcerns;
    }

    public boolean isAcceptedAndSignaturedCaptured() {
        return acceptedAndSignaturedCaptured;
    }

    public void setAcceptedAndSignaturedCaptured(boolean acceptedAndSignaturedCaptured) {
        this.acceptedAndSignaturedCaptured = acceptedAndSignaturedCaptured;
    }

    public LocalDateTime getCustomStartDate() {
        return customStartDate;
    }

    public void setCustomStartDate(LocalDateTime customStartDate) {
        this.customStartDate = customStartDate;
    }

    public LocalDateTime getCustomEndDate() {
        return customEndDate;
    }

    public void setCustomEndDate(LocalDateTime customEndDate) {
        this.customEndDate = customEndDate;
    }

    public LocalDateTime getDefaultOptionStartDate() {
        return defaultOptionStartDate;
    }

    public void setDefaultOptionStartDate(LocalDateTime defaultOptionStartDate) {
        this.defaultOptionStartDate = defaultOptionStartDate;
    }

    public LocalDateTime getDefaultOptionEndDate() {
        return defaultOptionEndDate;
    }

    public void setDefaultOptionEndDate(LocalDateTime defaultOptionEndDate) {
        this.defaultOptionEndDate = defaultOptionEndDate;
    }
}
