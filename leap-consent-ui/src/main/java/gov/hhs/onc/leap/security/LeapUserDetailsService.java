package gov.hhs.onc.leap.security;

import gov.hhs.onc.leap.security.model.Role;
import gov.hhs.onc.leap.security.model.User;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.rowset.serial.SerialBlob;
import javax.transaction.Transactional;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static gov.hhs.onc.leap.ui.util.UIUtils.IMG_PATH;

/**
 * A Leap Consent UserDetailsService implementation to augment the base UserDetailsService.
 *
 * This class will create the custom {@link UserDetails}, please see {@link User} for further details.
 * @author: sgroh@saperi.io
 */
@Service
public class LeapUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userService.findUserByUserName(userName);
        List<GrantedAuthority> authorities = getUserAuthority(user.getRoles());
        return buildUserForAuthentication(user, authorities);
    }

    private List<GrantedAuthority> getUserAuthority(Set<Role> userRoles) {
        Set<GrantedAuthority> roles = new HashSet<GrantedAuthority>();
        for (Role role : userRoles) {
            roles.add(new SimpleGrantedAuthority(role.getRole()));
        }
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>(roles);
        return grantedAuthorities;
    }

    private UserDetails buildUserForAuthentication(User user, List<GrantedAuthority> authorities) {
        User nUser = new User(user.getUserName(), user.getPassword(),
                user.getActive(), true, true, true, authorities);
        nUser.setId(user.getId());
        nUser.setEmail(user.getEmail());
        nUser.setName(user.getName());
        nUser.setLastName(user.getLastName());
        nUser.setPhoto(user.getPhoto());
        nUser.setRoles(user.getRoles());
        return nUser;
    }

    @PostConstruct
    public void loadImage() {
        try {
            ClassPathResource classPathResource = new ClassPathResource(IMG_PATH + "ironmanbike.jpg");
            InputStream imageInputStream = classPathResource.getInputStream();
            User u =userService.findUserByEmail("sgroh@gmail.com");
            u.setPhoto(new SerialBlob(IOUtils.toByteArray(imageInputStream)));
            userService.saveUser(u);
        } catch (Exception e){

        }
    }
}
