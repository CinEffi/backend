package shinzo.cineffi.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.dto.AuthRequestDTO;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.domain.entity.user.User;

import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserRepository;


@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuthService(UserAccountRepository userAccountRepository, UserRepository userRepository) {
        this.userAccountRepository = userAccountRepository;
        this.userRepository = userRepository;

    }


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
                .loginType(request.getLoginType())
                .password(BCrypt.hashpw(request.getPassword(),BCrypt.gensalt()))
                .Email(request.getEmail())
                .user(user)
                .build();

        userAccountRepository.save(userAccount);

        return true; //이메일 중복 없을 때는 true
    }

}
