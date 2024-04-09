package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
}
