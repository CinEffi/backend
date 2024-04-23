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

    @GetMapping("/api/movie/init")
    public ResponseEntity<ResponseDTO<?>> init() {
        List<Integer> movieIds = movieService.fetchAllMovieIds();
        movieService.saveMovieIds(movieIds);

        movieService.initMovieData();
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result("잘됏나?!?")
                        .build()
        );
    }


}
