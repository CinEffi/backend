package shinzo.cineffi.domain.dto;

import lombok.Data;

@Data
public class EditProfileReq {
    private String password;
    private String nickname;
}
