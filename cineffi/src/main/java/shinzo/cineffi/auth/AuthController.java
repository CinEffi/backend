package shinzo.cineffi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shinzo.cineffi.domain.dto.KakaoToken;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/api/auth/login/kakao")
    public KakaoToken getKakaoToken(@RequestParam final String code){
        //인가코드로 엑세스 코드 발급
        KakaoToken kakaoToken = authService.getAccessToken(code);

        return kakaoToken;
    }

}
