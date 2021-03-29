package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.adr.model.LivingWill;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.signature.PDFSigningService;
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

public class PDFLivingWillHandler {

    private static final Logger log = LoggerFactory.getLogger(PDFLivingWillHandler.class);

    private LivingWill livingWill;
    private byte[] initials;

    private ConsentSession consentSession;

    private byte[] pdfAsByteArray;

    private ConsentUser consentUser;

    private PDFSigningService PDFSigningService;

    public PDFLivingWillHandler(PDFSigningService PDFSigningService) {
        this.PDFSigningService = PDFSigningService;
    }


    public StreamResource retrievePDFForm(LivingWill livingWill, byte[] initials) {
        this.livingWill = livingWill;
        this.initials = initials;
        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        consentUser = consentSession.getConsentUser();
        StreamResource stream = retrievePatientPrivacyForm();
        return stream;
    }

    public StreamResource retrievePatientPrivacyForm() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientState = consentSession.getPrimaryState();
        String languagePreference = consentSession.getLanguagePreference();

        String fullFormPath = "/advanced_directives/"+patientState+"/LivingWill/"+languagePreference+"/LivingWill.pdf";
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
            stream = new StreamResource("LivingWill.pdf", iFactory);
        }
        catch (IOException ix) {
            log.error("Failed PDF Processing Living Will "+ix.getMessage());
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
                if (field.getFullyQualifiedName().equals("patientFullName")) field.setValue(livingWill.getPrinciple().getName());
                if (field.getFullyQualifiedName().equals("patientAddress1")) field.setValue(livingWill.getPrinciple().getAddress1());
                if (field.getFullyQualifiedName().equals("patientAddress2")) field.setValue(livingWill.getPrinciple().getAddress2());
                if (field.getFullyQualifiedName().equals("dateOfBirth")) field.setValue(livingWill.getPrinciple().getDateOfBirth());
                if (field.getFullyQualifiedName().equals("patientPhoneNumber")) field.setValue(livingWill.getPrinciple().getPhoneNumber());
                if (field.getFullyQualifiedName().equals("patientEmailAddress")) field.setValue(livingWill.getPrinciple().getEmailAddress());


                if (field.getFullyQualifiedName().equals("comfortOnly_af_image")) {
                    if (livingWill.isComfortCareOnly()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("comfortOnlyAndNot_af_image")) {
                    if (livingWill.isComfortCareOnlyButNot()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("noCPR_af_image")) {
                    if (livingWill.isNoCardioPulmonaryRecusitation()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("noFluidsFood_af_image")) {
                    if (livingWill.isNoArtificalFluidsFoods()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("avoidHospital_af_image")) {
                    if (livingWill.isAvoidTakingToHospital()) {
                        insertImageInField(field, initials, doc);
                    }
                }
                if (field.getFullyQualifiedName().equals("pregnant_af_image")) {
                    if (livingWill.isPregnantSaveFetus()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("doctorsConcludeNoHope_af_image")) {
                    if (livingWill.isCareUntilDoctorsConcludeNoHope()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("prolongLife_af_image")) {
                    if (livingWill.isProlongLifeToGreatestExtentPossible()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("noAdditionalInstructions_af_image")) {
                    if (livingWill.isNoAdditionalInstructions()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("additionalInstructions_af_image")) {
                    if (livingWill.isAdditionalInstructions()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                //patient principle signature
                if (field.getFullyQualifiedName().equals("patientSignature_af_image")) {
                    try {
                        insertImageInField(field, livingWill.getPrincipleSignature().getBase64EncodeSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("patientSignatureDate")) field.setValue(livingWill.getPrincipleSignature().getDateSigned());

                //alternate principle signature
                if (field.getFullyQualifiedName().equals("witnessNotarySignature_af_image")) {
                    try {
                        insertImageInField(field, livingWill.getPrincipleAlternateSignature().getBase64EncodedSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("witnessNotarySignatureDate")) {
                    if (livingWill.getPrincipleAlternateSignature().getNameOfWitnessOrNotary() != null) {
                        field.setValue(livingWill.getPrincipleAlternateSignature().getDateSigned());
                    }
                }
                if (field.getFullyQualifiedName().equals("witnessNotaryName")) field.setValue(livingWill.getPrincipleAlternateSignature().getNameOfWitnessOrNotary());


                if (field.getFullyQualifiedName().equals("witnessSignature_af_image")) {
                    try {
                        insertImageInField(field, livingWill.getWitnessSignature().getBase64EncodedSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("witnessSignatureDate")) field.setValue(livingWill.getWitnessSignature().getDateSigned());
                if (field.getFullyQualifiedName().equals("witnessName")) field.setValue(livingWill.getWitnessSignature().getWitnessName());
                if (field.getFullyQualifiedName().equals("witnessAddress")) field.setValue(livingWill.getWitnessSignature().getWitnessAddress());


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
