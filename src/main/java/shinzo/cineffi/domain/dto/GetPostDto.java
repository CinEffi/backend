package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetPostDto {
    @Schema(description = "게시글 id")
    private String postId;

    @Schema(description = "제목")
    private String title;

    @Schema(description = "작성시간", example = "2024/11/11T09:51:01")
    @JsonFormat(pattern = "yyyy/MM/dd'T'hh:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "작성자 정보")
    private UserDto user;

    @Schema(description = "태그")
    private List<String> tags;

    @Schema(description = "좋아요 수")
    private Integer likeNumber;

    @Schema(description = "댓글 수")
    private Integer commentNumber;

    @Schema(description = "핫 게시글 여부")
    private Boolean isHotPost;
}
