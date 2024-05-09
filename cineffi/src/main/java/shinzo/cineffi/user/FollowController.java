package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.FollowReq;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.SuccessMsg;

@RequiredArgsConstructor
@RestController
public class FollowController {

    private final FollowService followService;
    private final EncryptUtil encryptUtil;
    /**
     * 팔로우 하기
     * @param followReq
     * @return
     */
    @PostMapping("/api/users/follow")
    public ResponseEntity<ResponseDTO<?>> postFollow(@RequestBody FollowReq followReq) {

        String EncryptTargetUserId = followReq.getUserId();
        Long targetUserId = encryptUtil.LongDecrypt(EncryptTargetUserId);
        Long loginUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        followService.followUser(targetUserId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build()
        );
    }

    /**
     * 팔로우 취소
     * @param followReq
     * @return
     */
    @DeleteMapping("/api/users/follow")
    public ResponseEntity<ResponseDTO<?>> deleteFollow(@RequestBody FollowReq followReq) {
        String EncryptTargetUserId = followReq.getUserId();
        Long targetUserId = encryptUtil.LongDecrypt(EncryptTargetUserId);
        Long loginUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        followService.unfollowUser(targetUserId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build()
        );
    }

    /**
     * 팔로워 리스트 조회
     * @param targetUserId
     * @return
     */
    @GetMapping("/api/users/{user-id}/followers")
    public ResponseEntity<ResponseDTO<?>> getFollowerList(@PathVariable("user-id") String targetUserId,
                                                          @PageableDefault(page = 0, size=10) Pageable pageable) {
        Long loginUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()); // 쿠키가 없으면 null (필터에서 처리 필요)
        Long DecryptTargetUserId = encryptUtil.LongDecrypt(targetUserId);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(followService.getFollowerList(DecryptTargetUserId, loginUserId, pageable))
                        .build());

    }

    /**
     * 팔로우 리스트 조회
     * @param targetUserId
     * @return
     */
    @GetMapping("/api/users/{user-id}/followings")
    public ResponseEntity<ResponseDTO<?>> getFollowingList(@PathVariable("user-id") String targetUserId,
                                                           @PageableDefault(page = 0, size=10) Pageable pageable) {
        Long loginUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        Long DecryptTargetUserId = encryptUtil.LongDecrypt(targetUserId);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(followService.getFollowingList(DecryptTargetUserId, loginUserId, pageable))
                        .build());

    }
}
