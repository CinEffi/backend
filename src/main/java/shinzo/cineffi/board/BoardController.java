package shinzo.cineffi.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.response.PageResponse;
import shinzo.cineffi.domain.request.PostCommentRequest;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

import static shinzo.cineffi.auth.AuthService.getLoginUserId;
import static shinzo.cineffi.exception.message.ErrorMsg.NOT_LOGGED_ID;

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

    @Operation(summary = "")
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<ResponseDTO<?>> postComment(
            @PathVariable("postId") String encryptedPostId,
            @RequestBody PostCommentRequest postCommentRequest) {
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null)
            throw new CustomException(NOT_LOGGED_ID);

        Long postId = EncryptUtil.LongDecrypt(encryptedPostId);
        String content = postCommentRequest.getContent();
        boardService.postPostComment(postId, loginUserId, content);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build());
    }
}
