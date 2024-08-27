package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.Director;

import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {

    Optional<Director> findByName(String name);

    @Query("SELECT d FROM Director d WHERE d.tmdbId = :tmdbId")
    Optional<Director> findByTmbdId(int tmdbId);

    @Query("SELECT d.id FROM Director d WHERE d.name = :name")
    Long idByName(String name);

    boolean existsByName(String name);
}
