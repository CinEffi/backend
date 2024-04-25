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


    public boolean emailLogin(LoginRequestDTO request) {
        UserAccount userAccount = userAccountRepository.findByEmail(request.getEmail());
        return BCrypt.checkpw(request.getPassword(), userAccount.getPassword());
    }



    public Long getUserIdByEmail(String email) {
        Optional<UserAccount> user = Optional.ofNullable(userAccountRepository.findByEmail(email));
        return user.map(UserAccount::getId).orElse(null);
    }

    public void normalLoginRefreshToken(Long memberNo, String refreshToken) {
        UserAccount userAccount = userAccountRepository.getReferenceById(memberNo);
        userAccount.changeToken(refreshToken);
    }
}