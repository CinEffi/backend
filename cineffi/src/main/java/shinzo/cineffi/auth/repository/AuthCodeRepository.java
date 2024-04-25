package shinzo.cineffi.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.AuthCode;

import java.util.Optional;

public interface AuthCodeRepository extends JpaRepository<AuthCode, Long>  {
    Optional<AuthCode> findByEmailAndCode(String email, int code);
}
