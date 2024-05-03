package shinzo.cineffi.domain.dto;

import lombok.Getter;

@Getter
public class ReviewCreateDTO {
    Long movieId;
    String content;
}
