package shinzo.cineffi.auth;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shinzo.cineffi.domain.dto.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;
import shinzo.cineffi.jwt.JWToken;

import java.util.Arrays;


@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login/kakao")
    public ResponseEntity<ResponseDTO<Object>> loginByKakao(@RequestParam final String code) throws JsonProcessingException {
        //인가코드로 카카오 토큰 발급
        KakaoToken kakaoToken = authService.requestKakaoToken(code);

        //카카오 토큰으로 필요하다면 회원가입하고 userId 반환
        Long userId = authService.loginByKakao(kakaoToken.getAccessToken());

        return authLogin(userId);
    }

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO<String>> signup(@RequestBody AuthRequestDTO request) {
        if(request.getIsauthentication()) {
            boolean AuthSuccess = authService.authUser(request);
            if (AuthSuccess) {
                ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                        .isSuccess(true)
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build();
                return ResponseEntity.ok(responseDTO);
            } else {
                return ResponseEntity.status(ErrorMsg.DUPLICATE_USER.getHttpStatus())
                        .body(ResponseDTO.<String>builder()
                                .isSuccess(false)
                                .message(ErrorMsg.DUPLICATE_USER.getDetail())
                                .build());
            }
        }
        else{
            return ResponseEntity.status(ErrorMsg.UNAUTHORIZED_MEMBER.getHttpStatus())
                    .body(ResponseDTO.<String>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.UNAUTHORIZED_MEMBER.getDetail())
                            .build());
        }
    }

    @PostMapping("/login/email")
    public ResponseEntity<ResponseDTO<Object>> emailLogin(@RequestBody LoginRequestDTO request) throws JsonProcessingException {
        Long userId = authService.getUserIdByEmail(request.getEmail());
        boolean LoginSuccess = authService.emailLogin(request);

        if(LoginSuccess) {
            return authLogin(userId);
        }
        else {
            return ResponseEntity.status(ErrorMsg.PASSWORD_INCORRECT_MISMATCH.getHttpStatus())
                    .body(ResponseDTO.<Object>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.PASSWORD_INCORRECT_MISMATCH.getDetail())
                            .build());
        }
    }

    private ResponseEntity<ResponseDTO<Object>> authLogin(Long userId) throws JsonProcessingException {
        Object[] result = authService.makeCookie(userId);
        JWToken jwToken = (JWToken) result[0];
        HttpHeaders headers = (HttpHeaders) result[1];
        LoginResponseDTO response = (LoginResponseDTO) result[2];

        authService.normalLoginRefreshToken(userId, jwToken.getRefreshToken());
        ObjectMapper objectMapper = new ObjectMapper();

        ResponseDTO<Object> responseDTO = ResponseDTO.<Object>builder()
                .isSuccess(true)
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(response)
                .build();
        return ResponseEntity.ok().headers(headers).body(responseDTO);
    }

    //이메일 중복 검사
    @PostMapping("/email/check")
    public ResponseEntity<ResponseDTO<String>> maildupcheck(@RequestBody EmailRequestDTO request){
       boolean MailDupCheck = authService.dupMail(request);
       if(!MailDupCheck){
           ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                   .isSuccess(true)
                   .message(SuccessMsg.SUCCESS.getDetail())
                   .build();
           return ResponseEntity.ok(responseDTO);
       }else{
           return ResponseEntity.status(ErrorMsg.DUPLICATE_EMAIL.getHttpStatus())
                   .body(ResponseDTO.<String>builder()
                           .isSuccess(false)
                           .message(ErrorMsg.DUPLICATE_EMAIL.getDetail())
                           .build());
       }

    }
    @PostMapping("/nickname/check")
    public ResponseEntity<ResponseDTO<String>> nicknamedupcheck(@RequestBody NickNameDTO request){
        boolean NickDupCheck = authService.dupNickname(request);
        if(!NickDupCheck){
            ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .build();
            return ResponseEntity.ok(responseDTO);
        }else{
            return ResponseEntity.status(ErrorMsg.DUPLICATE_EMAIL.getHttpStatus())
                    .body(ResponseDTO.<String>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.DUPLICATE_EMAIL.getDetail())
                            .build());
        }

    }
}