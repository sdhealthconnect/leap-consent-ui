package gov.hhs.onc.leap.backend.model;

import java.util.Date;

public class PrincipleSignature {
    private byte[] base64EncodeSignature;
    private Date dateSigned;

    public byte[] getBase64EncodeSignature() {
        return base64EncodeSignature;
    }

    public void setBase64EncodeSignature(byte[] base64EncodeSignature) {
        this.base64EncodeSignature = base64EncodeSignature;
    }

    public Date getDateSigned() {
        return dateSigned;
    }

    public void setDateSigned(Date dateSigned) {
        this.dateSigned = dateSigned;
    }
}
