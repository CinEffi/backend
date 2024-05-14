package shinzo.cineffi.user;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.auth.AuthService;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.entity.user.Report;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;
import shinzo.cineffi.movie.ScrapService;
import shinzo.cineffi.report.repository.ReportRepository;
import shinzo.cineffi.review.ReviewService;
import shinzo.cineffi.user.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;

import static shinzo.cineffi.auth.AuthService.getLoginUserId;
import static shinzo.cineffi.exception.message.ErrorMsg.EMPTY_USER;
import static shinzo.cineffi.exception.message.ErrorMsg.NOT_LOGGED_IN;


@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final ScrapService scrapService;
    private final ReportRepository reportRepository;
    private final EncryptUtil encryptUtil;

    /**
     * 유저 마이페이지 조회
     * @param userId
     * @return
     */
    @GetMapping("/api/users/{user-id}")
    public ResponseEntity<ResponseDTO<?>> getMyPage(@PathVariable("user-id") String userId) {
        Long DecryptUserId= encryptUtil.LongDecrypt(userId);
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(userService.getMyPage(DecryptUserId, loginUserId))
                        .build());
    }

    /**
     * 유저 (내) 프로필 조회
     *
     * @return
     */
    @GetMapping("/api/users/profile")
    public ResponseEntity<ResponseDTO<?>> getMyProfile() {
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) throw new CustomException(NOT_LOGGED_IN);
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(userService.getMyProfileInfo(loginUserId))
                        .build());
    }

    /**
     * 유저 (내) 프로필 수정
     *
     * @param request
     * @return
     */
    @PostMapping("/api/users/profile/edit")
    public ResponseEntity<ResponseDTO<?>> editMyProfile(MultipartHttpServletRequest request) throws IOException {
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) throw new CustomException(NOT_LOGGED_IN);

        String nickname = request.getParameter("nickname") == null ? null : request.getParameter("nickname");
        String password = request.getParameter("password") == null ? null : request.getParameter("password");
        MultipartFile userProfileImage = request.getFile("userProfileImage") == null ? null : request.getFile("userProfileImage");

        userService.editUserProfile(loginUserId, nickname, password, userProfileImage);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build());

    }

    /**
     * 유저 컬렉션 조회
     * @param userId
     * @return
     */
    @GetMapping("api/users/{user-id}/reviews")
    public ResponseEntity<ResponseDTO<?>> getReviewList(@PathVariable("user-id") String userId
//            , @PageableDefault(page = 0, size=10) Pageable pageable
    ) {
        Long DecryptUserId= encryptUtil.LongDecrypt(userId);
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(reviewService.getUserReviewList(DecryptUserId, loginUserId))//, pageable ))
                        .build());
    }

    /**
     * 유저 스크랩 목록 조회
     * @param userId
     * @return
     */
    @GetMapping("/api/users/{user-id}/scrap")
    public ResponseEntity<ResponseDTO<?>> getScrapList(@PathVariable("user-id") String userId
//            , @PageableDefault(page = 0, size=10) Pageable pageable
    ) {

        Long DecryptUserId= encryptUtil.LongDecrypt(userId);
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(scrapService.getUserScrapList(DecryptUserId, loginUserId))//, pageable))
                        .build()
        );
    }

    /**
     * 유저 신고
     * @param request
     * @return
     */
    @PostMapping("/api/users/report")
    public ResponseEntity<ResponseDTO<?>> postReport(MultipartHttpServletRequest request) {
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) throw new CustomException(NOT_LOGGED_IN);

        Long reportedUserId = Long.parseLong(request.getParameter("userId"));
        String reportReason = request.getParameter("reportReason");
        MultipartFile file = request.getFile("file");

        Optional<User> reported = userRepository.findById(reportedUserId);
        if (reported.isEmpty()) throw new CustomException(EMPTY_USER);
        Optional<User> loginUser = userRepository.findById(loginUserId);
        if (loginUser.isEmpty()) throw new CustomException(EMPTY_USER);


        try {
            Report savedReport = reportRepository.save(
                    Report.builder()
                            .reportReason(reportReason)
                            .evidenceImage(file.getBytes())
                            .reported(reported.get())
                            .reporter(loginUser.get())
                            .build()
            );

            return ResponseEntity.ok(
                    ResponseDTO.builder()
                            .message(SuccessMsg.SUCCESS.getDetail())
                            .result(savedReport.getId())
                            .build()
                    );
        } catch (IOException e) {
            throw new CustomException(ErrorMsg.FAIDED_TO_CONVERT_IMAGE);
        }
    }
}
