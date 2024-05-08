package shinzo.cineffi.domain.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetMyPageRes {
    private Long userId;
    private String nickname;
    private String userProfileImage;
    private int collectionNum;
    private int scrapNum;
    private int followerNum;
    private int followingNum;
    private int level;
    private int exp;
    private int expMax;
    private Boolean isCertified;
    private Boolean isBad;
    private Boolean isFollowed;
    private LabelDTO scoreLabel;
    private LabelDTO genreLabel;
}