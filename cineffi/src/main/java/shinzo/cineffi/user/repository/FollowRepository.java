package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.Follow;

public interface FollowRepository extends JpaRepository<Follow, Long> {
}
