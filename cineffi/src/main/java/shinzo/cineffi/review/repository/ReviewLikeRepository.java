package shinzo.cineffi.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.review.ReviewLike;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
}
