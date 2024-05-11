package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetCollectionRes {
    private int totalCollectionNum;
    private List<ReviewDto> collection;
}
