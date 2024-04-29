package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.Actor;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    Optional<Actor> findByName(String name);

    @Query("SELECT a FROM Actor a WHERE a.tmdbId = :tmdbId")
    Optional<Actor> findByTmdbId(int tmdbId);
}
