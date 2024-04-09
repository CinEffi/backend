package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Actor;

public interface ActorRepository extends JpaRepository<Actor, Long> {
}
