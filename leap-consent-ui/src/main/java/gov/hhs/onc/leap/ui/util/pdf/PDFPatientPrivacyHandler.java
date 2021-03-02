package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.signature.PDFSigningService;
import gov.hhs.onc.leap.ui.MainLayout;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

public class PDFPatientPrivacyHandler {

    private static final Logger log = LoggerFactory.getLogger(PDFPatientPrivacyHandler.class);
    private String sDate;
    private String eDate;
    private String dataDomainConstraintList;
    private String custodian;
    private String recipient;
    private String sensitivities;
    private byte[] patientSignatureImage;
    private String signatureLocation;
    private ConsentSession consentSession;
    private byte[] pdfAsByteArray;
    private PDFSigningService PDFSigningService;


    public PDFPatientPrivacyHandler(PDFSigningService PDFSigningService){
        this.PDFSigningService = PDFSigningService;
    }

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
            pdfdocument = signDocumentWithCert(pdfdocument);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pdfdocument.save(out);
            pdfdocument.close();
            pdfAsByteArray = out.toByteArray();
            InputStream bi = new ByteArrayInputStream(out.toByteArray());
            InputStreamFactory iFactory = new InputStreamFactory() {
                @Override
                public InputStream createInputStream() {
                    InputStream bi = new ByteArrayInputStream(out.toByteArray());
                    return bi;
                }
            };
            stream = new StreamResource("patient-privacy.pdf", iFactory);
        }
        catch (IOException ix) {
            //add handling
        }
        return stream;
    }

    private PDDocument signDocumentWithCert(PDDocument pdDocument) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdDocument.save(byteArrayOutputStream);

        byte[] pdfSigned = PDFSigningService.signPdf(byteArrayOutputStream.toByteArray());
        pdDocument.close(); //We need to close the document that will be modified
        PDDocument signedDoc = PDDocument.load(pdfSigned);
        return signedDoc;
    }

    private PDDocument setFields(PDDocument doc) {
        try {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            List<PDField> fieldList = acroForm.getFields();
            Iterator iter = fieldList.iterator();
            String fullName = consentSession.getConsentUser().getLastName()+", "+consentSession.getConsentUser().getFirstName()+" "+consentSession.getConsentUser().getMiddleName();
            signatureLocation = VaadinSession.getCurrent().getBrowser().getAddress();

            //populate the field we know about from session
            while (iter.hasNext()) {
                PDField field = (PDField) iter.next();
                if (field.getFullyQualifiedName().equals("startDate")) field.setValue(sDate);
                if (field.getFullyQualifiedName().equals("endDate")) field.setValue(eDate);
                if (field.getFullyQualifiedName().equals("dataClasses")) field.setValue(dataDomainConstraintList);
                if (field.getFullyQualifiedName().equals("custodian")) field.setValue(custodian);
                if (field.getFullyQualifiedName().equals("recipient")) field.setValue(recipient);
                if (field.getFullyQualifiedName().equals("sensitivity")) field.setValue(sensitivities);
                if (field.getFullyQualifiedName().equals("patientSignature_af_image")) insertImageInField(field, patientSignatureImage, doc);
                if (field.getFullyQualifiedName().equals("patientName")) field.setValue(fullName);
                if (field.getFullyQualifiedName().equals("signatureDate")) field.setValue(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
                if (field.getFullyQualifiedName().equals("signatureLocation")) field.setValue(signatureLocation);
                field.setReadOnly(true);
            }
            acroForm.flatten();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }

    /**
     * Inserts an image in the PDF field.
     *
     * @param field the {@link PDField} that will include the image
     * @param baImage the image byte array
     * @param doc the PDF {@link PDDocument} that contains the field specifies as first argument.
     * @throws IOException if some IO exception occurs manipulating the PDF object
     */
    private void insertImageInField(final PDField field, final byte[] baImage,
                                    PDDocument doc) throws IOException {
        List<PDAnnotationWidget> widgets = field.getWidgets();
        if (widgets != null && widgets.size() > 0) {
            PDAnnotationWidget annotationWidget = widgets.get(0); // We need the first widget to manipulate the field element
            PDImageXObject pdImageXObject =  LosslessFactory.createFromImage(doc, createImageFromBytes(baImage));
            float imageScaleRatio = (float) pdImageXObject.getHeight() / (float) pdImageXObject.getWidth();

            PDRectangle fieldPosition = getFieldArea(field);
            float height = fieldPosition.getHeight();
            float width = height / imageScaleRatio;
            float x = 0;
            float y = 0;

            PDAppearanceStream pdAppearanceStream = new PDAppearanceStream(doc);
            pdAppearanceStream.setResources(new PDResources());
            try (PDPageContentStream pdPageContentStream = new PDPageContentStream(doc, pdAppearanceStream)) {
                pdPageContentStream.drawImage(pdImageXObject, x, y, width, height);
            }
            pdAppearanceStream.setBBox(new PDRectangle(x, y, width, height));

            PDAppearanceDictionary pdAppearanceDictionary = annotationWidget.getAppearance();
            if (pdAppearanceDictionary == null) {
                pdAppearanceDictionary = new PDAppearanceDictionary();
                annotationWidget.setAppearance(pdAppearanceDictionary);
            }

            pdAppearanceDictionary.setNormalAppearance(pdAppearanceStream);
            log.debug("Signature inserted in field {}", field.getFullyQualifiedName());
        }
    }

    /**
     * Creates a BufferedImage from a byte array.
     *
     * @param imageData the byte array image
     * @return a {@link BufferedImage} created from the byte array image.
     */
    private BufferedImage createImageFromBytes(final byte[] imageData) {
        ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
        try {
            return ImageIO.read(bais);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private PDRectangle getFieldArea(PDField field) {
        COSDictionary fieldDict = field.getCOSObject();
        COSArray fieldAreaArray = (COSArray) fieldDict.getDictionaryObject(COSName.RECT);
        return new PDRectangle(fieldAreaArray);
    }

    public byte[] getPdfAsByteArray() {
        return pdfAsByteArray;
    }

    public void setPdfAsByteArray(byte[] pdfAsByteArray) {
        this.pdfAsByteArray = pdfAsByteArray;
    }
}
