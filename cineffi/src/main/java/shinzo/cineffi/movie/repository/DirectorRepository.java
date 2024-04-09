package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Director;

public interface DirectorRepository extends JpaRepository<Director, Long> {
}
