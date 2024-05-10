package shinzo.cineffi.movie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.Scrap;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

//    List<Scrap> findAllByUserId(Long userId);
//    Page<Scrap> findAllByUserId(Long userId, Pageable pageable);

    List<Scrap> findAllByUserIdOrderByIdDesc(Long userId);

//    Page<Scrap> findAllByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    boolean existsByMovieIdAndUserId(Long movieId, Long userId);

    Optional<Scrap> findByMovieAndUser(Movie movie, User user);
}
