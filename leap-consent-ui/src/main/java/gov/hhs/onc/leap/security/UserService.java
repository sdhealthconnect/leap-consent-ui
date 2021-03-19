package gov.hhs.onc.leap.security;

import gov.hhs.onc.leap.security.model.Role;
import gov.hhs.onc.leap.security.model.User;
import gov.hhs.onc.leap.security.repository.RoleRepository;
import gov.hhs.onc.leap.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;

/**
 * A User Service to retrieve the entity from database.
 *
 * @author: sgroh@saperi.io
 */
@Service
public class UserService {

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    public User saveUser(User user) {
        return saveUser(user, false);
    }

    public User saveUser(User user, boolean savePass) {
        if (savePass){
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }
        user.setActive(true);
        if (user.getRoles().isEmpty()){
            Role userRole = roleRepository.findByRole("USER");
            user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
        }
        return userRepository.save(user);
    }

}
