package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.LoginRequestDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;
import shinzo.cineffi.jwt.JWTUtil;
import shinzo.cineffi.jwt.JWToken;

import static shinzo.cineffi.jwt.JWTUtil.ACCESS_PERIOD;
import static shinzo.cineffi.jwt.JWTUtil.REFRESH_PERIOD;

@RequiredArgsConstructor
@RestController
public class UserController {
    private final UserService userService;

}
