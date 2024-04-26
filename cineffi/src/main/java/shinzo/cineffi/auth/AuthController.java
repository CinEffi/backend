package shinzo.cineffi.auth;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.AuthRequestDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/signup")
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
}