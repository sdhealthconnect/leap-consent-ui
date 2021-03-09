package gov.hhs.onc.leap.adr.model;


public class PrincipleSignature {
    private byte[] base64EncodeSignature;
    private String dateSigned;

    public byte[] getBase64EncodeSignature() {
        return base64EncodeSignature;
    }

    public void setBase64EncodeSignature(byte[] base64EncodeSignature) {
        this.base64EncodeSignature = base64EncodeSignature;
    }

    public String getDateSigned() {
        return dateSigned;
    }

    public void setDateSigned(String dateSigned) {
        this.dateSigned = dateSigned;
    }
}
