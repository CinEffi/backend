package shinzo.cineffi;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Properties;

@RestController
public class TestController {

    @GetMapping("/api/test")
    public String test() {;
        // 메일 서버 설정
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // SMTP 서버 주소
        props.put("mail.smtp.port", "587"); // SMTP 포트 번호
        props.put("mail.smtp.auth", "true"); // 인증 필요 여부
        props.put("mail.smtp.starttls.enable", "true"); // TLS 사용 여부

        // 프록시 설정
        props.put("mail.smtp.proxy.host", "krmp-proxy.9rum.cc");
        props.put("mail.smtp.proxy.port", "3128");

        // 사용자 인증 정보
        final String username = "skwd1012@gmail.com";
        final String password = "tboxbeusjvbysajd";

        // 세션 생성
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            // 메시지 생성
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("skwd1012@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("radic7700@gmail.com"));
            message.setSubject("Test Email");
            message.setText("This is a test email sent through a proxy server.");

            // 메일 전송
            Transport.send(message);
            System.out.println("Email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return "백엔드와 통신 성공! 이제 안심하라구!";
    }
}
