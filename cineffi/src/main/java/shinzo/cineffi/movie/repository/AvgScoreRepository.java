package shinzo.cineffi.movie.repository;

import org.antlr.v4.runtime.ListTokenSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.movie.AvgScore;

import java.util.List;
import java.util.Optional;

public interface AvgScoreRepository extends JpaRepository<AvgScore, Long> {

    @Query("SELECT a FROM AvgScore a WHERE a.id = :movidId")
    Optional<AvgScore> findByMovieId(Long movidId);
}
