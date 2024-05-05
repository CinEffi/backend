package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReviewLookupDTO {
    private Long movieId;
    private String movieTitle;
    private byte[] moviePoster;
    private Long reviewId;
    private Long reviewWriterId;
    private String reviewWriterNickname;
    private String reviewContent;
    private Integer likeNumber;
    private LocalDateTime createdAt;
}