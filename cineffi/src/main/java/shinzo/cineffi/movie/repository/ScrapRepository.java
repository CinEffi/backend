package shinzo.cineffi.movie.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Scrap;

import java.util.List;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    List<Scrap> findAllByUserId(Long userId);
    Page<Scrap> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByMovieIdAndUserId(Long movieId, Long userId);
}
