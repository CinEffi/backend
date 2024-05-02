package shinzo.cineffi.domain.dto;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class InListMoviveDTO {
    private Long movieId;
    private String title;
    private LocalDate releaseDate;
    private byte[] poster;
    private Float levelAvgScore;
    private Float cinephileAvgScore;
}
