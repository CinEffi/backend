package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.Actor;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, Long> {

    Optional<Actor> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT a.id FROM Actor a WHERE a.name = :name")
    Long idByName(String name);

}