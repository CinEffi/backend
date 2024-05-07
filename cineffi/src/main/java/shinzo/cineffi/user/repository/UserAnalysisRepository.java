package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.UserAnalysis;

public interface UserAnalysisRepository extends JpaRepository<UserAnalysis, Long> {}