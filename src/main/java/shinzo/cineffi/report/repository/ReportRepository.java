package shinzo.cineffi.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
