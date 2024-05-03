package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class InMovieDetailDTO {

    private Long movieId;
    private String movieTitle;
    private LocalDate releaseDate;
    private byte[] poster;
    private String originCountry;
    private List<String> genre;
}
