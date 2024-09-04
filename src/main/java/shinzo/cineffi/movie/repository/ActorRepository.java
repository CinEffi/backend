package shinzo.cineffi.movie.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Actor;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    @Transactional
    Optional<Actor> findByTmdbId(Long tmdbId);

}