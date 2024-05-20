package shinzo.cineffi.auth;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import shinzo.cineffi.domain.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static shinzo.cineffi.exception.message.ErrorMsg.*;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${kakao.redirect_url}")
    private String REDIRECT_URL;

    @GetMapping("/auth/login/kakao")
    public ResponseEntity<ResponseDTO<Object>> loginKakao(@RequestParam final String code) throws JsonProcessingException {

        //인가코드로 카카오 토큰 발급
        KakaoToken kakaoToken = authService.requestKakaoToken(code);
        //카카오 토큰으로 필요하다면 회원가입하고 userId 반환
        Long userId = authService.loginByKakao(kakaoToken.getAccessToken());
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if(loginUserId==null) {
            if (userId == null) {
                return createErrorResponse(ErrorMsg.EMPTY_USER);
            }
        }else{
            authService.logout(loginUserId);
        }
        LoginResponseDTO userInfo = authService.userInfo(userId);

        return redirectauthLogin(userId,userInfo);

    }

    @PostMapping("/auth/signup")
    public ResponseEntity<ResponseDTO<String>> signup(@RequestBody AuthRequestDTO request) {
        if (request.getIsauthentication() == null) {
            throw new CustomException(UNAUTHORIZED_MEMBER);
        }
        boolean authSuccess = authService.authUser(request);
        HttpStatus httpStatus = authSuccess ? HttpStatus.OK : DUPLICATE_USER.getHttpStatus();
        String message = authSuccess ? SuccessMsg.SUCCESS.getDetail() : DUPLICATE_USER.getDetail();

        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .isSuccess(authSuccess)
                .message(message)
                .build();

        return ResponseEntity.status(httpStatus).body(responseDTO);
    }

    @PostMapping("/auth/login/email")
    public ResponseEntity<ResponseDTO<Object>> emailLogin(@RequestBody LoginRequestDTO request) throws JsonProcessingException {
        System.out.println("Login Start");
        Long userId = authService.getUserIdByEmail(request.getEmail());
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) {
            if (userId == null) {
                return createErrorResponse(ErrorMsg.EMPTY_USER);
            }
        }else{
        authService.logout(loginUserId);
        }
        boolean loginSuccess = authService.emailLogin(request);
        if (loginSuccess) {
            System.out.println("Login Success");
            return authLogin(userId);
        } else {
            System.out.println("Login Failed");
            return createErrorResponse(ErrorMsg.ACCOUNT_MISMATCH);
        }

    }

    private ResponseEntity<ResponseDTO<Object>> createErrorResponse(ErrorMsg errorMsg) {
        return ResponseEntity.status(errorMsg.getHttpStatus())
                .body(ResponseDTO.<Object>builder()
                        .isSuccess(false)
                        .message(errorMsg.getDetail())
                        .build());
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
    private ResponseEntity<ResponseDTO<Object>> redirectauthLogin(Long userId, LoginResponseDTO userInfo) throws JsonProcessingException {
        Object[] result = authService.makeCookie(userId);
        HttpHeaders headers = (HttpHeaders) result[0];

        // 리다이렉션 URL 설정
        String redirectUrl = REDIRECT_URL;

        // ResponseDTO 생성
        ResponseDTO<Object> responseDTO = ResponseDTO.<Object>builder()
                .isSuccess(true)
                .result(userInfo)
                .message(SuccessMsg.SUCCESS.getDetail())
                .build();

        // 리다이렉션 응답 반환
        return ResponseEntity.status(HttpStatus.FOUND)
                .headers(headers)
                .location(URI.create(redirectUrl))
                .body(responseDTO);
    }


    @PostMapping("/auth/email/check")
    public ResponseEntity<ResponseDTO<String>> checkDuplicateEmail(@RequestBody EmailRequestDTO request) {
        boolean isDuplicate = authService.dupMail(request);

        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .isSuccess(!isDuplicate)
                .message(isDuplicate ? ErrorMsg.DUPLICATE_EMAIL.getDetail() : SuccessMsg.SUCCESS.getDetail())
                .build();

        return isDuplicate ? ResponseEntity.status(ErrorMsg.DUPLICATE_EMAIL.getHttpStatus()).body(responseDTO)
                : ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/auth/nickname/check")
    public ResponseEntity<ResponseDTO<String>> checkDuplicateNickname(@RequestBody NickNameDTO request) {
        boolean isDuplicate = authService.dupNickname(request);

        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .isSuccess(!isDuplicate)
                .message(isDuplicate ? ErrorMsg.DUPLICATE_NICKNAME.getDetail() : SuccessMsg.SUCCESS.getDetail())
                .build();

        return isDuplicate ? ResponseEntity.status(ErrorMsg.DUPLICATE_NICKNAME.getHttpStatus()).body(responseDTO)
                : ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/auth/userInfo")
    public ResponseEntity<ResponseDTO<LoginResponseDTO>> getUserInfo() {
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) {
            throw new CustomException(NOT_LOGGED_IN);
        } else {
            LoginResponseDTO userInfo = authService.userInfo(loginUserId);
            ResponseDTO<LoginResponseDTO> responseDTO = ResponseDTO.<LoginResponseDTO>builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .result(userInfo)
                    .build();
            return ResponseEntity.ok(responseDTO);
        }
    }
    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseDTO<?>> logout()  {
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) throw new CustomException(NOT_LOGGED_IN);
        else{
            Object[] result = authService.logout(loginUserId);
            //텅빈 쿠키 담아서 쿠키 전달하기
            HttpHeaders headers = (HttpHeaders) result[0];
            ResponseDTO<?> responseDTO = ResponseDTO.builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .build();
            return ResponseEntity.ok().headers(headers).body(responseDTO);
        }
    }


    @GetMapping("/auth/user/check")
    public ResponseEntity<ResponseDTO<?>> usercheck(){
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) throw new CustomException(NOT_LOGGED_IN);
        else{
            Map<String, Long> userJson = new HashMap<>();
            userJson.put("userId", loginUserId);

            ResponseDTO<?> responseDTO = ResponseDTO.builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .result(userJson)
                    .build();
            return ResponseEntity.ok(responseDTO);
        }
    }
    @PostMapping("/user/delete")
    public ResponseEntity<ResponseDTO<?>> userDelete(){
        Long loginUserId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        if (loginUserId == null) throw new CustomException(NOT_LOGGED_IN);
        else{
            boolean isSuccess = authService.userdelete(loginUserId);
            if(isSuccess) {
                ResponseDTO<?> responseDTO = ResponseDTO.builder()
                        .isSuccess(true)
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .build();
                return ResponseEntity.ok(responseDTO);
            }
            else {
                return ResponseEntity.status(ErrorMsg.ISDELETE_USER.getHttpStatus())
                        .body(ResponseDTO.<Object>builder()
                                .isSuccess(false)
                                .message(ErrorMsg.ISDELETE_USER.getDetail())
                                .build());
            }
        }
    }
}