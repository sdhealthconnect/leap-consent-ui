package gov.hhs.onc.leap.adr.model;


public class PrincipleAlternateSignature {
    private String nameOfWitnessOrNotary;
    private byte[] base64EncodedSignature;
    private String dateSigned;

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

    public String getDateSigned() {
        return dateSigned;
    }

    public void setDateSigned(String dateSigned) {
        this.dateSigned = dateSigned;
    }
}
