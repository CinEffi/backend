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
import shinzo.cineffi.domain.dto.KakaoToken;
import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserCoreRepository;

@Transactional
@Service
@RequiredArgsConstructor
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;
import shinzo.cineffi.domain.dto.AuthRequestDTO;
import shinzo.cineffi.domain.dto.EmailRequestDTO;
import shinzo.cineffi.domain.dto.NickNameDTO;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.domain.entity.user.User;

import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserRepository;

@RequiredArgsConstructor
@Service
@ResponseBody
public class AuthService {
    private final UserCoreRepository userCoreRepo;
    private final UserAccountRepository userAccountRepo;

    @Value("${kakao.rest_api_key}")
    private String restApiKey;
    @Value("${kakao.redirect_url}")
    private String redirectUrl;

    public KakaoToken getAccessToken(String code){
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




    private final UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;

    public boolean authUser(AuthRequestDTO request) {
        boolean isEmailDuplicate = userAccountRepository.existsByEmail(request.getEmail());
        boolean isNickNameDuplicate = userRepository.existsByNickname(request.getNickname());
        if (isEmailDuplicate) {

            return false; // 이메일 중복 시 false 반환
        }
        if(isNickNameDuplicate){

            return false; // 이메일 중복 시 false 반환
        }

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

        return true; //이메일 중복 없을 때는 true
    }

    public boolean dupmail(EmailRequestDTO request) {
        boolean isdup = userAccountRepository.existsByEmail(request.getEmail());

        return isdup;
    }

    public boolean dupnickname(NickNameDTO request) {
        boolean isdup = userRepository.existsByNickname(request.getNickname());

        return isdup;
    }
}
