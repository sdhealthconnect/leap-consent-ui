package gov.hhs.onc.leap.security.repository;

import gov.hhs.onc.leap.security.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<gov.hhs.onc.leap.security.model.Role, Integer> {
    Role findByRole(String role);

}
