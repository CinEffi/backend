package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetProfileRes {
    private String nickname;
    private String email;
    private String userProfileImage;
}
