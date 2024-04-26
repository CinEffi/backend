package shinzo.cineffi.movie;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @GetMapping("/api/movie/updateBoxOffice")
    public ResponseEntity<ResponseDTO<?>> updateBoxOffice() {
        movieService.insertDailyBoxOffice();

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result("영화진흥원 일별박스오피스 불러오기 완료")
                        .build()
        );
    }
}
