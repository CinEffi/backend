package shinzo.cineffi.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import shinzo.cineffi.auth.repository.AuthCodeRepository;
import shinzo.cineffi.domain.dto.AuthCodeDTO;
import shinzo.cineffi.domain.entity.user.AuthCode;
import shinzo.cineffi.movie.NewMovieInitService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class MailService {
    private final AuthCodeRepository authCodeRepository;
    private final JavaMailSender javaMailSender;
    private final RestTemplate restTemplate;
    private static final String senderEmail = "cineffi24@gmail.com";
    private static int number;

    @Value("${email.proxy-url}")
    private String cineffiProxyServer;


    public static void createNumber(){
        number = (int)(Math.random() * (90000)) + 100000;// (int) Math.random() * (최댓값-최소값+1) + 최소값
    }
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);


    public MimeMessage CreateMail(String request){


        createNumber();
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, request);
            message.setSubject("이메일 인증");
            String body = "";
            body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
            body += "<h1>" + number + "</h1>";
            body += "<h3>" + "감사합니다." + "</h3>";
            message.setText(body,"UTF-8", "html");
            message.saveChanges();
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            saveAuthCode(request,number,expirationTime);
        } catch (MessagingException e) {
            logger.error("Error occurred while creating email message", e);
        }

        return message;
    }
    public void saveAuthCode(String email,int code, LocalDateTime expirationTime) {
        AuthCode authCode1 = authCodeRepository.findByEmail(email);
        if(authCode1 != null){
            authCodeRepository.delete(authCode1);
        }
        AuthCode authCode = AuthCode.builder()
                .email(email)
                .time(LocalDateTime.now())
                .code(code)
                .expirationTime(expirationTime)
                .build();

        authCodeRepository.save(authCode);
    }
    //expirationTime 값이 now 이전인 AuthCode엔티티 목록 반환
    //반환된 리스트를 삭제
    @Scheduled(fixedRate = 60000) // 1분 간격으로 실행
    public void deleteExpiredAuthCodes() {
        LocalDateTime now = LocalDateTime.now();
        authCodeRepository.findByExpirationTimeBefore(now)
                .forEach(authCodeRepository::delete);
    }

    //생성한 메일을 전송
    public int sendMail(String toEmailAddress) {
        // 씨네피 프록시 서버로 이메일 전송 요청!
        // 인증번호 생성
        createNumber();

        // 프록시 서버 url 지정
        String url = cineffiProxyServer + "/mail-send";

        // 요청 객체 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");

        Map<String, Object> body = new HashMap<>();
        body.put("authCode", number);
        body.put("emailAddress", toEmailAddress);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        // 요청 보내기
        System.out.println("스프링 -> 파이썬 프록시 요청 보내기!");
        try {
            Map<String, Object> response = parseJson(restTemplate.postForObject(url, entity, String.class));

            if (response.get("isSuccess") == Boolean.FALSE || response.get("isSuccess") == null) {
                return -1;
            }

            // 만료시간 저장
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
            saveAuthCode(toEmailAddress, number, expirationTime);

            return number;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
        //이메일 인증번호 체크
        public boolean checkCode (AuthCodeDTO authCodeDTO){
            Optional<AuthCode> optionalAuthCode = authCodeRepository.findByEmailAndCode(authCodeDTO.getEmail(), authCodeDTO.getCode());

            if (optionalAuthCode.isPresent()) {
                AuthCode authCode = optionalAuthCode.get();

                // 인증 코드 유효 시간 확인
                LocalDateTime expirationTime = authCode.getTime().plusMinutes(5);
                if (LocalDateTime.now().isBefore(expirationTime)) {
                    // 인증 코드 일치 및 유효 시간 내
                    authCodeRepository.delete(authCode);
                    return true;
                } else {
                    // 인증 코드 만료
                    authCodeRepository.delete(authCode);
                    return false;
                }
            } else {
                // 인증 코드 불일치
                return false;
            }
        }

        private Map<String, Object> parseJson(String json) throws IOException {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Map.class);
        }

}

