package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.user.UserCore;

import java.util.Optional;

public interface UserCoreRepository extends JpaRepository<UserCore, Long> {
    @Query("SELECT uc FROM UserCore uc JOIN uc.userProfile up JOIN uc.userAccount ua WHERE up.name = ?1 AND ua.phoneNumber = ?2")
    Optional<UserCore> findByKakaoProfile (String name, String phone);
}
