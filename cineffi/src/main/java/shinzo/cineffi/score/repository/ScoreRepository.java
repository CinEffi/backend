package shinzo.cineffi.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;

import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    Score findByMovieAndUser(Movie movie, User user);

    Optional<Score> findByMovieIdAndUserId(Long movieId, Long userId);
}