package gov.hhs.onc.leap.backend;

import gov.hhs.onc.leap.backend.model.SDOHOrganization;
import gov.hhs.onc.leap.backend.repository.SDOHOrganizationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SDOHOrganizationService {

    private SDOHOrganizationRepository sdohOrganizationRepository;

    public SDOHOrganizationService(SDOHOrganizationRepository sdohOrganizationRepository) {
        this.sdohOrganizationRepository = sdohOrganizationRepository;
    }

    public List<SDOHOrganization> getSDOHOrganizationByTypeAndCityAndState(String type, String city, String state) {
        return sdohOrganizationRepository.getSDOHOrganizationByTypeAndCityAndState(type, city, state);
    }
}
