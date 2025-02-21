package shinzo.cineffi.domain.dto;

import lombok.Getter;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.user.ImageConverter;

@Getter
public class UserDto {
    private String userId;
    private String userNickname;
    private Integer level;
    private String profileImage;
    private Boolean isBad;
    private Boolean isCertified;

    public UserDto from (User user){
        this.userId = EncryptUtil.LongEncrypt(user.getId());
        this.userNickname = user.getNickname();
        this.level = user.getLevel();
        this.profileImage = ImageConverter.decodeImage(user.getProfileImage());
        this.isBad = user.getIsBad();
        this.isCertified = user.getIsCertified();

        return this;
    }
}
