package shinzo.cineffi.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDTO<?>> createReview (@RequestBody ReviewCreateDTO reviewCreateDTO) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ResponseDTO.builder()
                .result(reviewService.createReview(reviewCreateDTO, userId))
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @PostMapping("/{reviewId}/edit")
    public ResponseEntity<ResponseDTO<?>> updateReview (@PathVariable Long reviewId, @RequestBody ReviewUpdateDTO reviewUpdateDTO) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        reviewService.updateReview(reviewUpdateDTO, reviewId, userId);
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @DeleteMapping("/{reviewId}/delete")
    public ResponseEntity<ResponseDTO<?>> deleteReview(@PathVariable Long reviewId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @PostMapping("/{reviewId}/likes")
    public ResponseEntity<ResponseDTO<?>> likeReview (@PathVariable Long reviewId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ResponseDTO.builder()
                .result(reviewService.likeReview(reviewId, userId))
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @DeleteMapping("/{reviewId}/likes")
    public ResponseEntity<ResponseDTO<?>> unlikeReview (@PathVariable Long reviewId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return ResponseEntity.ok(ResponseDTO.builder()
                .result(reviewService.unlikeReview(reviewId, userId))
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<ResponseDTO<?>> getReviewsByMovieId(@PathVariable Long movieId
            , @PageableDefault(page = 0, size=10) Pageable pageable) {
        String string = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Long userId = string.equals("anonymousUser") ? null : Long.parseLong(string);
        return ResponseEntity.ok(ResponseDTO.builder()
                .result(reviewService.lookupReviewByMovie(movieId, pageable, userId))
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @GetMapping("/new")
    public ResponseEntity<ResponseDTO<?>> getReviewsByNew(
            @PageableDefault(page = 0, size=10) Pageable pageable) {
        // 여기서는 유저 정보가 아예 불필요할지 어쩔지 몰라서 우선 적고 전달은 해뒀습니다.
        String string = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Long userId = string.equals("anonymousUser") ? null : Long.parseLong(string);
        return ResponseEntity.ok(ResponseDTO.builder()
                .result(reviewService.sortReviewByNew(pageable, userId))
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }

    @GetMapping("/hot")
    public ResponseEntity<ResponseDTO<?>> getReviewsByHot(
            @PageableDefault(page = 0, size=10) Pageable pageable) {
        // 여기서는 유저 정보가 아예 불필요할지 어쩔지 몰라서 우선 적고 전달은 해뒀습니다.
        // 인기를 어떻게 측정할꺼니 // likeNum 이겠구나.
        //
        // 더 나가면, 분명 sort기준을 따로 만들어 줄수도 있지만 우선 간단하게 좋아요 수 만으로 정렬해 보자.
        String string = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Long userId = string.equals("anonymousUser") ? null : Long.parseLong(string);
        return ResponseEntity.ok(ResponseDTO.builder()
                .result(reviewService.sortReviewByHot(pageable, userId))
                .message(SuccessMsg.SUCCESS.getDetail()).build());
    }
}