package shinzo.cineffi.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetFollowRes {
    private Long followId;
    private Long userId;
    private String nickname;
    private String profileImage;
    private int level;
    private boolean isCertified;
    private boolean isBad;
    private boolean isFollowed;
}
