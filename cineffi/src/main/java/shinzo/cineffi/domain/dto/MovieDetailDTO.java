package shinzo.cineffi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class MovieDetailDTO {
    private InMovieDetailDTO movie;
    private int runtime;
    private String introduction;
    private Float cinephileAvgScore;
    private Float levelAvgScore;
    private Float allAvgScore;
    private Float myScore;
    private Boolean isScrap;
    private Long existingReviewId;
    private String existingReviewContent;
    private List<CrewListDTO> crewList;

}
