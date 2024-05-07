package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.movie.BoxOfficeMovie;
import shinzo.cineffi.exception.message.SuccessMsg;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;
    private final MovieInitService movieInitService;

    @GetMapping("/init")
    public ResponseEntity<ResponseDTO<?>> init() {
        long beforeTime = System.currentTimeMillis(); //코드 실행 전에 시간 받아오기

        movieInitService.fetchTMDBIdsByDate();

        long afterTime = System.currentTimeMillis(); // 코드 실행 후에 시간 받아오기
        long secDiffTime = (afterTime - beforeTime)/1000; //두 시간에 차 계산

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(secDiffTime)
                        .build()
        );
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ResponseDTO<?>> findUpcomingList(){
        List<UpcomingMovieDTO> result = movieService.findUpcomingList();
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(result)
                        .build()
        );
    }

    @GetMapping("/genre")
    public ResponseEntity<ResponseDTO<?>> findRandomGenreList(){
        GenreMovieListDTO movieList = movieService.findGenreList();
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(movieList)
                        .build()
        );
    }

    @GetMapping("updateBoxOffice")
    public ResponseEntity<ResponseDTO<?>> updateBoxOffice() {
        movieService.insertDailyBoxOffice();

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result("영화진흥원 일별박스오피스 불러오기 완료")
                        .build()
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseDTO<?>> findsearchMovieList(@RequestParam String q, @RequestParam int page, @RequestParam int size){
        MovieSearchRespon response = movieService.findSearchList(q, page, size);

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(response)
                        .build()
        );
    }


    @GetMapping("boxOffice")
    public ResponseEntity<ResponseDTO<List<BoxOfficeMovie>>> getDailyBoxOffice() {
        List<BoxOfficeMovie> boxOfficeMovies = movieService.getEnhancedDailyMovies();
        return ResponseEntity.ok(
                ResponseDTO.<List<BoxOfficeMovie>>builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boxOfficeMovies)
                        .build());
    }


}
