package shinzo.cineffi.movie.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinzo.cineffi.domain.entity.movie.Director;

import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, Long> {

    @Transactional
    Optional<Director> findByName(@Param("name") String name);

    @Query("SELECT d.id FROM Director d WHERE d.name = :name")
    Optional<Long> findIdByName(String name);

    boolean existsByName(String name);
}
