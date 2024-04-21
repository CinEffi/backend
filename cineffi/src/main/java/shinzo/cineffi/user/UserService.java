package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import shinzo.cineffi.domain.dto.LoginRequestDTO;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.user.repository.UserAccountRepository;

@Controller
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserAccountRepository userAccountRepository;


    public boolean emailLogin(LoginRequestDTO request) {
        UserAccount userAccount = userAccountRepository.findByEmail(request.getEmail());
        return userAccount != null && userAccount.getPassword().equals(request.getPassword());
    }
}