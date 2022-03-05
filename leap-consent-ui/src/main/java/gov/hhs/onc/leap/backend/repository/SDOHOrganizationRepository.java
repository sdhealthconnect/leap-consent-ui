package gov.hhs.onc.leap.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import gov.hhs.onc.leap.backend.model.SDOHOrganization;

import java.util.List;

@Repository
public interface SDOHOrganizationRepository extends JpaRepository<SDOHOrganization, Long> {

    @Query("Select a from SDOHOrganization a where a.type = :type and a.city = :city and a.state = :state")
    List<SDOHOrganization> getSDOHOrganizationByTypeAndCityAndState(@Param("type") String type, @Param("city") String city, @Param("state") String state);

    @Query("Select a from SDOHOrganization a where a.type = :type and a.state = :state and a.county = :county")
    List<SDOHOrganization> getSDOHOrganizationByTypeAndStateAndCounty(@Param("type") String type, @Param("state") String state, @Param("county") String country);
}
