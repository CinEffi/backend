package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewByMovieDTO {
    private Long reviewId;
    private Boolean isMyReview;
    private String userId;
    private String nickname;
    private Integer level;
    private String userProfileImage;
    private Boolean isCertified;
    private Boolean isBad;
    private String content;
    private Float score;
    private Integer likeNumber;
//    private LocalDateTime createdAt;
    private LocalDate createdAt;
    private Boolean isLiked;
}