package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.signature.PDFSigningService;
import gov.hhs.onc.leap.ui.util.UIUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
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
import java.util.Iterator;
import java.util.List;

public class PDFDNRHandler {

    private static final Logger log = LoggerFactory.getLogger(PDFDNRHandler.class);
    private String patientFullName;
    private byte[] patientsignatureImage;
    private String patientsignatureDate;
    private String poaHealthcare;
    private byte[] poaSignatureImage;
    private String dateOfBirth;
    private String gender;
    private String ethnicity;
    private String eyeColor;
    private String hairColor;
    private byte[] patientPhotoImage;
    private String primaryPhysician;
    private String primaryPhysicianPhoneNumber;
    private String hospiceProgram;
    private byte[] healthcareprovidersignatureImage;
    private String healthcareprovidersignaturedate;
    private byte[] witnesssignatureImage;
    private String witnesssignaturedate;

    private ConsentSession consentSession;

    private byte[] pdfAsByteArray;

    private ConsentUser consentUser;

    private PDFSigningService PDFSigningService;

    public PDFDNRHandler (PDFSigningService PDFSigningService) {
        this.PDFSigningService = PDFSigningService;
    }


    public StreamResource retrievePDFForm(String patientFullName, byte[] patientsignatureImage, String patientsignatureDate, String poaHealthcare, byte[] poaSignatureImage,
                                          String dateOfBirth, String gender, String ethnicity, String eyeColor, String hairColor, byte[] patientPhotoImage,
                                          String primaryPhysician, String primaryPhysicianPhoneNumber, String hospiceProgram, byte[] healthcareprovidersignatureImage,
                                          String healthcareprovidersignaturedate, byte[] witnesssignatureImage, String witnesssignaturedate) {
        this.patientFullName = patientFullName;
        this.patientsignatureImage = patientsignatureImage;
        this.patientsignatureDate = patientsignatureDate;
        this.poaHealthcare = poaHealthcare;
        this.poaSignatureImage = poaSignatureImage;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.ethnicity = ethnicity;
        this.eyeColor = eyeColor;
        this.hairColor = hairColor;
        this.patientPhotoImage = patientPhotoImage;
        this.primaryPhysician = primaryPhysician;
        this.primaryPhysicianPhoneNumber = primaryPhysicianPhoneNumber;
        this.hospiceProgram = hospiceProgram;
        this.healthcareprovidersignatureImage = healthcareprovidersignatureImage;
        this.healthcareprovidersignaturedate = healthcareprovidersignaturedate;
        this.witnesssignatureImage = witnesssignatureImage;
        this.witnesssignaturedate = witnesssignaturedate;
        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        consentUser = consentSession.getConsentUser();
        StreamResource stream = retrievePatientPrivacyForm();
        return stream;
    }

    public StreamResource retrievePatientPrivacyForm() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientState = consentSession.getPrimaryState();
        String languagePreference = consentSession.getLanguagePreference();
        languagePreference = UIUtils.getLanguage(languagePreference);
        String fullFormPath = "/advanced_directives/"+patientState+"/DNR/"+languagePreference+"/DNR.pdf";
        if (getClass().getResource(fullFormPath) == null) {
            //Using English as default if the resource do not exists
            fullFormPath = "/advanced_directives/"+patientState+"/DNR/English/DNR.pdf";
        }
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
            stream = new StreamResource("DNR.pdf", iFactory);
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


            //populate the field we know about from session
            while (iter.hasNext()) {
                PDField field = (PDField) iter.next();
                if (field.getFullyQualifiedName().equals("patientFullName")) field.setValue(patientFullName);
                if (field.getFullyQualifiedName().equals("patientsignature_af_image")) {
                    try {
                        insertImageInField(field, patientsignatureImage, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("patientSignatureDate")) field.setValue(patientsignatureDate);
                if (field.getFullyQualifiedName().equals("poaHealthcare")) field.setValue(poaHealthcare);
                if (field.getFullyQualifiedName().equals("poahealthcaresignature_af_image")) {
                     try {
                         insertImageInField(field, poaSignatureImage, doc);
                     }
                     catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("dateOfBirth")) field.setValue(dateOfBirth);
                if (field.getFullyQualifiedName().equals("gender")) field.setValue(gender);
                if (field.getFullyQualifiedName().equals("ethnicity")) field.setValue(ethnicity);
                if (field.getFullyQualifiedName().equals("eyeColor")) field.setValue(eyeColor);
                if (field.getFullyQualifiedName().equals("hairColor")) field.setValue(hairColor);
                if (field.getFullyQualifiedName().equals("patientphoto_af_image")) {
                    try {
                        insertImageInField(field,patientPhotoImage, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("primaryPhysician")) field.setValue(primaryPhysician);
                if (field.getFullyQualifiedName().equals("primaryPhysicianPhoneNumber")) field.setValue(primaryPhysicianPhoneNumber);
                if (field.getFullyQualifiedName().equals("healthcareprovidersignature_af_image")) {
                    try {
                        insertImageInField(field, healthcareprovidersignatureImage, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("hospiceProgram")) field.setValue(hospiceProgram);
                if (field.getFullyQualifiedName().equals("healthcareprovidersignaturedate")) field.setValue(healthcareprovidersignaturedate);
                if (field.getFullyQualifiedName().equals("witnesssignature_af_image")) {
                    try {
                        insertImageInField(field, witnesssignatureImage, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("witnesssignaturedate")) field.setValue(witnesssignaturedate);
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
        //TODO: Move this to some utils to nor replicate the code
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
