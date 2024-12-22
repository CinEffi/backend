package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import shinzo.cineffi.domain.entity.board.Post;

import java.time.LocalDateTime;

@Getter
public class GetPostDto {
    @Schema(description = "제목")
    private String title;

    @Schema(description = "내용")
    private String content;

    @Schema(description = "작성자")
    private UserDto user;

    @Schema(description = "조회수")
    private Integer view;

    @Schema(description = "좋아요 수")
    private Integer likeNumber;

    @Schema(description = "좋아요 여부 (비 로그인 시 항상 false)")
    private Boolean isLike;

    @Schema(description = "작성일시")
    @JsonFormat(pattern = "yyyy/MM/dd'T'hh:mm:ss")
    private LocalDateTime createdAt;

    public GetPostDto from(Post post, UserDto userDto, boolean isLike) {
        this.title = post.getTitle();
        this.content = post.getContent();
        this.user = userDto;
        this.view = post.getView();
        this.likeNumber = post.getLikeNumber();
        this.isLike = isLike;
        this.createdAt = post.getCreatedAt();

        return this;
    }
}
