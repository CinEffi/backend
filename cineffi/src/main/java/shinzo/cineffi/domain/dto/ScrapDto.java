package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ScrapDto {
    private Long movieId;
    private String title;
    private String poster;
    private float userScore;
    private LocalDate releaseDate;
    private boolean isScrap;
}
