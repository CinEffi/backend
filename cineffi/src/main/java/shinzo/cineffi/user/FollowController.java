package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.SuccessMsg;

@RequiredArgsConstructor
@RestController
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우 하기
     * @param followReq
     * @return
     */
    @PostMapping("/api/users/follow")
    public ResponseEntity<ResponseDTO<?>> postFollow(@RequestBody FollowReq followReq) {
        Long targetUserId = followReq.getUserId();
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
        Long targetUserId = followReq.getUserId();
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
    public ResponseEntity<ResponseDTO<?>> getFollowerList(@PathVariable("user-id") Long targetUserId) {
        Long loginUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString()); // 쿠키가 없으면 null (필터에서 처리 필요)
        System.out.println(loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(followService.getFollowerList(targetUserId, loginUserId))
                        .build());

    }

    /**
     * 팔로우 리스트 조회
     * @param targetUserId
     * @return
     */
    @GetMapping("/api/users/{user-id}/followings")
    public ResponseEntity<ResponseDTO<?>> getFollowingList(@PathVariable("user-id") Long targetUserId) {
        Long loginUserId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(followService.getFollowingList(targetUserId, loginUserId))
                        .build());

    }
}
