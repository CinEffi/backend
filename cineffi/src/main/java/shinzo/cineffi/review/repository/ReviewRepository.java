package shinzo.cineffi.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.review.Review;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByUserId(Long userId);
    Page<Review> findAllByUserId(Long userId, Pageable pageable);
    Page<Review> findByMovie(Movie movie, Pageable pageable);
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Review> findAllByOrderByLikeNumDesc(Pageable pageable);
}
