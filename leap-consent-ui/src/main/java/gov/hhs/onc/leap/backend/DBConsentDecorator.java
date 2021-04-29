package gov.hhs.onc.leap.backend;


import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.session.ConsentSession;

public class DBConsentDecorator implements ConsentDecorator {

    private ConsentSession consentSession;
    private ConsentUserService consentUserService;

    public DBConsentDecorator(ConsentSession consentSession, ConsentUserService consentUserService) {
        this.consentSession = consentSession;
        this.consentUserService = consentUserService;
    }

    @Override
    public ConsentSession decorate() {
        if (consentSession.getConsentUser()==null || consentSession.getConsentUser().getUser()==null || consentSession.getConsentUser().getUser().getFhirPatientId()==null){
            return null;
        }
        ConsentUser consentUser = consentUserService.getConsentUser(consentSession.getConsentUser().getUser().getFhirPatientId());
        consentUser.setEmailAddress(consentSession.getConsentUser().getUser().getEmail());
        consentUser.setUserName(consentSession.getConsentUser().getUser().getUserName());
        consentSession.setConsentUser(consentUser);
        consentSession.setUserId(consentSession.getConsentUser().getUser().getFhirPatientId());
        consentSession.setUsername(consentUser.getUserName());
        consentSession.setPrimaryState(consentUser.getState());
        consentSession.setFhirPatientId(consentSession.getConsentUser().getUser().getFhirPatientId());
        consentSession.setLanguagePreference(consentUser.getLanguagePreference());
        return consentSession;
    }
}
