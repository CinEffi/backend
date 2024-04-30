package shinzo.cineffi.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

@Getter@Setter
public class CustomAuthenticatedUser extends AbstractAuthenticationToken {

    private Long userSequence;

    public CustomAuthenticatedUser(Collection<? extends GrantedAuthority> authorities, Long userSequence, boolean isAuthenticated) {
        super(authorities);
        super.setAuthenticated(true);
        this.setAuthenticated(isAuthenticated);
        this.userSequence = userSequence;
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
