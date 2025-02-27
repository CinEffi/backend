package shinzo.cineffi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    @CrossOrigin
    public ResponseEntity<?> test() {

        return ResponseEntity.ok().build();
    }
}
