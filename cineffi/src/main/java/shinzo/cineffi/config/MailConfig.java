package shinzo.cineffi.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {


    @Bean
    public JavaMailSenderImpl mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // SMTP 호스트 설정
        mailSender.setPort(465); // SSL 포트 설정

        mailSender.setUsername("skwd1012@gmail.com");
        mailSender.setPassword("tboxbeusjvbysajd");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtps"); // SMTPS 프로토콜 사용
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.starttls.enable", "true");
        props.put("mail.smtps.starttls.required", "true");

        return mailSender;
    }
}