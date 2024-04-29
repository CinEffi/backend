package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinzo.cineffi.domain.entity.movie.Movie;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTmdbId(int tmdbId);


    Optional<Movie> findByTitle(String title);


    @Query("SELECT m FROM Movie m WHERE REPLACE(m.title, ' ', '') = REPLACE(:title, ' ', '')")
    List<Movie> findByTitleIgnoringSpaces(@Param("title") String title);



}
