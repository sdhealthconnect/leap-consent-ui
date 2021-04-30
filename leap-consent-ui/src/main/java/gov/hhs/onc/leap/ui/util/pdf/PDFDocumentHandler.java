package gov.hhs.onc.leap.ui.util.pdf;

import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;
import gov.hhs.onc.leap.session.ConsentSession;
import gov.hhs.onc.leap.ui.util.UIUtils;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

public class PDFDocumentHandler {

    private String formType;
    private ConsentSession consentSession;

    public StreamResource retrievePDFForm(String formType) {
        this.formType = formType;
        consentSession = (ConsentSession)VaadinSession.getCurrent().getAttribute("consentSession");
        StreamResource stream = retrieveLanguageAndStateSpecificForm();
        return stream;
    }

    public StreamResource retrieveLanguageAndStateSpecificForm() {
        ConsentSession consentSession = (ConsentSession) VaadinSession.getCurrent().getAttribute("consentSession");
        String patientState = consentSession.getPrimaryState();
        String languagePreference = consentSession.getLanguagePreference();
        languagePreference = UIUtils.getLanguage(languagePreference);
        String fullFormPath = "/advanced_directives/"+patientState+"/"+formType+"/"+languagePreference+"/"+formType+".pdf";
        if (getClass().getResource(fullFormPath) == null) {
            //Using English as default if the resource do not exists
            fullFormPath = "/advanced_directives/" + patientState + "/" + formType + "/English/" + formType + ".pdf";
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

    private PDDocument setFields(PDDocument doc) {
        try {
            PDDocumentCatalog catalog = doc.getDocumentCatalog();
            PDAcroForm arcoForm = catalog.getAcroForm();
            List<PDField> fieldList = arcoForm.getFields();
            Iterator iter = fieldList.iterator();
            String fullName = consentSession.getConsentUser().getLastName()+", "+consentSession.getConsentUser().getFirstName()+" "+consentSession.getConsentUser().getMiddleName();
            String address1 = consentSession.getConsentUser().getStreetAddress1()+", "+consentSession.getConsentUser().getStreetAddress2();
            String address2 = consentSession.getConsentUser().getCity()+", "+consentSession.getConsentUser().getState()+" "+consentSession.getConsentUser().getZipCode();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String dateOfBirth = sdf.format(consentSession.getConsentUser().getDateOfBirth());
            String eyeColor = consentSession.getConsentUser().getEyeColor();
            String hairColor = consentSession.getConsentUser().getHairColor();
            String ethnicity = consentSession.getConsentUser().getEthnicity();
            String primaryPhysician = consentSession.getConsentUser().getPrimaryPhysician();
            String primaryPhysicianPhone = consentSession.getConsentUser().getPrimaryPhysicianPhoneNumber();
            String gender = consentSession.getConsentUser().getGender();
            String patientPhoneNumber = consentSession.getConsentUser().getPhone();
            String patientEmailAddress = consentSession.getConsentUser().getEmailAddress();

            //populate the field we know about from session
            while (iter.hasNext()) {
                PDField field = (PDField)iter.next();
                if (field.getFullyQualifiedName().equals("patientFullName")) field.setValue(fullName);
                if (field.getFullyQualifiedName().equals("dateOfBirth")) field.setValue(dateOfBirth);
                if (field.getFullyQualifiedName().equals("gender")) field.setValue(gender);
                if (field.getFullyQualifiedName().equals("ethnicity")) field.setValue(ethnicity);
                if (field.getFullyQualifiedName().equals("eyeColor")) field.setValue(eyeColor);
                if (field.getFullyQualifiedName().equals("hairColor")) field.setValue(hairColor);
                if (field.getFullyQualifiedName().equals("primaryPhysician")) field.setValue(primaryPhysician);
                if (field.getFullyQualifiedName().equals("primaryPhysicianPhoneNumber")) field.setValue(primaryPhysicianPhone);
                if (field.getFullyQualifiedName().equals("patientAddress1")) field.setValue(address1);
                if (field.getFullyQualifiedName().equals("patientAddress2")) field.setValue(address2);
                if (field.getFullyQualifiedName().equals("patientPhoneNumber")) field.setValue(patientPhoneNumber);
                if (field.getFullyQualifiedName().equals("patientEmailAddress")) field.setValue(patientEmailAddress);
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return doc;
    }
}
