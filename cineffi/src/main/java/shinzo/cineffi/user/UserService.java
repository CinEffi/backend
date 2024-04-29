package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import shinzo.cineffi.domain.dto.LoginRequestDTO;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.user.repository.UserAccountRepository;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserAccountRepository userAccountRepository;

}