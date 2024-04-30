package shinzo.cineffi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.AuthCodeDTO;
import shinzo.cineffi.domain.dto.EmailRequestDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @PostMapping("/api/auth/verify/email")
    public ResponseEntity<ResponseDTO<String>> sendEmail(@RequestBody EmailRequestDTO request) {
        int number = mailService.sendMail(request.getEmail());
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .isSuccess(true)
                .message(SuccessMsg.SUCCESS.getDetail())
                .build();
        return ResponseEntity.ok(responseDTO);

    }

    // 인증번호 일치여부 확인
    @GetMapping("/api/verify/email/check")
    public ResponseEntity<ResponseDTO<Boolean>> mailCheck(@RequestParam("code") int code, @RequestParam("email") String email) {
        AuthCodeDTO request = new AuthCodeDTO();
        request.setCode(code);
        request.setEmail(email);
        boolean ischecked = mailService.checkCode(request);
        if(ischecked) {
            ResponseDTO<Boolean> responseDTO = ResponseDTO.<Boolean>builder()
                    .isSuccess(true)
                    .message(SuccessMsg.SUCCESS.getDetail())
                    .build();

            return ResponseEntity.ok(responseDTO);
        }
        else{
            return ResponseEntity.status(ErrorMsg.UNAUTHORIZED_MEMBER.getHttpStatus())
                    .body(ResponseDTO.<Boolean>builder()
                            .isSuccess(false)
                            .message(ErrorMsg.UNAUTHORIZED_MEMBER.getDetail())
                            .build());

        }

    }
}
