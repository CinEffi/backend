package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.UserActivityNum;

public interface UserActivityNumRepository extends JpaRepository<UserActivityNum, Long> {
}
