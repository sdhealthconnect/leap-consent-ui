package gov.hhs.onc.leap.sdoh.model;

public class SDOHNeed {
    private String domain;
    private boolean needed;


    public SDOHNeed() {

    }

    public SDOHNeed(String domain, boolean needed) {
        this.domain = domain;
        this.needed = needed;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isNeeded() {
        return needed;
    }

    public void setNeeded(boolean needed) {
        this.needed = needed;
    }
}

