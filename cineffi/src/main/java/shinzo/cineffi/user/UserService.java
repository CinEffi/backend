package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import shinzo.cineffi.user.repository.UserAccountRepository;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserAccountRepository userAccountRepository;

}