package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.UserProfile;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
