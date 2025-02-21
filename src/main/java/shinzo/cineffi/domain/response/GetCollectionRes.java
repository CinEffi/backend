package shinzo.cineffi.domain.response;

import lombok.Builder;
import lombok.Data;
import shinzo.cineffi.domain.dto.ReviewDto;

import java.util.List;

@Data
@Builder
public class GetCollectionRes {
    private int totalCollectionNum;
    private List<ReviewDto> collection;
}
