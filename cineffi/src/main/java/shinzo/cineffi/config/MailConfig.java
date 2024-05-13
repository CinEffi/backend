package shinzo.cineffi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSenderImpl mailSender() {
        // 시스템 속성 설정
        System.setProperty("socksProxyHost", "krmp-proxy.9rum.cc");
        System.setProperty("socksProxyPort", "3128");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com"); // SMTP 호스트 설정
        mailSender.setPort(587); // SMTP 포트 설정

        mailSender.setUsername("skwd1012@gmail.com");
        mailSender.setPassword("tboxbeusjvbysajd");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.socks.host", "krmp-proxy.9rum.cc"); // 프록시 호스트
        props.put("mail.smtp.socks.port", "3128"); // 프록시 포트
        props.put("mail.debug", "true");

        return mailSender;
    }
}