package shinzo.cineffi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import shinzo.cineffi.config.RestTemplateConfig;
import shinzo.cineffi.domain.dto.AuthCodeDTO;
import shinzo.cineffi.domain.dto.EmailRequestDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
    private final RestTemplate restTemplate;

    @PostMapping("/api/auth/verify/email")
    public ResponseEntity<ResponseDTO<String>> sendEmail(@RequestBody EmailRequestDTO request) {
        String apiUrl = "YOUR_API_ENDPOINT_URL"; // API 요청 보내기
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
        int number = mailService.sendMail(request.getEmail());
        ResponseDTO<String> responseDTO = ResponseDTO.<String>builder()
                .isSuccess(true)
                .message(SuccessMsg.SUCCESS.getDetail())
                .build();
        return ResponseEntity.ok(responseDTO);

    }

    // 인증번호 일치여부 확인
    @PostMapping("/api/auth/verify/email/check")
    public ResponseEntity<ResponseDTO<Boolean>> mailCheck(@RequestBody AuthCodeDTO request) {
        request.setCode(request.getCode());
        request.setEmail(request.getEmail());
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
