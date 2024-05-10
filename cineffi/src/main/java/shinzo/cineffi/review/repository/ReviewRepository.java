package shinzo.cineffi.review.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByIdAndIsDeleteFalse(Long userId);

    Review findByMovieAndUserAndIsDeleteFalse(Movie movie, User user);
//    List<Review> findAllByUserIdAndIsDeleteFalse(Long userId);
//    Page<Review> findAllByUserIdAndIsDeleteFalse(Long userId, Pageable pageable);

//    Page<Review> findAllByUserIdAndIsDeleteFalseOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<Review> findAllByUserIdAndIsDeleteFalseOrderByCreatedAtDesc(Long userId);

    Page<Review> findByMovieAndIsDeleteFalseOrderById(Movie movie, Pageable pageable);

    Page<Review> findAllByIsDeleteFalseOrderByCreatedAtDesc(Pageable pageable);
    Page<Review> findAllByIsDeleteFalseOrderByLikeNumDesc(Pageable pageable);

    Optional<Object> findByIdAndUserId(Long reviewId, Long userId);
    Integer countByMovieAndIsDeleteFalse(Movie movie);
    Review findByMovieAndUserIdAndIsDeleteFalse(Movie movie, Long userId);
}
