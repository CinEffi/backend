package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.AvgScore;

public interface AvgScoreRepository extends JpaRepository<AvgScore, Long> {
}
