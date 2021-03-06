package gov.hhs.onc.leap.backend.model;

import java.util.Date;

public class PrincipleAlternateSignature {
    private String nameOfWitnessOrNotary;
    private byte[] base64EncodedSignature;
    private Date dateSigned;

    public String getNameOfWitnessOrNotary() {
        return nameOfWitnessOrNotary;
    }

    public void setNameOfWitnessOrNotary(String nameOfWitnessOrNotary) {
        this.nameOfWitnessOrNotary = nameOfWitnessOrNotary;
    }

    public byte[] getBase64EncodedSignature() {
        return base64EncodedSignature;
    }

    public void setBase64EncodedSignature(byte[] base64EncodedSignature) {
        this.base64EncodedSignature = base64EncodedSignature;
    }

    public Date getDateSigned() {
        return dateSigned;
    }

    public void setDateSigned(Date dateSigned) {
        this.dateSigned = dateSigned;
    }
}
