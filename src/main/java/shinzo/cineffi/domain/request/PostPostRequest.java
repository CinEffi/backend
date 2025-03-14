package shinzo.cineffi.domain.request;

import io.netty.channel.ChannelHandler;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class PostPostRequest {
    @Schema(description = "제목")
    private String title;

    @Schema(description = "내용(게시글의 HTML 문서 전체)")
    private String content;

    @Schema(description = "태그 목록")
    private List<String> tags;
}
