package gov.hhs.onc.leap.adr.model;

import java.util.Date;

public class PhysicansAffidavit {
    private String physiciansName;
    private String principlesName;
    private String signatureDate;
    private byte[] base64EncodedSignature;


    public String getPhysiciansName() {
        return physiciansName;
    }

    public void setPhysiciansName(String physiciansName) {
        this.physiciansName = physiciansName;
    }

    public String getPrinciplesName() {
        return principlesName;
    }

    public void setPrinciplesName(String principlesName) {
        this.principlesName = principlesName;
    }

    public String getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(String signatureDate) {
        this.signatureDate = signatureDate;
    }

    public byte[] getBase64EncodedSignature() {
        return base64EncodedSignature;
    }

    public void setBase64EncodedSignature(byte[] base64EncodedSignature) {
        this.base64EncodedSignature = base64EncodedSignature;
    }
}
