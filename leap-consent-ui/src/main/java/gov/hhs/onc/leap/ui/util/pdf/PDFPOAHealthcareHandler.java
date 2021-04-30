package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.adr.model.PowerOfAttorneyHealthCare;
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

public class PDFPOAHealthcareHandler {

    private static final Logger log = LoggerFactory.getLogger(PDFPOAHealthcareHandler.class);

    private PowerOfAttorneyHealthCare poaHealthcare;
    private byte[] initials;

    private ConsentSession consentSession;

    private byte[] pdfAsByteArray;

    private ConsentUser consentUser;

    private PDFSigningService PDFSigningService;

    public PDFPOAHealthcareHandler(PDFSigningService PDFSigningService) {
        this.PDFSigningService = PDFSigningService;
    }


    public StreamResource retrievePDFForm(PowerOfAttorneyHealthCare poaHealthcare, byte[] initials) {
        this.poaHealthcare = poaHealthcare;
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
        languagePreference = UIUtils.getLanguage(languagePreference);
        String fullFormPath = "/advanced_directives/"+patientState+"/POAHealthcare/"+languagePreference+"/POAHealthcare.pdf";
        if (getClass().getResource(fullFormPath) == null) {
            //Using English as default if the resource do not exists
            fullFormPath = "/advanced_directives/"+patientState+"/POAHealthcare/English/POAHealthcare.pdf";
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
            stream = new StreamResource("POAHealthcare.pdf", iFactory);
        }
        catch (IOException ix) {
            log.error("Failed PDF Processing Healthcare POA "+ix.getMessage());
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
                if (field.getFullyQualifiedName().equals("patientFullName")) field.setValue(poaHealthcare.getPrinciple().getName());
                if (field.getFullyQualifiedName().equals("patientAddress1")) field.setValue(poaHealthcare.getPrinciple().getAddress1());
                if (field.getFullyQualifiedName().equals("patientAddress2")) field.setValue(poaHealthcare.getPrinciple().getAddress2());
                if (field.getFullyQualifiedName().equals("dateOfBirth")) field.setValue(poaHealthcare.getPrinciple().getDateOfBirth());
                if (field.getFullyQualifiedName().equals("patientPhoneNumber")) field.setValue(poaHealthcare.getPrinciple().getPhoneNumber());
                if (field.getFullyQualifiedName().equals("patientEmailAddress")) field.setValue(poaHealthcare.getPrinciple().getEmailAddress());

                if (field.getFullyQualifiedName().equals("poaHealthcare")) field.setValue(poaHealthcare.getAgent().getName());
                if (field.getFullyQualifiedName().equals("poaHealthcareAddress1")) field.setValue(poaHealthcare.getAgent().getAddress1());
                if (field.getFullyQualifiedName().equals("poaHealthcareAddress2")) field.setValue(poaHealthcare.getAgent().getAddress2());
                if (field.getFullyQualifiedName().equals("poaHealthcareHomePhone")) field.setValue(poaHealthcare.getAgent().getHomePhone());
                if (field.getFullyQualifiedName().equals("poaHealthcareWorkPhone")) field.setValue(poaHealthcare.getAgent().getWorkPhone());
                if (field.getFullyQualifiedName().equals("poaHealthcareCellPhone")) field.setValue(poaHealthcare.getAgent().getCellPhone());

                if (field.getFullyQualifiedName().equals("poaHealthcareAlt")) field.setValue(poaHealthcare.getAlternate().getName());
                if (field.getFullyQualifiedName().equals("poaHealthcareAltAddress1")) field.setValue(poaHealthcare.getAlternate().getAddress1());
                if (field.getFullyQualifiedName().equals("poaHealthcareAltAddress2")) field.setValue(poaHealthcare.getAlternate().getAddress2());
                if (field.getFullyQualifiedName().equals("poaHealthcareAltHomePhone")) field.setValue(poaHealthcare.getAlternate().getHomePhone());
                if (field.getFullyQualifiedName().equals("poaHealthcareAltWorkPhone")) field.setValue(poaHealthcare.getAlternate().getWorkPhone());
                if (field.getFullyQualifiedName().equals("poaHealthcareAltCellPhone")) field.setValue(poaHealthcare.getAlternate().getCellPhone());

                if (field.getFullyQualifiedName().equals("explicitDoNotAuthorizeTreatment1")) field.setValue(poaHealthcare.getDoNotAuthorize1());
                if (field.getFullyQualifiedName().equals("explicitDoNotAuthorizeTreatment2")) field.setValue(poaHealthcare.getDoNotAuthorize2());
                if (field.getFullyQualifiedName().equals("explicitDoNotAuthorizeTreatment3")) field.setValue(poaHealthcare.getDoNotAuthorize3());
                //Autopsy
                if (field.getFullyQualifiedName().equals("denyVoluntaryAutopsy_af_image")) {
                    if (poaHealthcare.isDenyAutopsy()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitVolunatryAutopsy_af_image")) {
                    if (poaHealthcare.isPermitAutopsy()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("poaDecisionVoluntaryAutopsy_af_image")) {
                    if(poaHealthcare.isAgentDecidesAutopsy()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                //organ donation
                if (field.getFullyQualifiedName().equals("denyOrganDonation_af_image")) {
                    if (poaHealthcare.isDenyOrganTissueDonation()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("donorCardOrAgreementExists_af_image")) {
                    if (poaHealthcare.isHaveExistingOrganTissueCardOrAgreement()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("donorCardOrAgreementCustodian")) field.setValue(poaHealthcare.getOrganTissueCardOrAgreementInstitution());
                if (field.getFullyQualifiedName().equals("permitOrganOrTissueDonation_af_image")) {
                    if (poaHealthcare.isPermitOrganTissueDonation()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitWholeBody_af_image")) {
                    if (poaHealthcare.isWholeBodyDonation()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitAnyNeededPart_af_image")) {
                    if (poaHealthcare.isAnyPartOrOrganNeeded()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitLimitedTo_af_image")) {
                    if (poaHealthcare.isSpecificPartsOrOrgans()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permittedPartsOrOrgansList")) field.setValue(poaHealthcare.getSpecificPartsOrOrgansList());
                if (field.getFullyQualifiedName().equals("permitPOUAnyLegal_af_image")) {
                    if (poaHealthcare.isAnyLegalPurpose()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitPOUTransplantTherapeutic_af_image")) {
                    if (poaHealthcare.isTransplantOrTherapeutic()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitPOUResearch_af_image")) {
                    if (poaHealthcare.isResearchOnly()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("permitPOUOther_af_image")) {
                    if (poaHealthcare.isOtherPurposes()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("pouOrganTissueDonationPTRQTLIST")) field.setValue(poaHealthcare.getOtherPurposesList());
                if (field.getFullyQualifiedName().equals("explicitOrganTissueDonationDestination_af_image")) {
                    if (poaHealthcare.isPrincipleDefined()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("explicitOrganTissueDonationDestinationValue")) field.setValue(poaHealthcare.getPrincipleDefinedList());
                if (field.getFullyQualifiedName().equals("poaDecidesOrganTissueDestination_af_image")) {
                    if (poaHealthcare.isAgentDecidedOrganTissueDestination()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }

                //burial information
                if (field.getFullyQualifiedName().equals("uponMyDeathBodyBuried_af_image")) {
                    if (poaHealthcare.isBodyToBeBuried()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("uponMyDeathBodyBuriedIn_af_image")) {
                    if (poaHealthcare.isBodyToBeBuriedIn()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("uponMyDeathBodyBuriedInValue")) field.setValue(poaHealthcare.getBodyToBeBuriedInInstructions());
                if (field.getFullyQualifiedName().equals("uponMyDeathBodyCremated_af_image")) {
                    if (poaHealthcare.isBodyToBeCremated()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("uponMyDeathBodyCrematedDisposition_af_image")) {
                    if (poaHealthcare.isBodyToBeCrematedAshesDisposition()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("uponMyDeathBodyCrematedDispositionValue2")) field.setValue(poaHealthcare.getBodyToBeCrematedAshesDispositionInstructions());
                if (field.getFullyQualifiedName().equals("poaMakesBurialDecisions_af_image")) {
                    if (poaHealthcare.isAgentDecidesBurial()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }

                //support documents
                if (field.getFullyQualifiedName().equals("signedLivingWill_af_image")) {
                    if (poaHealthcare.isSignedLivingWill()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("notSignedLivingWill_af_image")) {
                    if (poaHealthcare.isNotSignedLivingWill()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("signedPOLST_af_image")) {
                    if (poaHealthcare.isSignedPOLST()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("notSignedPOLST_af_image")) {
                    if (poaHealthcare.isNotSignedPOLST()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("signedDNR_af_image")) {
                    if (poaHealthcare.isSignedDNR()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                if (field.getFullyQualifiedName().equals("notSignedDNR_af_image")) {
                    if (poaHealthcare.isNotSignedDNR()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }

                //attestation
                if (field.getFullyQualifiedName().equals("affidavitPhysicianName")) field.setValue(poaHealthcare.getPhysiciansAffidavit().getPhysiciansName());
                if (field.getFullyQualifiedName().equals("affidavitPatientName")) field.setValue(poaHealthcare.getPhysiciansAffidavit().getPrinciplesName());
                if (field.getFullyQualifiedName().equals("affidavitDate")) field.setValue(poaHealthcare.getPhysiciansAffidavit().getSignatureDate());
                if (field.getFullyQualifiedName().equals("affidavitPhysicianSignature_af_image")) {
                    try {
                        insertImageInField(field, poaHealthcare.getPhysiciansAffidavit().getBase64EncodedSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                //hipaa waiver
                if (field.getFullyQualifiedName().equals("hipaaInitials_af_image")) {
                    if (poaHealthcare.getHipaaWaiver().isUseDisclosure()) {
                        try {
                            insertImageInField(field, initials, doc);
                        }
                        catch (Exception ex) {}
                    }
                }
                //patient principle signature
                if (field.getFullyQualifiedName().equals("patientSignature_af_image")) {
                    try {
                        insertImageInField(field, poaHealthcare.getPrincipleSignature().getBase64EncodeSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("patientSignatureDate")) field.setValue(poaHealthcare.getPrincipleSignature().getDateSigned());

                //alternate principle signature
                if (field.getFullyQualifiedName().equals("witnessNotarySignature_af_image")) {
                    try {
                        insertImageInField(field, poaHealthcare.getPrincipleAlternateSignature().getBase64EncodedSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("witnessNotarySignatureDate")) field.setValue(poaHealthcare.getPrincipleAlternateSignature().getDateSigned());
                if (field.getFullyQualifiedName().equals("witnessNotaryName")) field.setValue(poaHealthcare.getPrincipleAlternateSignature().getNameOfWitnessOrNotary());


                if (field.getFullyQualifiedName().equals("witnessSignature_af_image")) {
                    try {
                        insertImageInField(field, poaHealthcare.getWitnessSignature().getBase64EncodedSignature(), doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("witnessSignatureDate")) field.setValue(poaHealthcare.getWitnessSignature().getDateSigned());
                if (field.getFullyQualifiedName().equals("witnessName")) field.setValue(poaHealthcare.getWitnessSignature().getWitnessName());
                if (field.getFullyQualifiedName().equals("witnessAddress")) field.setValue(poaHealthcare.getWitnessSignature().getWitnessAddress());


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
