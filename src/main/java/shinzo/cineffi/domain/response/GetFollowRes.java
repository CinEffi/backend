package shinzo.cineffi.domain.response;

import lombok.Builder;
import lombok.Data;
import shinzo.cineffi.domain.dto.FollowDto;

import java.util.List;

@Data
@Builder
public class GetFollowRes {
    private int totalPageNum;
    private List<FollowDto> followList;
}