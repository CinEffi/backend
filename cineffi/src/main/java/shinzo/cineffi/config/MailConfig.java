//package shinzo.cineffi.config;
//
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//import java.util.Properties;
//
//@Configuration
//public class MailConfig {
//
//
//    @Bean
//    public JavaMailSenderImpl mailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("smtp.gmail.com"); // SMTP 호스트 설정
//        mailSender.setPort(587); // SSL 포트 설정
//
//        mailSender.setUsername("skwd1012@gmail.com");
//        mailSender.setPassword("tboxbeusjvbysajd");
//
//        Properties props = mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp"); // SMTP 프로토콜 사용
//        props.put("mail.smtp.auth", "true"); // SMTP 인증 활성화
//        props.put("mail.smtp.starttls.enable", "true"); // STARTTLS 활성화
//        props.put("mail.smtp.starttls.required", "true"); // STARTTLS 필수
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // SSL 소켓 팩토리 사용
//        props.put("mail.smtp.socketFactory.port", "465"); // SSL 소켓 팩토리 포트
//
//        return mailSender;
//    }
//}