package shinzo.cineffi.user;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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

import static shinzo.cineffi.exception.message.ErrorMsg.EMPTY_USER;


@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final ScrapService scrapService;
    private final ReportRepository reportRepository;


    /**
     * 유저 마이페이지 조회
     * @param userId
     * @return
     */
    @GetMapping("/api/users/{user-id}")

    public ResponseEntity<ResponseDTO<?>> getMyPage(@PathVariable("user-id") Long userId) {
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(userService.getMyPage(userId, loginUserId))
                        .build());
    }

    /**
     * 유저 (내) 프로필 조회
     *
     * @return
     */
    @GetMapping("/api/users/profile")
    public ResponseEntity<ResponseDTO<?>> getMyProfile() {
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
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
    public ResponseEntity<ResponseDTO<?>> editMyProfile(MultipartHttpServletRequest request) {
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");
        MultipartFile file = request.getFile("file");

        userService.editUserProfile(loginUserId, nickname, password, file);

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
    public ResponseEntity<ResponseDTO<?>> getReviewList(@PathVariable("user-id") Long userId,
                                                        @PageableDefault(page = 0, size=10) Pageable pageable) {

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(reviewService.getUserReviewList(userId, pageable))
                        .build());

    }

    /**
     * 유저 스크랩 목록 조회
     * @param userId
     * @return
     */
    @GetMapping("/api/users/{user-id}/scrap")
    public ResponseEntity<ResponseDTO<?>> getScrapList(@PathVariable("user-id") Long userId,
                                                       @PageableDefault(page = 0, size=10) Pageable pageable) {
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(scrapService.getUserScrapList(userId, loginUserId, pageable))
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
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());

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
