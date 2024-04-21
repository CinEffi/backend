package shinzo.cineffi.auth;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import shinzo.cineffi.auth.repository.AuthCodeRepository;
import shinzo.cineffi.domain.dto.AuthCodeDTO;
import shinzo.cineffi.domain.entity.user.AuthCode;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MailService {
    private final AuthCodeRepository authCodeRepository;
    private final JavaMailSender javaMailSender;
    private static final String senderEmail = "cineffi24@gmail.com";
    private static int number;
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

            saveAuthCode(request,number);
        } catch (MessagingException e) {
            logger.error("Error occurred while creating email message", e);
        }

        return message;
    }

    public void saveAuthCode(String email,int code) {
        AuthCode authCode = AuthCode.builder()
                .email(email)
                .time(LocalDateTime.now())
                .code(code)
                .build();

        authCodeRepository.save(authCode);
    }

    public int sendMail(String request){
        MimeMessage message = CreateMail(request);
        javaMailSender.send(message);

        return number;
    }

    public boolean checkCode(AuthCodeDTO authCodeDTO) {
        Optional<AuthCode> optionalAuthCode = authCodeRepository.findByEmailAndCode(authCodeDTO.getEmail(), authCodeDTO.getCode());

        if (optionalAuthCode.isPresent()) {
            AuthCode authCode = optionalAuthCode.get();

            // 인증 코드 유효 시간 확인
            LocalDateTime expirationTime = authCode.getTime().plusMinutes(5);
            if (LocalDateTime.now().isBefore(expirationTime)) {
                // 인증 코드 일치 및 유효 시간 내
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



}

