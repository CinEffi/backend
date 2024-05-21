package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class InListMovieDTO {
    private Long movieId;
    private String title;
    private LocalDate releaseDate;
    private String poster;
    private Float levelAvgScore;
    private Float cinephileAvgScore;
}