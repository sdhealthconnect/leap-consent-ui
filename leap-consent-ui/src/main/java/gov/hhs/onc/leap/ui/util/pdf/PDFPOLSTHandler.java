package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.adr.model.POLSTPortableMedicalOrder;
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
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
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

public class PDFPOLSTHandler {

    private static final Logger log = LoggerFactory.getLogger(PDFPOLSTHandler.class);

    private POLSTPortableMedicalOrder polst;


    private ConsentSession consentSession;

    private byte[] pdfAsByteArray;

    private ConsentUser consentUser;

    private PDFSigningService PDFSigningService;

    public PDFPOLSTHandler(PDFSigningService PDFSigningService) {
        this.PDFSigningService = PDFSigningService;
    }


    public StreamResource retrievePDFForm(POLSTPortableMedicalOrder polst) {
        this.polst = polst;
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
        // Forcing English
        // Spanish version of POLST has also been provided. However the system will use this Spanish version for informational purposes to the user. A POLST form must be signed and completed in English only.
        String fullFormPath = "/advanced_directives/"+patientState+"/POLST/English/POLST.pdf";
        if (getClass().getResource(fullFormPath) == null) {
            //Using English as default if the resource do not exists
            fullFormPath = "/advanced_directives/"+patientState+"/POLST/English/POLST.pdf";
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
            stream = new StreamResource("POLST.pdf", iFactory);
        }
        catch (IOException ix) {
            log.error("Failed PDF Processing POLST Portable Medical Order "+ix.getMessage());
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
                if (field.getFullyQualifiedName().equals("patientFirstName")) field.setValue(polst.getPatientFirstName());
                if (field.getFullyQualifiedName().equals("patientMiddleName")) field.setValue(polst.getPatientMiddleName());
                if (field.getFullyQualifiedName().equals("lastName")) field.setValue(polst.getPatientLastName());
                if (field.getFullyQualifiedName().equals("preferredName")) field.setValue(polst.getPatientPreferredName());
                if (field.getFullyQualifiedName().equals("suffix")) field.setValue(polst.getPatientSuffix());
                if (field.getFullyQualifiedName().equals("dateOfBirth")) field.setValue(polst.getPatientDateOfBirth());
                if (field.getFullyQualifiedName().equals("last4SSN")) field.setValue(polst.getLast4SSN());

                if (field.getFullyQualifiedName().equals("genderM")) {
                    if (polst.isGenderM()) field.setValue("M");
                }
                if (field.getFullyQualifiedName().equals("genderF")) {
                    if (polst.isGenderF()) field.setValue("F");
                }
                if (field.getFullyQualifiedName().equals("genderX")) {
                    if (polst.isGenderX()) field.setValue("X");
                }

                if (field.getFullyQualifiedName().equals("yesCPR")) {
                    if (polst.isYesCPR()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("noCPR")) {
                    if (polst.isNoCPR()) ((PDCheckBox) field).check();
                }

                if (field.getFullyQualifiedName().equals("fullTreatment")) {
                    if (polst.isFullTreatments()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("selectiveTreatment")) {
                    if (polst.isSelectiveTreatments()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("comfortFocusTreatment")) {
                    if (polst.isComfortFocusedTreament()) ((PDCheckBox) field).check();
                }

                if (field.getFullyQualifiedName().equals("additionalOrders")) field.setValue(polst.getAdditionalTreatments());

                if (field.getFullyQualifiedName().equals("provideFeeding")) {
                    if (polst.isNutritionByArtificialMeans()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("trialFeeding")) {
                    if (polst.isTrialNutritionByArtificialMeans()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("noArtificalFeeding")) {
                    if (polst.isNoArtificialMeans()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("noDecisionOnFeeding")) {
                    if (polst.isNoNutritionDecisionMade()) ((PDCheckBox) field).check();
                }

                if (field.getFullyQualifiedName().equals("patientOrRepresentativeSignature_af_image")) {
                        try {
                            insertImageInField(field, polst.getBase64EncodedSignature(), doc);
                        }
                        catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("nameOfRepresentative")) field.setValue(polst.getRepresentativeName());
                if (field.getFullyQualifiedName().equals("representativeAuthority")) field.setValue(polst.getRepresentativeAuthority());

                if (field.getFullyQualifiedName().equals("healthcareProviderSignature_af_image")) {
                        try {
                            insertImageInField(field, polst.getBase64EncodedSignatureHealthcareProvider(), doc);
                        }
                        catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("healthcareProviderSignatureDate")) field.setValue(polst.getSignatureDate());

                if (field.getFullyQualifiedName().equals("healthcareProviderName")) field.setValue(polst.getHealthcareProviderFullName());
                if (field.getFullyQualifiedName().equals("healthcareProviderLicenseCert")) field.setValue(polst.getHealthcareProviderLicenseOrCert());
                if (field.getFullyQualifiedName().equals("healthcareProviderPhoneNumber")) field.setValue(polst.getHealthcareProviderPhoneNumber());

                if (field.getFullyQualifiedName().equals("supervisorSignatureRequired")) {
                    if (polst.isRequiredSupervisingPhysicianSignature()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("supervisingPhysicianLicense")) {
                    if (!polst.isRequiredSupervisingPhysicianSignature()) {
                        field.setValue(polst.getSupervisingPhysicianLicense());
                    }
                }
                if (field.getFullyQualifiedName().equals("supervisingPhysicianSignature_af_image")) {
                    if (!polst.isRequiredSupervisingPhysicianSignature()) {
                        try {
                            insertImageInField(field, polst.getBase64EncodedSupervisingPhysicianSignature(), doc);
                        }
                        catch (Exception ex) {}
                    }
                }

                if (field.getFullyQualifiedName().equals("patientFullName")) field.setValue(polst.getPatientLastName()+", "+polst.getPatientFirstName()+" "+polst.getPatientMiddleName());

                if (field.getFullyQualifiedName().equals("emergencyContactFullName")) field.setValue(polst.getEmergencyContactFullName());
                if (field.getFullyQualifiedName().equals("legalRepresentative")) {
                    if (polst.isPatientRepresentative()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("otherEmergencyContact")) {
                    if (polst.isOtherEmergencyType()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("dayPhoneNumber")) field.setValue(polst.getEmergencyContactPhoneNumberDay());
                if (field.getFullyQualifiedName().equals("nightPhoneNumber")) field.setValue(polst.getEmergencyContactPhoneNumberNight());

                if (field.getFullyQualifiedName().equals("primaryProviderFullName")) field.setValue(polst.getPrimaryPhysicianFullName());
                if (field.getFullyQualifiedName().equals("primaryProviderPhoneNumber")) field.setValue(polst.getPrimaryPhysicianPhoneNumber());

                //hospice
                if (field.getFullyQualifiedName().equals("enrolledInHospice")) {
                    if (polst.isInHospice()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("hospiceAgencyName")) {
                    if (polst.isInHospice()) {
                        field.setValue(polst.getHospiceAgencyName());
                    }
                }
                if (field.getFullyQualifiedName().equals("hospiceAgencyPhoneNumber")) {
                    if (polst.isInHospice()) {
                        field.setValue(polst.getHospiceAgencyPhoneNumber());
                    }
                }

                //directive review
                if (field.getFullyQualifiedName().equals("advanceDirectiveReviewed")) {
                    if (polst.isAdvancedDirectiveReviewed()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("dateAdvanceDirectiveReviewed")) field.setValue(polst.getDateAdvancedDirectiveReviewed());
                if (field.getFullyQualifiedName().equals("advanceDirectiveConflictExists")) {
                    if (polst.isAdvanceDirectiveConflictExists()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("advanceDirectiveNotAvailable")) {
                    if (polst.isAdvanceDirectiveNotAvailable()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("noAdvanceDirectiveExists")) {
                    if (polst.isNoAdvanceDirectiveExists()) ((PDCheckBox) field).check();
                }

                if (field.getFullyQualifiedName().equals("patientDecisionMaker")) {
                    if (polst.isPatientWithDecisionMakingCapacity()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("legalSurrogateHealthcareAgent")) {
                    if (polst.isLegalSurrogateOrHealthcareAgent()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("courtAppointedGuardian")) {
                    if (polst.isCourtAppointedGuardian()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("parentOfMinor")) {
                    if (polst.isParentOfMinor()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("otherParticipated")) {
                    if (polst.isOtherParticipants()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("otherParticipantList")) field.setValue(polst.getOtherParticipantsList());
                if (field.getFullyQualifiedName().equals("assistingHealthcareProviderFullName")) field.setValue(polst.getAssistingHealthcareProviderFullName());
                if (field.getFullyQualifiedName().equals("healthcareProviderDateAssisted")) field.setValue(polst.getDateAssistedByHealthcareProvider());
                if (field.getFullyQualifiedName().equals("assistingHealthcareProviderPhoneNumber")) field.setValue(polst.getAssistingHealthcareProviderPhoneNumber());

                if (field.getFullyQualifiedName().equals("socialWorker")) {
                    if (polst.isSocialWorker()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("nurse")) {
                    if (polst.isNurse()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("clergy")) {
                    if (polst.isClergy()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("otherAssistedType")) {
                    if (polst.isAssistingOther()) ((PDCheckBox) field).check();
                }
                if (field.getFullyQualifiedName().equals("otherAssistedTypeList")) field.setValue(polst.getAssistingOtherList());

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
