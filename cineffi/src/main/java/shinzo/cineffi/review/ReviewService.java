package shinzo.cineffi.review;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.review.repository.ReviewRepository;
import shinzo.cineffi.user.GetReviewRes;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.crypto.codec.Utf8.decode;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;


    public List<GetReviewRes> getUserReviewList(Long userId) {
        List<GetReviewRes> getReviewResList = new ArrayList<>();
        reviewRepository.findAllByUserId(userId).forEach(review -> {

                    Movie movie = review.getMovie();
                    getReviewResList.add(GetReviewRes.builder()
                            .reviewId(review.getId())
                            .movieId(movie.getId())
                            .movieTitle(movie.getTitle())
                            .poster(decode(movie.getPoster()))
                            .content(review.getContent())
                            .userScore(3) // score 엔티티 생성되면 수정 필요
                            .likeNumber(review.getLikeNum())
                            .build());
        });
        return getReviewResList;
    }
}
