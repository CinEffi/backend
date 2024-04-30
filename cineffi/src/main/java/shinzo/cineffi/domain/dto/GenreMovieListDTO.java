package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import shinzo.cineffi.domain.enums.Genre;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class GenreMovieListDTO {
    private Genre genre;
    private List<InListMoviveDTO> movieList;
}
