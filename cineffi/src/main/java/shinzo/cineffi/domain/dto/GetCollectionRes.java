package shinzo.cineffi.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GetCollectionRes {
    private int totalPageNum;
    private List<ReviewDto> collection;
}
