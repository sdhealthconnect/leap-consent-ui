package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.backend.model.ConsentUser;
import gov.hhs.onc.leap.backend.repository.ConsentUserRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsentUserService {

    private ConsentUserRepository consentUserRepository;

    public ConsentUserService(ConsentUserRepository consentUserRepository){
        this.consentUserRepository = consentUserRepository;
    }

    public ConsentUser getConsentUser(String patientId){
        return consentUserRepository.findByUser_FhirPatientId(patientId);
    }

}
