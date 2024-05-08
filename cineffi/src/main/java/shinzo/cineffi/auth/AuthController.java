package shinzo.cineffi.auth;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shinzo.cineffi.domain.dto.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;
import shinzo.cineffi.jwt.JWToken;

import java.util.HashMap;
import java.util.Map;


@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login/kakao")
    public RedirectView loginKakao(@RequestParam final String code, RedirectAttributes redirectAttributes) {

        //인가코드로 카카오 토큰 발급
        KakaoToken kakaoToken = authService.requestKakaoToken(code);

        //카카오 토큰으로 필요하다면 회원가입하고 userId 반환
        Long userId = authService.loginByKakao(kakaoToken.getAccessToken());
        LoginResponseDTO userInfo = authService.userInfo(userId);
        ResponseEntity<ResponseDTO<Object>> result = null;

        try {
            result = authLogin(userId);
            ResponseDTO<Object> obj = result.getBody().toBuilder()
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .result(userInfo)
                    .build();
            result.ok(obj);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ResponseEntity<ResponseDTO<Object>> a = result;

        redirectAttributes.addFlashAttribute("result", result);
        redirectAttributes.addFlashAttribute("되나?", "돼라제발");

        // Redirect to another page
        return new RedirectView("http://localhost:3000");
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
        System.out.println("Login Start");
        Long userId = authService.getUserIdByEmail(request.getEmail());
        System.out.println(userId);
        if(userId==null){
            return ResponseEntity.status(ErrorMsg.ACCOUNT_MISMATCH.getHttpStatus())
                    .body(ResponseDTO.<Object>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.ACCOUNT_MISMATCH.getDetail())
                            .build());
        }

        boolean LoginSuccess = authService.emailLogin(request);

        if(LoginSuccess) {
            System.out.println("Login Success");
            return authLogin(userId);
        }
        else {
            System.out.println("Login Failed");
            return ResponseEntity.status(ErrorMsg.ACCOUNT_MISMATCH.getHttpStatus())
                    .body(ResponseDTO.<Object>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.ACCOUNT_MISMATCH.getDetail())
                            .build());
        }
    }

    private ResponseEntity<ResponseDTO<Object>> authLogin(Long userId) throws JsonProcessingException {
        Object[] result = authService.makeCookie(userId);
        HttpHeaders headers = (HttpHeaders) result[0];
        ResponseDTO<Object> responseDTO = ResponseDTO.<Object>builder()
                .isSuccess(true)
                .message(SuccessMsg.SUCCESS.getDetail())
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

    @GetMapping("/userInfo")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> userInfo(){
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal()!= "anonymousUser") {

            Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            LoginResponseDTO userInfo = authService.userInfo(userId);
            ResponseDTO<LoginResponseDTO> userInf = ResponseDTO.<LoginResponseDTO>builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .result(userInfo)
                    .build();
            return ResponseEntity.ok(userInf);
        }else{
            return ResponseEntity.status(ErrorMsg.NOT_LOGGED_ID.getHttpStatus())
                    .body(ResponseDTO.<LoginResponseDTO>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.NOT_LOGGED_ID.getDetail())
                            .build());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<ResponseDTO<?>> logout()  {
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal()!= "anonymousUser") {
            Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
                Object[] result = authService.logout(userId);
                //텅빈 쿠키 담아서 쿠키 전달하기
                HttpHeaders headers = (HttpHeaders) result[0];
                ResponseDTO<?> responseDTO = ResponseDTO.builder()
                        .isSuccess(true)
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build();
                return ResponseEntity.ok().headers(headers).body(responseDTO);
        }
    else{
        return ResponseEntity.status(ErrorMsg.UNAUTHORIZED_MEMBER.getHttpStatus())
                .body(ResponseDTO.builder()
                        .isSuccess(false)
                        .message(ErrorMsg.UNAUTHORIZED_MEMBER.getDetail())
                        .build());
            }
    }


    @GetMapping("/user/check")
    public ResponseEntity<ResponseDTO<?>> usercheck(){
        if(SecurityContextHolder.getContext().getAuthentication().getPrincipal()!= "anonymousUser") {
            Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
            Map<String, Long> userJson = new HashMap<>();
            userJson.put("userId", userId);

            ResponseDTO<?> responseDTO = ResponseDTO.builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .result(userJson)
                    .build();
            return ResponseEntity.ok(responseDTO);
        }
        else{
            return ResponseEntity.status(ErrorMsg.NOT_LOGGED_ID.getHttpStatus())
                    .body(ResponseDTO.<Object>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.NOT_LOGGED_ID.getDetail())
                            .build());
        }

    }
}