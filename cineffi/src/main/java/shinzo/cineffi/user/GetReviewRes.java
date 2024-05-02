package shinzo.cineffi.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetReviewRes {
    private Long movieId;
    private String movieTitle;
    private String poster;
    private Long reviewId;
    private String content;
    private float userScore;
    private int likeNumber;
}
