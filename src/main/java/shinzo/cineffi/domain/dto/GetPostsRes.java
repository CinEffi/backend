package shinzo.cineffi.domain.dto;

import lombok.Getter;

@Getter
public class GetPostsRes {
    PageResponse<GetPostsDto> postList;
}
