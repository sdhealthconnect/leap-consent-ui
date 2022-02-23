package gov.hhs.onc.leap.backend.model;

import javax.persistence.*;


@Entity
@Table(name = "sdohorganization")
public class SDOHOrganization {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Column(name = "type")
    private String type;
    @Column(name = "name")
    private String name;
    @Column(name = "address")
    private String address;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "county")
    private String county;
    @Column(name="postalcode")
    private String postalcode;
    @Column(name = "phonenumber")
    private String phonenumber;
    @Column(name = "emailaddress")
    private String emailaddress;
    @Column(name = "daysopen")
    private String daysopen;
    @Column(name = "hoursofoperation")
    private String hoursofoperation;
    @Column(name = "parentorganizationid")
    private String parentorganizationid;
    @Column(name = "parentorganizationname")
    private String parentorganizationname;
    @Column(name = "statewide")
    private boolean statewide;
    @Column(name = "programname")
    private String programname;
    @Column(name = "website")
    private String website;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public String getEmailaddress() {
        return emailaddress;
    }

    public void setEmailaddress(String emailaddress) {
        this.emailaddress = emailaddress;
    }

    public String getDaysopen() {
        return daysopen;
    }

    public void setDaysopen(String daysopen) {
        this.daysopen = daysopen;
    }

    public String getHoursofoperation() {
        return hoursofoperation;
    }

    public void setHoursofoperation(String hoursofoperation) {
        this.hoursofoperation = hoursofoperation;
    }

    public String getParentorganizationid() {
        return parentorganizationid;
    }

    public void setParentorganizationid(String parentorganizationid) {
        this.parentorganizationid = parentorganizationid;
    }

    public String getParentorganizationname() {
        return parentorganizationname;
    }

    public void setParentorganizationname(String parentorganizationname) {
        this.parentorganizationname = parentorganizationname;
    }

    public String getPostalcode() {
        return postalcode;
    }

    public void setPostalcode(String postalcode) {
        this.postalcode = postalcode;
    }

    public boolean isStatewide() {
        return statewide;
    }

    public void setStatewide(boolean statewide) {
        this.statewide = statewide;
    }

    public String getProgramname() {
        return programname;
    }

    public void setProgramname(String programname) {
        this.programname = programname;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
