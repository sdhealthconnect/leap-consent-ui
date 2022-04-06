package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.backend.model.SDOHOrganization;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PDFACORNHandler {
    private static final Logger log = LoggerFactory.getLogger(PDFACORNHandler.class);

    private String formType;
    private List<SDOHOrganization> sdohOrganizationList;
    private byte[] patientSignature;


    private ConsentSession consentSession;

    private byte[] pdfAsByteArray;

    private ConsentUser consentUser;

    private gov.hhs.onc.leap.signature.PDFSigningService PDFSigningService;

    public PDFACORNHandler(PDFSigningService PDFSigningService) {
        this.PDFSigningService = PDFSigningService;
    }

    public StreamResource updateAndRetrievePDFForm(String formType, List<SDOHOrganization> sdohOrganizationList,
                                                   byte[] patientSignature) {
        this.formType = formType;
        this.sdohOrganizationList = sdohOrganizationList;
        this.patientSignature = patientSignature;

        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        consentUser = consentSession.getConsentUser();
        StreamResource stream = retrieveUpdatedResearchStudyPDF();
        return stream;
    }



    private StreamResource retrieveUpdatedResearchStudyPDF() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientState = consentSession.getPrimaryState();
        String languagePreference = consentSession.getLanguagePreference();
        languagePreference = UIUtils.getLanguage(languagePreference);
        String fullFormPath = "/sdoh/"+languagePreference+"/"+formType+".pdf";
        if (getClass().getResource(fullFormPath) == null) {
            //Using English as default if the resource do not exists
            fullFormPath = "/sdoh/English"+languagePreference+"/"+formType+".pdf";
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
            stream = new StreamResource("sdoh.pdf", iFactory);
        }
        catch (IOException ix) {
            log.error("Failed PDF Processing ACORN Consent "+ix.getMessage());
        }
        return stream;
    }

    private PDDocument setFields(PDDocument doc) {
        try {
            String fullName = consentSession.getConsentUser().getLastName()+", "+consentSession.getConsentUser().getFirstName()+" "+consentSession.getConsentUser().getMiddleName();

            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm acroForm = catalog.getAcroForm();
            List<PDField> fieldList = acroForm.getFields();
            Iterator iter = fieldList.iterator();

            //populate the field we know about from questionnaire
            while (iter.hasNext()) {
                PDField field = (PDField)iter.next();

                if (field.getFullyQualifiedName().equals("sdoh1")) field.setValue(getServiceOrganization(0));
                if (field.getFullyQualifiedName().equals("sdoh2")) field.setValue(getServiceOrganization(1));
                if (field.getFullyQualifiedName().equals("sdoh3")) field.setValue(getServiceOrganization(2));
                if (field.getFullyQualifiedName().equals("sdoh4")) field.setValue(getServiceOrganization(3));
                if (field.getFullyQualifiedName().equals("sdoh5")) field.setValue(getServiceOrganization(4));
                if (field.getFullyQualifiedName().equals("sdoh6")) field.setValue(getServiceOrganization(5));
                if (field.getFullyQualifiedName().equals("sdoh7")) field.setValue(getServiceOrganization(6));
                if (field.getFullyQualifiedName().equals("sdoh8")) field.setValue(getServiceOrganization(7));
                if (field.getFullyQualifiedName().equals("sdoh9")) field.setValue(getServiceOrganization(8));


                if (field.getFullyQualifiedName().equals("patientSignature_af_image")) {
                    try {
                        insertImageInField(field, patientSignature, doc);
                    }
                    catch (Exception ex) {}
                }
                if (field.getFullyQualifiedName().equals("patientName")) field.setValue(fullName);
                if (field.getFullyQualifiedName().equals("patientSignatureDate")) field.setValue(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));


                field.setReadOnly(true);
            }
            acroForm.flatten();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }

    private String getServiceOrganization(int i) {
        String res = "";
        try {
            SDOHOrganization sdoh = sdohOrganizationList.get(i);
            res = sdoh.getName()+" - "+sdoh.getProgramname();
        }
        catch (Exception ex) {
            log.warn("No SDOH selection for field.");
        }
        return res;
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
