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
    public String getKakaoToken(@RequestParam final String code){
        //인가코드로 카카오 토큰 발급
        KakaoToken kakaoToken = authService.getKakaoToken(code);

        //카카오 토큰으로 로그인 or 회원가입
        authService.loginByKakao(kakaoToken.getAccessToken());


        return "임시 반환값";
    }

}
