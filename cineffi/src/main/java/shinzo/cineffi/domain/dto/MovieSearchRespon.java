package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class MovieSearchRespon {
    private List<MovieDTO> movieList;
    private int totalPageNum;

}
