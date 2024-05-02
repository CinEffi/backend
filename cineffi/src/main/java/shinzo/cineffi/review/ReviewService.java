package shinzo.cineffi.review;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.domain.entity.review.ReviewLike;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.MovieRepository;
import shinzo.cineffi.review.repository.ReviewLikeRepository;
import shinzo.cineffi.review.repository.ReviewRepository;
import shinzo.cineffi.score.repository.ScoreRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.crypto.codec.Utf8.decode;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final ScoreRepository scoreRepository;

    public GetCollectionRes getUserReviewList(Long userId, Pageable pageable) {
        Page<Review> userCollection = reviewRepository.findAllByUserId(userId, pageable);
        int totalPageNum = userCollection.getTotalPages();


        List<ReviewDto> reviewList = new ArrayList<>();
        userCollection.forEach(review -> {
                    Movie movie = review.getMovie();

             reviewList.add(ReviewDto.builder()
                            .reviewId(review.getId())
                            .movieId(movie.getId())
                            .movieTitle(movie.getTitle())
                            .poster(decode(movie.getPoster()))
                            .content(review.getContent())
                            .userScore(3) // score 엔티티 생성되면 수정 필요 음.. null이면 표시해주길 원한다고 하셨는데
                            .likeNumber(review.getLikeNum())
                            .build());
        });
        return GetCollectionRes.builder()
                .totalPageNum(totalPageNum)
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
        // 평론 생성 + DB에 저장하기

        Review createReview = Review.builder()
                .movie(movie)
                .user(user)
                .content(reviewCreateDTO.getContent())
                .build();
        Review review = reviewRepository.save(createReview);
        return review.getId();
    }

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
        review.setContent(content);
        reviewRepository.save(review);
    }

    //평론 삭제
    public void deleteReview(Long reviewId, Long userId) {
        //리뷰 삭제하는 유저찾기 + 권한 (추루 JWT 구현되면 추가)
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        //삭제할 리뷰 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        //리뷰 삭제하는 유저찾기 + 권한
        if (!review.getUser().equals(user))
            throw new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER);
        //리뷰 삭제
        reviewRepository.delete(review);
    }

    //해당 영화의 평론목록 조회
    public Long likeReview(Long reviewId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        if (reviewLikeRepository.findByReviewAndUser(review, user) != null)
            throw new CustomException(ErrorMsg.REVIEW_LIKE_EXIST);
        ReviewLike reviewLike = reviewLikeRepository.save(ReviewLike.builder().review(review).user(user).build());
        reviewRepository.save(review.addLikeNum());
        return reviewLike.getId();
    }

    public Long unlikeReview(Long reviewId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorMsg.REVIEW_NOT_FOUND));
        ReviewLike reviewLike = reviewLikeRepository.findByReviewAndUser(review, user);
        if (reviewLike == null) throw new CustomException(ErrorMsg.REVIEW_LIKE_NOT_EXIST);
        reviewLikeRepository.delete(reviewLike);
        reviewRepository.save(review.subLikeNum());
        return reviewLike.getId();
    }



    public ReviewByMovieListDTO lookupReviewByMovie(Long movieId, Pageable pageable, Long myUserId) {

        Movie movie = movieRepository.findById(movieId).orElseThrow(
                () -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));
        Page<Review> reviewPage = reviewRepository.findByMovie(movie, pageable);
        List<ReviewByMovieDTO> reviewLookupDTOList = new ArrayList<>();
        for (Review review : reviewPage) {
            User user = review.getUser();
            Score score = scoreRepository.findByMovieAndUser(movie, user);

            ReviewByMovieDTO reviewByMovieDTO = ReviewByMovieDTO.builder()
                    .reviewId(review.getId())
                    .isMyReview(myUserId != null ? myUserId == user.getId() : false)
                    .nickname(user.getNickname())
                    .level(user.getLevel())
                    .userProfileImage(user.getProfileImage())
                    .isBad(user.getIsBad())
                    .isCertified(user.getIsCertified())
                    .content(review.getContent())
                    .score(score != null ? score.getScore() : null)
                    .likeNumber(review.getLikeNum())
                    .createdAt(review.getCreatedAt())
                    .isLiked(myUserId != null ? reviewLikeRepository.findByReviewAndUserId(review, myUserId) != null : false)
                    .build();
            reviewLookupDTOList.add(reviewByMovieDTO);
        }
        return ReviewByMovieListDTO.builder()
                .movieId(movie.getId())
                .totalPageNum(reviewPage.getTotalPages())
                .reviews(reviewLookupDTOList).build();
    }

    public ReviewLookupListDTO sortReview(Pageable pageable, Long myUserId, Sort sort) {
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<Review> reviewPage = reviewRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<ReviewLookupDTO> reviewLookupDTOList = new ArrayList<>();
        for (Review review : reviewPage) {
            Movie movie = review.getMovie();
            User user = review.getUser();
            ReviewLookupDTO reviewLookupDTO = ReviewLookupDTO.builder()
                    .movieId(movie.getId())
                    .movieTitle(movie.getTitle())
                    .moviePoster(movie.getPoster())
                    .reviewId(review.getId())
                    .reviewWriterId(user.getId())
                    .reviewWriterNickname(user.getNickname())
                    .reviewContent(review.getContent())
                    .likeNumber(review.getLikeNum()).build();
            reviewLookupDTOList.add(reviewLookupDTO);
        }
        return ReviewLookupListDTO.builder()
                .reviews(reviewLookupDTOList)
                .totalPageNum(reviewPage.getTotalPages()).build();
    }
    public ReviewLookupListDTO sortReviewByNew(Pageable pageable, Long myUserId) {
        return lookupReviewList(reviewRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    public ReviewLookupListDTO sortReviewByHot(Pageable pageable, Long myUserId) {
        return lookupReviewList(reviewRepository.findAllByOrderByLikeNumDesc(pageable));
    }

    public ReviewLookupListDTO lookupReviewList(Page<Review> reviewPage) {
        List<ReviewLookupDTO> reviewLookupDTOList = new ArrayList<>();
        for (Review review : reviewPage) {
            Movie movie = review.getMovie();
            User user = review.getUser();
            ReviewLookupDTO reviewLookupDTO = ReviewLookupDTO.builder()
                    .movieId(movie.getId())
                    .movieTitle(movie.getTitle())
                    .moviePoster(movie.getPoster())
                    .reviewId(review.getId())
                    .reviewWriterId(user.getId())
                    .reviewWriterNickname(user.getNickname())
                    .reviewContent(review.getContent())
                    .likeNumber(review.getLikeNum()).build();
            reviewLookupDTOList.add(reviewLookupDTO);
        }
        return ReviewLookupListDTO.builder()
                .reviews(reviewLookupDTOList)
                .totalPageNum(reviewPage.getTotalPages()).build();
    }
}
