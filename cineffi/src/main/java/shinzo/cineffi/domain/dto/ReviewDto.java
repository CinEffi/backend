package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto {
    private Long movieId;
    private String movieTitle;
    private String poster;
    private Long reviewId;
    private String content;
    private Float userScore;
    private int likeNumber;
    private Boolean isLiked;
    private Boolean isMyReview;
}