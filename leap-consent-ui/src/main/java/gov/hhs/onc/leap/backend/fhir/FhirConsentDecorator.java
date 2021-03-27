package gov.hhs.onc.leap.backend.fhir;

import gov.hhs.onc.leap.backend.ConsentDecorator;
import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.backend.fhir.client.utils.FHIRPatient;
import gov.hhs.onc.leap.session.ConsentSession;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

public class FhirConsentDecorator implements ConsentDecorator {

    private ConsentSession consentSession;
    private FHIRPatient fhirPatientUtil;

    public FhirConsentDecorator(ConsentSession consentSession, FHIRPatient fhirPatientUtil){
        this.consentSession = consentSession;
        this.fhirPatientUtil = fhirPatientUtil;
    }

    @Override
    public ConsentSession decorate() {
        Patient fhirPatient = fhirPatientUtil.get();
        consentSession.setFhirPatient(fhirPatient);
        ConsentUser consentUser = consentSession.getConsentUser();
        List<HumanName> names = fhirPatient.getName();
        // Firstname
        if (names.size()>0) {
            consentUser.setPrefix(names.get(0).getPrefix().get(0).getValue());
            consentUser.setFirstName(names.get(0).getFamily());
        }
        // Middle name and lastname
        if (names.size()>2) {
            consentUser.setMiddleName(names.get(1).getFamily());
            consentUser.setLastName(names.get(2).getFamily());
        } else if (names.size()==2) {
            // Lastname
            consentUser.setLastName(names.get(1).getFamily());
        }
        //Date of birth
        consentUser.setDateOfBirth(fhirPatient.getBirthDate());
        // Marital Status
        consentUser.setMaritalStatus(fhirPatient.hasMaritalStatus()?fhirPatient.getMaritalStatus().getText():"");
        // Gender
        consentUser.setGender(fhirPatient.getGender().getDisplay());
        List<Address> addresses = fhirPatient.getAddress();
        if (addresses.size()>1) {
            // Street address 2
            consentUser.setStreetAddress2(fhirPatient.getAddress().get(1).hasLine()?fhirPatient.getAddress().get(1).getLine().get(0).getValue():"");
        } else if(addresses.size()>0) {
            // Street address 1
            consentUser.setStreetAddress1(fhirPatient.getAddress().get(0).hasLine()?fhirPatient.getAddress().get(0).getLine().get(0).getValue():"");
            consentUser.setCity(fhirPatient.getAddress().get(0).getCity());
            // Do not override this attribute with Fhir information, if needed disable next line and two letter representation will be available
            // consentUser.setState(fhirPatient.getAddress().get(0).getState());
            consentUser.setZipCode(fhirPatient.getAddress().get(0).getPostalCode());
        }
        if (fhirPatient.getTelecom().size()>0) {
            List<ContactPoint> contactPoints = fhirPatient.getTelecom();
            for (ContactPoint contactPoint: contactPoints) {
                // phone
                if (contactPoint.getSystem().getSystem().equals("phone")){
                    consentUser.setPhone(contactPoint.getValue());
                }
                // mobile
                if (contactPoint.getSystem().getSystem().equals("sms")){
                    consentUser.setMobile(contactPoint.getValue());
                }
            }

        }
        return consentSession;
    }
}
