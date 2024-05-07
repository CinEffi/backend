package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewCreateDTO {
    Long movieId;
    String content;
}
