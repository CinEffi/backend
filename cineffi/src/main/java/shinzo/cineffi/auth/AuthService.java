package shinzo.cineffi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import shinzo.cineffi.domain.dto.AuthRequestDTO;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.domain.entity.user.User;

import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
@ResponseBody
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;

    public boolean authUser(AuthRequestDTO request) {
        boolean isEmailDuplicate = userAccountRepository.existsByEmail(request.getEmail());
        if (isEmailDuplicate) {
            return false; // 이메일 중복 시 false 반환
        }
        //isNickName 설정해야함
        User user = User.builder()
                .build();
        userRepository.save(user);

        UserAccount userAccount = UserAccount.builder()
                .password(BCrypt.hashpw(request.getPassword(),BCrypt.gensalt()))
                .email(request.getEmail())
                .user(user)
                .build();

        userAccountRepository.save(userAccount);

        return true; //이메일 중복 없을 때는 true
    }

}
