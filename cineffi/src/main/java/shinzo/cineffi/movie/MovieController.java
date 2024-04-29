package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.entity.movie.DailyMovie;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/api/movie/init")
    public ResponseEntity<ResponseDTO<?>> init() {
        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기

        movieService.fetchTMDBIdsByDate();

        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime)/1000; //두 시간에 차 계산

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(secDiffTime)
                        .build()
        );
    }



    @GetMapping("/api/movies/boxOffice")
    public ResponseEntity<ResponseDTO<List<DailyMovie>>> getDailyBoxOffice() {
        List<DailyMovie> dailyMovies = movieService.getEnhancedDailyMovies();
        return ResponseEntity.ok(
                ResponseDTO.<List<DailyMovie>>builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(dailyMovies)
                        .build());
    }


}
