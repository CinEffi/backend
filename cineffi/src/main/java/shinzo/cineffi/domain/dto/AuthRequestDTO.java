package shinzo.cineffi.domain.dto;
import lombok.experimental.SuperBuilder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@RequiredArgsConstructor
public class AuthRequestDTO {
    private String email;
    private String password;
    private String nickname;
    private Boolean isauthentication;
}
