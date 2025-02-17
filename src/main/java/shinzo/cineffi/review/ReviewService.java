package shinzo.cineffi.review;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.MovieGenre;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.domain.entity.review.ReviewLike;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.entity.user.UserAnalysis;
import shinzo.cineffi.domain.enums.ScoreTypeEvent;
import shinzo.cineffi.domain.response.GetCollectionRes;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.MovieRepository;
import shinzo.cineffi.review.repository.ReviewLikeRepository;
import shinzo.cineffi.review.repository.ReviewRepository;
import shinzo.cineffi.score.ScoreService;
import shinzo.cineffi.score.repository.ScoreRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.*;

import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;
    private final ScoreService scoreService;
//    private final EncryptUtil encryptUtil;


    @Transactional(readOnly = true)
    public GetCollectionRes getUserReviewList(Long userId, Long loginUserId //, Pageable pageable) {
    ) {// 존재하는 유저인지 검증
        if(!userRepository.existsById(userId)) throw new CustomException(ErrorMsg.EMPTY_USER);
        List<Review> userCollection = reviewRepository.findAllByUserIdAndIsDeleteFalseOrderByCreatedAtDesc(userId);
//        Page<Review> userCollection = reviewRepository.findAllByUserIdAndIsDeleteFalseOrderByCreatedAtDesc(userId, pageable);

        List<ReviewDto> reviewList = new ArrayList<>();
        userCollection.forEach(review -> {
            // 평론에서 영화 객체 따로 빼놓기
            Movie movie = review.getMovie();
            // Dto 꾸리기
            reviewList.add(ReviewDto.builder()
                    .reviewId(review.getId())
                    .movieId(movie.getId())
                    .movieTitle(movie.getTitle())
                    .poster(decodeImage(movie.getPoster()))
                    .content(review.getContent())
                    .userScore(scoreRepository.findByMovieAndUser(movie, review.getUser()) == null ? null : scoreRepository.findByMovieAndUser(movie, review.getUser()).getScore()) // 타겟 유저가 해당 영화에 준 평점
                    .likeNumber(review.getLikeNum())
                    .isLiked(loginUserId == null ? false : reviewLikeRepository.findByReviewAndUserId(review, loginUserId) != null)
                    // 로그인 안했으면 false, 로그인 했으면 유저가 평론에 좋아요 눌렀는지 아닌지
                    .isMyReview(loginUserId == null ? false : !reviewRepository.findByIdAndUserId(review.getId(), loginUserId).isEmpty())
                    // 로그인 안했으면 false, 로그인 했으면 로그인 유저가 작성한 평론이 해당 평론이라면 true
                    .build());
        });
        return GetCollectionRes.builder()
                .totalCollectionNum(userCollection.size())
                .collection(reviewList)
                .build();
    }

    //평론 작성
    public Long createReview(ReviewCreateDTO reviewCreateDTO, Long userId) {

        // 리뷰를 생성하는 유저 찾기
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        // 리뷰 쓸 영화 조회, 찾기
        Movie movie = movieRepository.findById(reviewCreateDTO.getMovieId())
                .orElseThrow(() -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));

        if (reviewRepository.findByMovieAndUserAndIsDeleteFalse(movie, user) != null) throw new CustomException(ErrorMsg.REVIEW_EXIST);

        // 평론 생성 + DB에 저장하기
        Review createReview = Review.builder()
                .movie(movie)
                .user(user)
                .content(reviewCreateDTO.getContent())
                .build();
        Review review = reviewRepository.save(createReview);// userActivityNum 업데이트
        // 유저 통계 갱신하기
        for (MovieGenre genre : movie.getGenreList())
            user.getUserAnalysis().updateGenreTendency(genre.getGenre(), UserAnalysis.reviewPoint);
        user.getUserActivityNum().addCollectionNum();
        userRepository.save(user);
        return review.getId();
    }

    //  kill

    //평론 수정
    public void updateReview(ReviewUpdateDTO reviewUpdateDTO, Long reviewId, Long userId) {
        String content = reviewUpdateDTO.getContent();

        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        //수정할 리뷰 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        //리뷰 수정하는 유저찾기 + 권한
        if (!review.getUser().equals(user))
            throw new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER);
        reviewRepository.save(review.toBuilder().content(content).build());
    }

    //평론 삭제
    public void deleteReview(Long reviewId, Long userId) {
        //리뷰 삭제하는 유저찾기 + 권한
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        //삭제할 리뷰 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        //리뷰 삭제하는 유저찾기 + 권한
        if (!review.getUser().equals(user))
            throw new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER);
        reviewRepository.save(review.setDelete(true));//리뷰 삭제

        // review로 얻었던 통계 점수도 회수
        for (MovieGenre genre : review.getMovie().getGenreList())
            user.getUserAnalysis().updateGenreTendency(genre.getGenre(), -UserAnalysis.reviewPoint);

        // 리뷰에 딸린 모든 좋아요 삭제
        List<ReviewLike> reviewLikeList = reviewLikeRepository.findByReview(review);
        // 경험치도 뺏어가고 // 레벨도 낮춰주고

        user.addExp(-reviewLikeList.size());
        userRepository.save(user);

        reviewLikeRepository.deleteAll(reviewLikeList);
        user.getUserActivityNum().subCollectionNum();
        userRepository.save(user);
    }

    public Long likeReview(Long reviewId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        Review review = reviewRepository.findByIdAndIsDeleteFalse(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        if (reviewLikeRepository.findByReviewAndUser(review, user) != null)
            throw new CustomException(ErrorMsg.REVIEW_LIKE_EXIST);
        ReviewLike reviewLike = reviewLikeRepository.save(ReviewLike.builder().review(review).user(user).build());
        reviewRepository.save(review.addLikeNum());
        ScoreTypeEvent scoreTypeEvent = review.getUser().addExp(1);
        scoreService.scoreTypeRefresh(userId, scoreTypeEvent);
        return reviewLike.getId();
    }

    // 평론 좋아요 취소
    public Long unlikeReview(Long reviewId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        Review review = reviewRepository.findByIdAndIsDeleteFalse(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        ReviewLike reviewLike = reviewLikeRepository.findByReviewAndUser(review, user);
        if (reviewLike == null) throw new CustomException(ErrorMsg.REVIEW_LIKE_NOT_EXIST);
        reviewLikeRepository.delete(reviewLike);
        reviewRepository.save(review.subLikeNum());
        ScoreTypeEvent scoreTypeEvent = review.getUser().addExp(-1);
        scoreService.scoreTypeRefresh(userId, scoreTypeEvent);
        return reviewLike.getId();
    }

    public ReviewByMovieListDTO lookupReviewByMovie(Long movieId, Pageable pageable, Long myUserId) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(
                () -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));
        Page<Review> reviewPage = reviewRepository.findByMovieAndIsDeleteFalseOrderById(movie, pageable);
        List<ReviewByMovieDTO> reviewLookupDTOList = new ArrayList<>();
        for (Review review : reviewPage) {
            User user = review.getUser();
            Score score = scoreRepository.findByMovieAndUser(movie, user);

            ReviewByMovieDTO reviewByMovieDTO = ReviewByMovieDTO.builder()
                    .userId(EncryptUtil.LongEncrypt(user.getId()))
                    .reviewId(review.getId())
                    .isMyReview(myUserId != null ? myUserId == user.getId() : false)
                    .nickname(user.getNickname())
                    .level(user.getLevel())
                    .userProfileImage(decodeImage(user.getProfileImage()))
                    .isBad(user.getIsBad())
                    .isCertified(user.getIsCertified())
                    .content(review.getContent())
                    .score(score != null ? score.getScore() : null)
                    .likeNumber(review.getLikeNum())
                    .createdAt(review.getCreatedAt().toLocalDate())
                    .isLiked(myUserId != null ? reviewLikeRepository.findByReviewAndUserId(review, myUserId) != null : false)
                    .build();
            reviewLookupDTOList.add(reviewByMovieDTO);
        }
        return ReviewByMovieListDTO.builder()
                .movieId(movie.getId())
                .totalReviewNum(reviewRepository.countByMovieAndIsDeleteFalse(movie))
                .totalPageNum(reviewPage.getTotalPages())
                .reviews(reviewLookupDTOList).build();
    }

    public ReviewLookupListDTO sortReviewByNew(Pageable pageable, Long myUserId) {
        return lookupReviewList(reviewRepository.findAllByIsDeleteFalseOrderByCreatedAtDesc(pageable), myUserId);
    }

    public ReviewLookupListDTO sortReviewByHot(Pageable pageable, Long myUserId) {
//        return lookupReviewList(reviewRepository.findAllByIsDeleteFalseOrderByLikeNumDesc(pageable), myUserId);
        return lookupReviewList(reviewRepository.findAllByIsDeleteFalseAndLikeNumGreaterThanOrderByLikeNumDesc(pageable, 1), myUserId);
    }

    public ReviewLookupListDTO lookupReviewList(Page<Review> reviewPage, Long myUserId) {
        List<ReviewLookupDTO> reviewLookupDTOList = new ArrayList<>();
        for (Review review : reviewPage) {
            Movie movie = review.getMovie();
            User user = review.getUser();
            Score score = scoreRepository.findByMovieIdAndUserId(movie.getId(),user.getId());

            ReviewLookupDTO reviewLookupDTO = ReviewLookupDTO.builder()
                    .movieId(movie.getId())
                    .movieTitle(movie.getTitle())
                    .moviePoster(encodeImage(movie.getPoster()))
                    .reviewScore(score != null ? score.getScore() : null)
                    .reviewId(review.getId())
                    .reviewWriterId(EncryptUtil.LongEncrypt(user.getId()))
                    .reviewWriterNickname(user.getNickname())
                    .reviewContent(review.getContent())
                    .likeNumber(review.getLikeNum())
                    .isLiked(myUserId != null ? reviewLikeRepository.findByReviewAndUserId(review, myUserId) != null : false)
                    .isCertified(user.getIsCertified())
                    .isBad(user.getIsBad())
                    .level(user.getLevel())
                    .createdAt(review.getCreatedAt().toLocalDate()).build();
            reviewLookupDTOList.add(reviewLookupDTO);
        }
        return ReviewLookupListDTO.builder().reviews(reviewLookupDTOList)
                .totalPageNum(reviewPage.getTotalPages()).build();
    }

    public String encodeImage(byte[] imageData) {
        String baseImgStr = "data:image/png;base64,";
        String result = Base64.getEncoder().encodeToString(imageData);
        return baseImgStr + result;
    }

}
