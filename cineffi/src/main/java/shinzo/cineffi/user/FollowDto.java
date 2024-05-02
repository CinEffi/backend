package shinzo.cineffi.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowDto {
    private Long followId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private int level;
    private Boolean isCertified;
    private Boolean isBad;
    private Boolean isFollowed;
}
