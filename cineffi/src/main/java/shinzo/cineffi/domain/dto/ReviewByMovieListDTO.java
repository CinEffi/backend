package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
@Data
@Builder
public class ReviewByMovieListDTO {
    private Long movieId;// 이게 안좋아서 movieId를 해야할 수도 있어.
    private Integer totalPageNum;
    private List<ReviewByMovieDTO> reviews;
}
