package gov.hhs.onc.leap.backend.repository;



import gov.hhs.onc.leap.backend.model.ConsentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsentUserRepository extends JpaRepository<ConsentUser, Long> {

    ConsentUser findByUser_FhirPatientId(String patientId);
}
