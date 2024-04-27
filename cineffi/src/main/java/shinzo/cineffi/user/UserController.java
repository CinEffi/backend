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


    @PostMapping("/api/auth/login/email")
    public ResponseEntity<ResponseDTO<String>> emailLogin(@RequestBody LoginRequestDTO request){
    Long userId = userService.getUserIdByEmail(request.getEmail());
    boolean LoginSuccess = userService.emailLogin(request);

    if(LoginSuccess) {
        JWToken jwToken = JWTUtil.allocateToken(userId,"ROLE_USER");//액세스 토큰 발급
        //Access 토큰 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("access",jwToken.getAccessToken())
                .sameSite("None")
                .maxAge(ACCESS_PERIOD)
                .path("/")
                .httpOnly(true)
                .build();
        //Refresh 토큰 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refresh",jwToken.getRefreshToken())
                .sameSite("None")
                .maxAge(REFRESH_PERIOD)
                .path("/")
                .httpOnly(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        userService.normalLoginRefreshToken(userId, jwToken.getRefreshToken());
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .isSuccess(true)
                .message(SuccessMsg.SUCCESS.getDetail())
                .build();
        return ResponseEntity.ok().headers(headers).body(responseDTO);
    }
    else {
        return ResponseEntity.status(ErrorMsg.PASSWORD_INCORRECT_MISMATCH.getHttpStatus())
                .body(ResponseDTO.<String>builder()
                .isSuccess(false)
                .message(ErrorMsg.PASSWORD_INCORRECT_MISMATCH.getDetail())
                .build());
    }
    }
}
