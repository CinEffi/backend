package shinzo.cineffi.domain.dto;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ReviewLookupListDTO {
    private List<ReviewLookupDTO> reviews;
    private Integer totalPageNum;
}
