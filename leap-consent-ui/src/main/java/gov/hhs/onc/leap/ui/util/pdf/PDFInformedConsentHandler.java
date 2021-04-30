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
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckbox;
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

public class PDFInformedConsentHandler {
    private static final Logger log = LoggerFactory.getLogger(PDFInformedConsentHandler.class);

    private String formType;
    private String rxnormPreferredLabel;
    private String medicationRequestID;
    private boolean consentGranted;
    private boolean consentDeclined;
    private byte[] patientSignature;
    private String patientName;
    private String patientSignatureDate;
    private byte[] physicianSignature;
    private String physicianName;
    private String physicianSignatureDate;

    private ConsentSession consentSession;

    private byte[] pdfAsByteArray;

    private ConsentUser consentUser;

    private gov.hhs.onc.leap.signature.PDFSigningService PDFSigningService;

    public PDFInformedConsentHandler(PDFSigningService PDFSigningService) {
        this.PDFSigningService = PDFSigningService;
    }

    public StreamResource retrievePDFForm(String formType) {
        this.formType = formType;
        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        StreamResource stream = retrieveLanguageAndStateSpecificForm();
        return stream;
    }

    public StreamResource updateAndRetrievePDFForm(String formType, String rxnormPreferredLabel, String medicationRequestID, boolean consentGranted, boolean consentDeclined,
                                                   byte[] patientSignature, String patientName, String patientSignatureDate, byte[] physicianSignature, String physicianName,
                                                   String physicianSignatureDate) {
        this.formType = formType;
        this.rxnormPreferredLabel = rxnormPreferredLabel;
        this.medicationRequestID = medicationRequestID;
        this.consentGranted = consentGranted;
        this.consentDeclined = consentDeclined;
        this.patientSignature = patientSignature;
        this.patientName = patientName;
        this.patientSignatureDate = patientSignatureDate;
        this.physicianSignature = physicianSignature;
        this.physicianName = physicianName;
        this.physicianSignatureDate = physicianSignatureDate;


        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        consentUser = consentSession.getConsentUser();
        StreamResource stream = retrieveUpdatedInformedConsentPDF();
        return stream;
    }

    public StreamResource retrieveLanguageAndStateSpecificForm() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientState = consentSession.getPrimaryState();
        String languagePreference = consentSession.getLanguagePreference();
        languagePreference = UIUtils.getLanguage(languagePreference);
        String fullFormPath = "/informed-consent/medication-request/"+languagePreference+"/"+formType+".pdf";
        if (getClass().getResource(fullFormPath) == null) {
            //Using English as default if the resource do not exists
            fullFormPath = "/informed-consent/medication-request/English/"+formType+".pdf";
        }
        byte[] bArray = null;
        PDDocument pdfdocument = null;
        StreamResource stream = null;
        try {
            bArray = IOUtils.toByteArray(getClass().getResourceAsStream(fullFormPath));
            pdfdocument = PDDocument.load(bArray);
            //pdfdocument = setFields(pdfdocument);
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
            stream = new StreamResource(formType+".pdf",iFactory);
        }
        catch (IOException ix) {
            //add handling
        }
        return stream;
    }

    private StreamResource retrieveUpdatedInformedConsentPDF() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientState = consentSession.getPrimaryState();
        String languagePreference = consentSession.getLanguagePreference();
        languagePreference = UIUtils.getLanguage(languagePreference);
        String fullFormPath = "/informed-consent/medication-request/"+languagePreference+"/"+formType+".pdf";
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
            stream = new StreamResource("InformedConsent.pdf", iFactory);
        }
        catch (IOException ix) {
            log.error("Failed PDF Processing Informed Consent "+ix.getMessage());
        }
        return stream;
    }

    private PDDocument setFields(PDDocument doc) {
        try {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            List<PDField> fieldList = acroForm.getFields();
            Iterator iter = fieldList.iterator();

            //populate the field we know about from questionnaire
            while (iter.hasNext()) {
                PDField field = (PDField)iter.next();

                if (field.getFullyQualifiedName().equals("rxnormPreferredLabel")) field.setValue(rxnormPreferredLabel);
                if (field.getFullyQualifiedName().equals("medicationRequestID")) field.setValue(medicationRequestID);
                if (field.getFullyQualifiedName().equals("consentGranted")) {
                    if (consentGranted) ((PDCheckbox) field).check();
                }
                if (field.getFullyQualifiedName().equals("consentDeclined")) {
                    if (consentDeclined) ((PDCheckbox) field).check();
                }
                if (field.getFullyQualifiedName().equals("patientSignature_af_image")) {
                    try {
                        insertImageInField(field, patientSignature, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("patientName")) field.setValue(patientName);
                if (field.getFullyQualifiedName().equals("patientSignatureDate")) field.setValue(patientSignatureDate);
                if (field.getFullyQualifiedName().equals("physicianSignature_af_image")) {
                    try {
                        insertImageInField(field, physicianSignature, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("physicianName")) field.setValue(physicianName);
                if (field.getFullyQualifiedName().equals("physicianSignatureDate")) field.setValue(physicianSignatureDate);

                field.setReadOnly(true);
            }
            acroForm.flatten();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }

    private PDDocument signDocumentWithCert(PDDocument pdDocument) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        pdDocument.save(byteArrayOutputStream);

        byte[] pdfSigned = PDFSigningService.signPdf(byteArrayOutputStream.toByteArray());
        pdDocument.close(); //We need to close the document that will be modified
        PDDocument signedDoc = PDDocument.load(pdfSigned);
        return signedDoc;
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
