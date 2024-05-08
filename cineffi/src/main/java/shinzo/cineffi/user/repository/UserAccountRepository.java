package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shinzo.cineffi.domain.entity.user.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long>,UserAccountRepositoryCustom {
    UserAccount findByEmail(String email);

    boolean existsByEmail(String email);
    @Query("SELECT ua FROM UserAccount ua JOIN ua.user u WHERE ua.email = :email AND u.isDelete = false")
    UserAccount findByEmailAndUserIsDeleted(@Param("email") String email);

}
