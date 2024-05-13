package shinzo.cineffi.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import shinzo.cineffi.config.NetworkUtils;
import shinzo.cineffi.domain.dto.AuthCodeDTO;
import shinzo.cineffi.domain.dto.EmailRequestDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
    private final RestTemplate restTemplate;

    @PostMapping("/api/auth/verify/email")
    public ResponseEntity<ResponseDTO<String>> sendEmail(@RequestBody EmailRequestDTO request) throws URISyntaxException {
//        HttpClient client = NetworkUtils.createHttpClientWithProxy();
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI("https://example.com"))
//                .GET()
//                .build();
//
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


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
