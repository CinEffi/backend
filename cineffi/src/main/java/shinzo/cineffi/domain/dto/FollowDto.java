package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FollowDto {
    private String followId;
    private String userId;
    private String nickname;
    private String profileImage;
    private int level;
    private Boolean isCertified;
    private Boolean isBad;
    private Boolean isFollowed;
}
