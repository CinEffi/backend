package shinzo.cineffi.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.EmailRequestDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/api/auth/login/email")
    public ResponseEntity<ResponseDTO<String>> emailLogin(@RequestBody EmailRequestDTO request){

        boolean LoginSuccess = userService.emailLogin(request);
        if(LoginSuccess) {
            ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .build();
            return ResponseEntity.ok(responseDTO);
        }
        else {
            return ResponseEntity.status(ErrorMsg.PASSWORD_INCORRECT_MISMATCH.getHttpStatus())
                    .body(ResponseDTO.<String>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.PASSWORD_INCORRECT_MISMATCH.getDetail())
                            .build());
        }
    }
}
