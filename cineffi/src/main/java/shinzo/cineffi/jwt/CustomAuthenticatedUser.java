package shinzo.cineffi.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter@Setter
public class CustomAuthenticatedUser extends AbstractAuthenticationToken {

    private Long userSequence;

    public CustomAuthenticatedUser(Collection<? extends GrantedAuthority> authorities, Long userSequence, boolean isAuthenticated) {
        super(authorities);
        super.setAuthenticated(true);
        this.setAuthenticated(isAuthenticated);
        this.userSequence = userSequence;
    }


    public Map<String, Object> objToMap() {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put("userSequence", userSequence);
        attributes.put("role", this.getAuthorities().stream()
                .findFirst()
                .orElseGet(() -> new SimpleGrantedAuthority("ROLE_USER"))
                .getAuthority());

        return attributes;
    }

    public static CustomAuthenticatedUser mapToObj(Map<String, Object> attributes) {
        return new CustomAuthenticatedUser(Collections.singleton(new SimpleGrantedAuthority(String.valueOf(attributes.get("role")))),
                Long.valueOf(attributes.get("userSequence").toString()),
                true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.userSequence;
    }

    public String getRole() {
        return this.getAuthorities().stream()
                .findFirst()
                .orElseGet(() -> new SimpleGrantedAuthority("ROLE_USER"))
                .getAuthority();
    }
}
//package shinzo.cineffi.jwt;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.security.authentication.AbstractAuthenticationToken;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.Map;
//
//@Getter@Setter
//public class CustomAuthenticatedUser extends AbstractAuthenticationToken {
//
//    private Long userSequence;
//
//    public CustomAuthenticatedUser(Collection<? extends GrantedAuthority> authorities, Long userSequence, boolean isAuthenticated) {
//        super(authorities);
//        this.setAuthenticated(isAuthenticated);
//        this.userSequence = userSequence;
//    }
//
//
//    public Map<String, Object> objToMap() {
//        Map<String, Object> attributes = new HashMap<>();
//
//        attributes.put("userSequence", userSequence);
//        attributes.put("role", this.getAuthorities().stream()
//                .findFirst()
//                .orElseGet(() -> new SimpleGrantedAuthority("ROLE_USER"))
//                .getAuthority());
//
//        return attributes;
//    }
//
//    public static CustomAuthenticatedUser mapToObj(Map<String, Object> attributes) {
//        return new CustomAuthenticatedUser(Collections.singleton(new SimpleGrantedAuthority(String.valueOf(attributes.get("role")))),
//                Long.valueOf(attributes.get("userSequence").toString()),
//                true);
//    }
//
//    @Override
//    public Object getCredentials() {
//        return null;
//    }
//
//    @Override
//    public Object getPrincipal() {
//        return this.userSequence;
//    }
//
//    public String getRole() {
//        return this.getAuthorities().stream()
//                .findFirst()
//                .orElseGet(() -> new SimpleGrantedAuthority("ROLE_USER"))
//                .getAuthority();
//    }
//}