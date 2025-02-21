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

    @GetMapping("/api/health")
    @CrossOrigin
    public String test() {

        return "백엔드와 통신 성공! 이제 안심하라구!";
    }
}
