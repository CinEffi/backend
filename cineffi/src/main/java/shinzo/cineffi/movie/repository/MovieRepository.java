package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.Movie;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTmdbId(int tmdbId);

    @Query(value = "SELECT * FROM movie WHERE release_date > CURRENT_DATE ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Movie> findUpcomingList();

}
