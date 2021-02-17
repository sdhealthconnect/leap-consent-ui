package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.session.ConsentSession;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDInlineImage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class PDFPatientPrivacyHandler {

    private String sDate;
    private String eDate;
    private String dataDomainConstraintList;
    private String custodian;
    private String recipient;
    private String sensitivities;
    private byte[] patientSignatureImage;
    private String signatureLocation;
    private ConsentSession consentSession;

    public StreamResource retrievePDFForm(String sDate, String eDate, String dataDomainConstraintList, String custodian,
                                          String recipient, String sensitivities, byte[] patientSignatureImage) {
        this.sDate = sDate;
        this.eDate = eDate;
        this.dataDomainConstraintList = dataDomainConstraintList;
        this.custodian = custodian;
        this.recipient = recipient;
        this.sensitivities = sensitivities;
        this.patientSignatureImage = patientSignatureImage;
        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        StreamResource stream = retrievePatientPrivacyForm();
        return stream;
    }

    public StreamResource retrievePatientPrivacyForm() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");


        String fullFormPath = "/patient-privacy/patient-privacy-demo.pdf";
        byte[] bArray = null;
        PDDocument pdfdocument = null;
        StreamResource stream = null;
        try {
            bArray = IOUtils.toByteArray(getClass().getResourceAsStream(fullFormPath));
            pdfdocument = PDDocument.load(bArray);
            pdfdocument = setFields(pdfdocument);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdfdocument.save(out);
            pdfdocument.close();
            InputStream bi = new ByteArrayInputStream(out.toByteArray());
            InputStreamFactory iFactory = new InputStreamFactory() {
                @Override
                public InputStream createInputStream() {
                    InputStream bi = new ByteArrayInputStream(out.toByteArray());
                    return bi;
                }
            };
            stream = new StreamResource("patient-privacy.pdf",iFactory);
        }
        catch (IOException ix) {
            //add handling
        }
        return stream;
    }

    private PDDocument setFields(PDDocument doc) {
        try {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm arcoForm = catalog.getAcroForm();
            List<PDField> fieldList = arcoForm.getFields();
            Iterator iter = fieldList.iterator();
            String fullName = consentSession.getConsentUser().getLastName()+", "+consentSession.getConsentUser().getFirstName()+" "+consentSession.getConsentUser().getMiddleName();
            signatureLocation = VaadinSession.getCurrent().getBrowser().getAddress();

            //populate the field we know about from session
            while (iter.hasNext()) {
                PDField field = (PDField)iter.next();
                if (field.getFullyQualifiedName().equals("startDate")) field.setValue(sDate);
                if (field.getFullyQualifiedName().equals("endDate")) field.setValue(eDate);
                if (field.getFullyQualifiedName().equals("dataClasses")) field.setValue(dataDomainConstraintList);
                if (field.getFullyQualifiedName().equals("custodian")) field.setValue(custodian);
                if (field.getFullyQualifiedName().equals("recipient")) field.setValue(recipient);
                if (field.getFullyQualifiedName().equals("sensitivity")) field.setValue(sensitivities);
                if (field.getFullyQualifiedName().equals("patientSignature_af_image")) {

                }
                if (field.getFullyQualifiedName().equals("patientName")) field.setValue(fullName);
                if (field.getFullyQualifiedName().equals("signatureDate")) field.setValue(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                if (field.getFullyQualifiedName().equals("signatureLocation")) field.setValue(signatureLocation);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }

    private PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }
}
