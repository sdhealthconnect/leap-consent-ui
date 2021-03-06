package gov.hhs.onc.leap.backend.model;

import java.util.Date;

public class PhysicansAffidavit {
    private String physiciansName;
    private String principlesName;
    private Date signatureDate;
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

    public Date getSignatureDate() {
        return signatureDate;
    }

    public void setSignatureDate(Date signatureDate) {
        this.signatureDate = signatureDate;
    }

    public byte[] getBase64EncodedSignature() {
        return base64EncodedSignature;
    }

    public void setBase64EncodedSignature(byte[] base64EncodedSignature) {
        this.base64EncodedSignature = base64EncodedSignature;
    }
}
