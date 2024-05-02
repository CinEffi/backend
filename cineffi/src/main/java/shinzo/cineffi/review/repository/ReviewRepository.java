package shinzo.cineffi.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.review.Review;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserId(Long userId);
    Page<Review> findAllByUserId(Long userId, Pageable pageable);
}
