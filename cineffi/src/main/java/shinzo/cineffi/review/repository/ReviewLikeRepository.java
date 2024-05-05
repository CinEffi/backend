package shinzo.cineffi.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.domain.entity.review.ReviewLike;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    ReviewLike findByReviewAndUser(Review review, User user);
    ReviewLike findByReviewAndUserId(Review review, Long userId);
    List<ReviewLike> findByReview(Review review);
}
