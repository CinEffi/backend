package shinzo.cineffi.movie.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.enums.Genre;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Transactional
    List<Movie> findAllByAvgScoreIdIsNull();

    @Transactional
    Boolean existsByTitleAndReleaseDate(String title, LocalDate releaseDate);

    @Transactional
    Optional<Movie> findByTitleAndReleaseDate(String title, LocalDate releaseDate);

    @Transactional
    Optional<Movie> findByTmdbId(Long tmdbId);




    @Transactional
    @Query(value = "SELECT * FROM movie WHERE release_date > CURRENT_DATE ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Movie> findUpcomingList();

    @Transactional
    @Query("SELECT m FROM Movie m JOIN m.genreList g WHERE g.genre = :genre ORDER BY RANDOM()")
    List<Movie> findGenreList(Genre genre, Pageable pageable);

    @Transactional
    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN FETCH m.director d " +
            "LEFT JOIN ActorMovie am ON m = am.movie " +
            "LEFT JOIN Actor a ON am.actor = a " +
            "WHERE LOWER(REPLACE(m.title, ' ', '')) LIKE '%' || LOWER(REPLACE(:q, ' ', '')) || '%' " +
            "OR LOWER(REPLACE(d.name, ' ', '')) LIKE '%' || LOWER(REPLACE(:q, ' ', '')) || '%' " +
            "OR LOWER(REPLACE(a.name, ' ', '')) LIKE '%' || LOWER(REPLACE(:q, ' ', '')) || '%'")
    List<Movie> findSearchList(String q, Pageable pageable);

}
