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
        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기

        movieService.fetchTMDBIdsByDate();

        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime)/1000; //두 시간에 차 계산

//        movieService.initMovieData();
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(secDiffTime)
                        .build()
        );
    }

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
