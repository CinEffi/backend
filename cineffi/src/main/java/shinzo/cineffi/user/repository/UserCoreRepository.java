package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.UserCore;

public interface UserCoreRepository extends JpaRepository<UserCore, Long> {
}
