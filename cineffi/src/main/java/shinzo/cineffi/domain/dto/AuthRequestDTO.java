package shinzo.cineffi.domain.dto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import shinzo.cineffi.domain.enums.LoginType;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthRequestDTO {
    private String email;
    private String password;
    private String nickname;
    private Boolean isauthentication;
}
