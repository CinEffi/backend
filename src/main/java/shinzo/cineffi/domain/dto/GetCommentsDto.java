package shinzo.cineffi.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.entity.board.Comment;
import shinzo.cineffi.domain.entity.user.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GetCommentsDto {
    @Schema(description = "댓글 id")
    private String commentId;

    @Schema(description = "작성자")
    private UserDto user;

    @Schema(description = "내용")
    private String content;

    @Schema(description = "작성일시")
    @JsonFormat(pattern = "yyyy/MM/dd'T'hh:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "좋아요 수")
    private Integer likeNumber;

    public GetCommentsDto from(Comment comment, UserDto userDto) {
        this.commentId = EncryptUtil.LongEncrypt(comment.getId());
        this.user = userDto;
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        this.likeNumber = comment.getLikeNumber();

        return this;
    }
}
