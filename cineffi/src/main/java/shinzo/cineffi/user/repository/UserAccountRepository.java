package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.UserAccount;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>,UserAccountRepositoryCustom {
    UserAccount findByEmail(String email);

    boolean existsByEmail(String email);


}
