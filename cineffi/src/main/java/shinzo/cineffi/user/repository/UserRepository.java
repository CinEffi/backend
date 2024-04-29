package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByNickname(String nickname);
}
