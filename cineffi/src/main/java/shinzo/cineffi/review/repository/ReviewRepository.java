package shinzo.cineffi.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.review.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
