package shinzo.cineffi.user;

import lombok.Data;

@Data
public class EditProfileReq {
    private String password;
    private String nickname;
}
