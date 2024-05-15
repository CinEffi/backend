package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;
import shinzo.cineffi.domain.entity.movie.AvgScore;
import shinzo.cineffi.domain.entity.score.Score;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewLookupDTO {
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private Long reviewId;
    private Long reviewWriterId;
    private String reviewWriterNickname;
    private String reviewContent;
    private Integer likeNumber;

    private Integer level;
    private Boolean isLiked;
    private Boolean isCertified;
    private Boolean isBad;

//    private LocalDateTime createdAt;
    private LocalDate createdAt;
    private Float reviewScore;
}