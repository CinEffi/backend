package shinzo.cineffi.domain.response;

import lombok.Getter;
import shinzo.cineffi.domain.dto.GetPostsDto;

@Getter
public class GetPostsRes {
    PageResponse<GetPostsDto> postList;
}
