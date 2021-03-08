package gov.hhs.onc.leap.adr.model;

public class HipaaWaiver {
    private boolean useDisclosure = false;
    private byte[] base64EncodedInitials;

    public boolean isUseDisclosure() {
        return useDisclosure;
    }

    public void setUseDisclosure(boolean useDisclosure) {
        this.useDisclosure = useDisclosure;
    }

    public byte[] getBase64EncodedInitials() {
        return base64EncodedInitials;
    }

    public void setBase64EncodedInitials(byte[] base64EncodedInitials) {
        this.base64EncodedInitials = base64EncodedInitials;
    }
}
