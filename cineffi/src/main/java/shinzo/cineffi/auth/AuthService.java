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
import shinzo.cineffi.domain.dto.KakaoProfile;
import shinzo.cineffi.domain.dto.KakaoToken;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.enums.LoginType;
import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserRepository;
import java.util.Optional;
import java.util.Random;

import static shinzo.cineffi.domain.enums.LoginType.KAKAO;
import org.springframework.security.crypto.bcrypt.BCrypt;
import shinzo.cineffi.domain.dto.AuthRequestDTO;

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

    public KakaoToken getKakaoToken(String code){
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

    public KakaoProfile findKakaoProfile(String accessToken){
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
        try{
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }
        System.out.println(kakaoProfile);
        return kakaoProfile;
    }

    //카카오 로그인 or 회원가입
    public String loginByKakao(String accessToken){
        String email = getKakaoEmail(accessToken);
        boolean isEmailDuplicate = userAccountRepository.existsByEmail(email);

        //회원가입 전적 없으면 회원가입
        if(!isEmailDuplicate) {
            AuthRequestDTO dto = AuthRequestDTO.builder()
                    .email(email)
                    .nickname(generateNickname())
                    .isauthentication(true) //이게뭐지??????? 프론트에서 주는건가??
                    .build();
            joinUser(dto);
        }






        return "임시 반환값";
    }

    //카카오 토큰으로 카카오 이메일 가져오기
    private String getKakaoEmail(String accessToken){
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
        try{
            kakaoProfile = objectMapper.readValue(kakaoProfileResponse.getBody(), KakaoProfile.class);
        }catch (JsonProcessingException e){
            e.printStackTrace();
        }

        return kakaoProfile.getKakaoAccount().getEmail();
    }
    //랜덤 닉네임 생성기
    private String generateNickname(){
        Random random = new Random();
        int randomNum = random.nextInt(10000);
        String randomStr = "";
        switch(randomNum%4){
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

        return randomStr + randomNum;
    }


    public boolean authUser(AuthRequestDTO request) {
        boolean isEmailDuplicate = userAccountRepository.existsByEmail(request.getEmail());
        boolean isNickNameDuplicate = userRepository.existsByNickname(request.getNickname());
        if (isEmailDuplicate) {
            System.out.println("isEmailDuplicate");
            return false; // 이메일 중복 시 false 반환
        }
        if(isNickNameDuplicate){
            System.out.println("isNickNameDuplicate");
            return false; // 이메일 중복 시 false 반환
        }

        joinUser(request);

        return true; //이메일 중복 없을 때는 true
    }

    private void joinUser(AuthRequestDTO request){
        User user = User.builder()
                .nickname(request.getNickname())
                .build();
        userRepository.save(user);

        UserAccount userAccount = UserAccount.builder()
                .password(BCrypt.hashpw(request.getPassword(),BCrypt.gensalt()))
                .email(request.getEmail())
                .user(user)
                .isAuthentication(request.getIsauthentication())
                .build();

        userAccountRepository.save(userAccount);
    }

}
