package shinzo.cineffi.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.jwt.JWTUtil;
import shinzo.cineffi.jwt.JWToken;
import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.Optional;
import java.util.Random;
import org.springframework.security.crypto.bcrypt.BCrypt;

import static shinzo.cineffi.jwt.JWTUtil.ACCESS_PERIOD;
import static shinzo.cineffi.jwt.JWTUtil.REFRESH_PERIOD;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;

    @Value("${kakao.rest_api_key}")
    private String restApiKey;
    @Value("${kakao.redirect_url}")
    private String redirectUrl;

    public KakaoToken requestKakaoToken(String code) {
        RestTemplate rt = new RestTemplate();
        //요청보낼 헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        //요청보낼 바디 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", restApiKey);
        params.add("redirect_uri", redirectUrl);
        params.add("code", code);
        //헤더 바디 합치기
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        //ResponseEntity 객체를 String 형만 받도록 생성. 응답받는 값이 Json 형식이니까
        ResponseEntity<String> accessTokenResponse = rt.exchange(
                "https://kauth.kakao.com/oauth/token",
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        //String으로 받은 Json 형식의 데이터를 ObjectMapper 라는 클래스를 사용해 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        KakaoToken kakaoToken = null;
        try {
            kakaoToken = objectMapper.readValue(accessTokenResponse.getBody(), KakaoToken.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return kakaoToken;

    }

    public KakaoProfile findKakaoProfile(String accessToken) {
        RestTemplate rt = new RestTemplate();

        //헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        //요청을 만들어 보내고, 그걸 응답에 받기
        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(kakaoProfile);
        return kakaoProfile;
    }

    //필요하다면 카카오 회원가입, 유저아이디 반환
    public Long loginByKakao(String accessToken) {
        String email = requestKakaoEmail(accessToken);
        boolean isEmailDuplicate = userAccountRepository.existsByEmail(email);

        //회원가입 전적 없으면 회원가입
        if (!isEmailDuplicate) {
            AuthRequestDTO dto = AuthRequestDTO.builder()
                    .email(email)
                    .nickname(generateNickname())
                    .isauthentication(true) //카카오는 이미 겅즘된 이메일이므로 그냥 true
                    .build();
            joinUser(dto);
        }

        return getUserIdByEmail(email);
    }

    //카카오 토큰으로 카카오 이메일 가져오기
    private String requestKakaoEmail(String accessToken) {
        RestTemplate rt = new RestTemplate();

        //헤더 생성
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        //요청을 만들어 보내고, 그걸 응답에 받기
        ResponseEntity<String> kakaoProfileResponse = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        ObjectMapper objectMapper = new ObjectMapper();
        KakaoProfile kakaoProfile = null;
        try {
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return kakaoProfile.getKakaoAccount().getEmail();
    }

    //랜덤 비중복 닉네임 생성기
    public String generateNickname() {
        boolean isDup = true;
        String newNickname = "";
        while (isDup) {
            Random random = new Random();
            int randomNum = random.nextInt(100000);
            String randomStr = "";
            switch (randomNum % 4) {
                case 0:
                    randomStr = "강아지";
                    break;
                case 1:
                    randomStr = "고양이";
                    break;
                case 2:
                    randomStr = "앵무새";
                    break;
                case 3:
                    randomStr = "토끼";
                    break;
            }

            newNickname = randomStr + randomNum;
            isDup = userRepository.existsByNickname(newNickname);
        }
        User user = User.builder().nickname(newNickname).build();
        return newNickname;
    }

    public boolean authUser(AuthRequestDTO request) {
        boolean isEmailDuplicate = userAccountRepository.existsByEmail(request.getEmail());
        boolean isNickNameDuplicate = userRepository.existsByNickname(request.getNickname());
        if (isEmailDuplicate) {
            return false; // 이메일 중복 시 false 반환
        }
        if (isNickNameDuplicate) {
            return false; // 이메일 중복 시 false 반환
        }
        joinUser(request);

        return true; //이메일 중복 없을 때는 true
    }

    public boolean emailLogin(LoginRequestDTO request) {
        UserAccount userAccount = userAccountRepository.findByEmail(request.getEmail());
        return BCrypt.checkpw(request.getPassword(), userAccount.getPassword());
    }

    public Long getUserIdByEmail(String email) {
        Optional<UserAccount> user = Optional.ofNullable(userAccountRepository.findByEmail(email));
        return user.map(UserAccount::getId).orElse(null);
    }

    public void normalLoginRefreshToken(Long memberNo, String refreshToken) {
        UserAccount userAccount = userAccountRepository.getReferenceById(memberNo);//userAccount객체
        userAccount.setUserToken(refreshToken);
        userAccountRepository.save(userAccount);


    }

    public Object[] makeCookie(Long userId) {
        JWToken jwToken = JWTUtil.allocateToken(userId, "ROLE_USER");//액세스 토큰 발급
        //Access 토큰 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("access", jwToken.getAccessToken())
                .sameSite("None")
                .maxAge(ACCESS_PERIOD)
                .path("/")
                .httpOnly(true)
                .build();
        //Refresh 토큰 쿠키
        ResponseCookie refreshCookie = ResponseCookie.from("refresh", jwToken.getRefreshToken())
                .sameSite("None")
                .maxAge(REFRESH_PERIOD)
                .path("/")
                .httpOnly(true)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new Object[]{jwToken, headers};
    }

    private void joinUser(AuthRequestDTO request) {
        User user = User.builder()
                .nickname(request.getNickname())
                .build();
        userRepository.save(user);

        UserAccount userAccount = UserAccount.builder()
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .email(request.getEmail())
                .user(user)
                .isAuthentication(request.getIsauthentication())
                .build();

        userAccountRepository.save(userAccount);
    }

    public boolean dupMail(EmailRequestDTO request) {
        boolean isdup = userAccountRepository.existsByEmail(request.getEmail());

        return isdup;
    }

    public boolean dupNickname(NickNameDTO request) {
        boolean isdup = userRepository.existsByNickname(request.getNickname());

        return isdup;
    }

}
