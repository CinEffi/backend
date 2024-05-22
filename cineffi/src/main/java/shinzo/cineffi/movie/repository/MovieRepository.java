package shinzo.cineffi.movie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.enums.Genre;

import java.util.List;
import java.util.Optional;


public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTmdbId(int tmdbId);

    List<Movie> findAllByAvgScoreIdIsNull();


    @Query("SELECT m FROM Movie m WHERE YEAR(m.releaseDate) = :year")
    List<Movie> findAllByReleaseDate(Integer year);


    Optional<Movie> findByTitle(String title);
    Boolean existsMovieByTitle(String title);
    Boolean existsMovieByTmdbId(int tmdbId);
    Boolean existsMovieByKobisCode(String kobisCode);

    Optional<Movie> findByTmdbId(int tmdbId);
    Optional<Movie> findByKobisCode(String kobisCode);


    @Query("SELECT m FROM Movie m WHERE REPLACE(m.title, ' ', '') = REPLACE(:title, ' ', '')")
    List<Movie> findByTitleIgnoringSpaces(@Param("title") String title);




    @Query(value = "SELECT * FROM movie WHERE release_date > CURRENT_DATE ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Movie> findUpcomingList();

    @Query("SELECT m FROM Movie m JOIN m.genreList g WHERE g.genre = :genre ORDER BY RANDOM()")
    List<Movie> findGenreList(Genre genre, Pageable pageable);

    @Query("SELECT DISTINCT m FROM Movie m " +
            "LEFT JOIN FETCH m.director d " +
            "LEFT JOIN ActorMovie am ON m = am.movie " +
            "LEFT JOIN Actor a ON am.actor = a " +
            "WHERE LOWER(REPLACE(m.title, ' ', '')) LIKE '%' || LOWER(REPLACE(:q, ' ', '')) || '%' " +
            "OR LOWER(REPLACE(d.name, ' ', '')) LIKE '%' || LOWER(REPLACE(:q, ' ', '')) || '%' " +
            "OR LOWER(REPLACE(a.name, ' ', '')) LIKE '%' || LOWER(REPLACE(:q, ' ', '')) || '%'")
    List<Movie> findSearchList(String q, Pageable pageable);

}
