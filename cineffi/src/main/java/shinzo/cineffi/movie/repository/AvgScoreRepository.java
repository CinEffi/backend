package shinzo.cineffi.movie.repository;

import org.antlr.v4.runtime.ListTokenSource;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.AvgScore;

import java.util.List;

public interface AvgScoreRepository extends JpaRepository<AvgScore, Long> {
}
