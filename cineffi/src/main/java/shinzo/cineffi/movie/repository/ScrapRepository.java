package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.Scrap;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
}
