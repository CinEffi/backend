package shinzo.cineffi.movie.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.enums.Genre;

import java.util.List;
import java.util.Optional;


public interface MovieRepository extends JpaRepository<Movie, Long> {
    boolean existsByTmdbId(int tmdbId);


    Optional<Movie> findByTitle(String title);


    @Query("SELECT m FROM Movie m WHERE REPLACE(m.title, ' ', '') = REPLACE(:title, ' ', '')")
    List<Movie> findByTitleIgnoringSpaces(@Param("title") String title);




    @Query(value = "SELECT * FROM movie WHERE release_date > CURRENT_DATE ORDER BY RANDOM() LIMIT 20", nativeQuery = true)
    List<Movie> findUpcomingList();

    @Query("SELECT m FROM Movie m JOIN m.genreList g WHERE g.genre = :genre ORDER BY RANDOM()")
    List<Movie> findGenreList(Genre genre, Pageable pageable);

}
