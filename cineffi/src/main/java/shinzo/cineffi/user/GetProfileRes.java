package shinzo.cineffi.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import shinzo.cineffi.domain.enums.LoginType;

@Data
@AllArgsConstructor
public class GetProfileRes {
    private String nickname;
    private String email;
    private String userProfileImage;
}
