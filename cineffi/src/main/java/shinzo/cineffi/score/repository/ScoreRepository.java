package shinzo.cineffi.score.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.Scrap;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findAllByMovie(Movie movie);
    Score findByMovieAndUser(Movie movie, User user);

    Score findByMovieIdAndUserId(Long movieId, Long userId);

    List<Score> findAllByUserId(Long userId);
}