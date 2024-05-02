package shinzo.cineffi.review;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.dto.GetCollectionRes;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.review.repository.ReviewRepository;
import shinzo.cineffi.domain.dto.ReviewDto;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.crypto.codec.Utf8.decode;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;


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
                            .userScore(3) // score 엔티티 생성되면 수정 필요
                            .likeNumber(review.getLikeNum())
                            .build());
        });
        return GetCollectionRes.builder()
                .totalPageNum(totalPageNum)
                .collection(reviewList)
                .build();
    }
}
