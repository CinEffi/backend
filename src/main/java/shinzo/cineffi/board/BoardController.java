package shinzo.cineffi.board;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.request.PostCommentRequest;
import shinzo.cineffi.domain.request.PostPostRequest;
import shinzo.cineffi.exception.CustomException;

import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.List;

import static shinzo.cineffi.auth.AuthService.getLoginUserId;
import static shinzo.cineffi.exception.message.ErrorMsg.NOT_LOGGED_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "게시판")
@Slf4j
public class BoardController {
    private final BoardService boardService;

    @Operation(summary = "게시글 목록 조회 API")
    @GetMapping("/posts")
    public ResponseEntity<ResponseDTO<?>> getPostList(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boardService.getPostList(pageable))
                        .build());
    }

    @Operation(summary = "게시글 상세 조회 API")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResponseDTO<?>> getPost(@PathVariable("postId") String encryptedPostId) {
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        Long postId = EncryptUtil.LongDecrypt(encryptedPostId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boardService.getPost(postId, loginUserId))
                        .build());
    }

    @Operation(summary = "게시글 등록 API")
    @PostMapping("/posts")
    public ResponseEntity<ResponseDTO<?>> postPost(@RequestBody PostPostRequest postPostRequest) {
        // 로그인 유저 정보
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null)
            throw new CustomException(NOT_LOGGED_ID);

        String title = postPostRequest.getTitle();
        String content = postPostRequest.getContent();
        List<String> tags = postPostRequest.getTags();

        // 저장
        boardService.submitPost(loginUserId, title, content, tags);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build());
    }


    @ApiResponse(description = "result가 true이면 좋아요 처리, false이면 좋아요 취소 처리된 것입니다.")
    @Operation(summary = "게시글 좋아요 API", description = "좋아요가 되어있으면 취소, 안되어있으면 좋아요")
    @PostMapping("/posts/{postId}/likes")
    public ResponseEntity<ResponseDTO<?>> postPostLike(@PathVariable("postId") String encryptedPostId) {
        // 게시글 정보
        Long postId = EncryptUtil.LongDecrypt(encryptedPostId);

        // 로그인 유저 정보
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null)
            throw new CustomException(NOT_LOGGED_ID);

        // 좋아요 작업
        boolean result = boardService.switchPostLike(postId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(result)
                        .build());
    }


    @Operation(summary = "게시글 삭제 API")
    @PatchMapping("/posts/{postId}")
    public ResponseEntity<ResponseDTO<?>> patchPost(@PathVariable("postId") String encryptedPostId) {
        // 게시글 정보
        Long postId = EncryptUtil.LongDecrypt(encryptedPostId);

        // 로그인 유저 정보
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null)
            throw new CustomException(NOT_LOGGED_ID);

        // 삭제
        boardService.removePost(postId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build());
    }

    @Operation(summary = "핫 게시글 조회 API")
    @GetMapping("/posts/hot")
    public ResponseEntity<ResponseDTO<?>> getHotPosts (@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boardService.getHotPostList(pageable))
                        .build());

    }

    @Operation(summary = "댓글 목록 조회 API")
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

    @Operation(summary = "댓글 등록 API")
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

    @ApiResponse(description = "result가 true이면 좋아요 처리, false이면 좋아요 취소 처리된 것입니다.")
    @Operation(summary = "댓글 좋아요 API")
    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<ResponseDTO<?>> postCommentLike(@PathVariable("commentId") String encryptedCommentId) {
        // 게시글 정보
        Long commentId = EncryptUtil.LongDecrypt(encryptedCommentId);

        // 로그인 유저 정보
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null)
            throw new CustomException(NOT_LOGGED_ID);

        // 좋아요 작업
        boolean result = boardService.switchCommentLike(commentId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(result)
                        .build());
    }

    @Operation(summary = "댓글 삭제 API")
    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<ResponseDTO<?>> patchComment(@PathVariable("commentId") String encryptCommentId) {
        // 게시글 정보
        Long commentId = EncryptUtil.LongDecrypt(encryptCommentId);

        // 로그인 유저 정보
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null)
            throw new CustomException(NOT_LOGGED_ID);

        // 삭제
        boardService.removeComment(commentId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build());
    }
}
