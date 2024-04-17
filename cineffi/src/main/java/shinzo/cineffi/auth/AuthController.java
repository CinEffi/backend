package shinzo.cineffi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shinzo.cineffi.domain.dto.KakaoToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import shinzo.cineffi.domain.dto.KakaoToken;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.AuthRequestDTO;
import shinzo.cineffi.domain.dto.EmailRequestDTO;
import shinzo.cineffi.domain.dto.NickNameDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;
@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/api/auth/login/kakao")
    public ResponseEntity<ResponseDTO<?>> loginByKakao(@RequestParam final String code){
        //인가코드로 카카오 토큰 발급
        KakaoToken kakaoToken = authService.getKakaoToken(code);

        //카카오 토큰으로 로그인 or 회원가입
        authService.loginByKakao(kakaoToken.getAccessToken());

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result("되나?")
                        .build());
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
    //이메일 중복 검사
    @GetMapping("/email/check")
    public ResponseEntity<ResponseDTO<String>> maildupcheck(@RequestBody EmailRequestDTO request){
       boolean MailDupCheck =authService.dupmail(request);
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
    @GetMapping("/nickname/check")
    public ResponseEntity<ResponseDTO<String>> nicknamedupcheck(@RequestBody NickNameDTO request){
        boolean NickDupCheck =authService.dupnickname(request);
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