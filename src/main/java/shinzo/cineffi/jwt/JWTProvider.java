package shinzo.cineffi.jwt;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
@Service
public class JWTProvider {

    public Authentication getAuthentication(String token) {
        String role = JWTUtil.getClaimAttribute(token, "role");
        if (role != null) {
            Collection<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));
            String userSequence = JWTUtil.getClaimAttribute(token, "sequence");

            CustomAuthenticatedUser customAuthenticatedUser = new CustomAuthenticatedUser(authorities, Long.valueOf(userSequence), true);
            return customAuthenticatedUser;
        }
        else{
            return null;
        }

    }

}