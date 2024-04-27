package shinzo.cineffi.redis;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LabController {

    @GetMapping("/hello")
    public String helloGet() {
        return "/hello : GET request\n";
    }

    @PostMapping("/hi")
    public String hiPost() {
        return "/hi : Post request\n";
    }

    @PostMapping("/bye")
    public String byePost() {
        return "/bye : Post request\n";
    }

    @PostMapping("/text")
    public String textPost(@RequestBody String body) {
        System.out.println("body = " + body);
        return "/text : Post request\n";
    }

}
