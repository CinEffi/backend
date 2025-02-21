package shinzo.cineffi.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import shinzo.cineffi.domain.dto.MovieDTO;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class MovieSearchRespon {
    private List<MovieDTO> movieList;

}
