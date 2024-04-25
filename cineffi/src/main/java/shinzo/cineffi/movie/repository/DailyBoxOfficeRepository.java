package shinzo.cineffi.movie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.movie.DailyBoxOffice;

public interface DailyBoxOfficeRepository extends JpaRepository<DailyBoxOffice, Long> {
}
