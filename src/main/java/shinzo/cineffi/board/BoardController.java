package shinzo.cineffi.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.board.Post;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "게시판")
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "게시글 목록 조회 API", description = "게시판의 게시글 목록을 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<ResponseDTO<?>> getPostList(@ParameterObject Pageable pageable) {
        PageResponse<GetPostsDto> postList = boardService.getPostList(pageable);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(postList)
                        .build());
    }

    @Operation(summary = "게시글 상세 조회")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResponseDTO<?>> getPost(@PathVariable("postId") String encryptedPostId) {
        Long postId = EncryptUtil.LongDecrypt(encryptedPostId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boardService.getPost(postId))
                        .build());
    }

    @Operation(summary = "댓글 목록 조회")
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<ResponseDTO<?>> getComments(
            @PathVariable("postId") String encryptedPostId,
            @ParameterObject Pageable pageable) {
        Long postId = EncryptUtil.LongDecrypt(encryptedPostId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boardService.getCommentList(postId, pageable))
                        .build());
    }
}
