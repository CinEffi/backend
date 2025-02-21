package shinzo.cineffi.domain.request;

import lombok.Data;

@Data
public class EditProfileReq {
    private String password;
    private String nickname;
}
