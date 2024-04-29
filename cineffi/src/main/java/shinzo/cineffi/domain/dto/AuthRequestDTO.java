package shinzo.cineffi.domain.dto;
import lombok.*;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.enums.LoginType;

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
