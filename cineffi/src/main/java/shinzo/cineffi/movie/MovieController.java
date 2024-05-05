package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.GenreMovieListDTO;
import shinzo.cineffi.domain.dto.MovieDetailDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.dto.UpcomingMovieDTO;
import shinzo.cineffi.domain.entity.movie.BoxOfficeMovie;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.List;

import static shinzo.cineffi.auth.AuthService.getLoginUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/init")
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

    @GetMapping("/upcoming")
    public ResponseEntity<ResponseDTO<?>> findUpcomingList(){
        List<UpcomingMovieDTO> upcomingList = movieService.findUpcomingList();
        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(upcomingList)
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


    @GetMapping("boxOffice")
    public ResponseEntity<ResponseDTO<List<BoxOfficeMovie>>> getDailyBoxOffice() {
        List<BoxOfficeMovie> boxOfficeMovies = movieService.getEnhancedDailyMovies();
        return ResponseEntity.ok(
                ResponseDTO.<List<BoxOfficeMovie>>builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(boxOfficeMovies)
                        .build());
    }


    @GetMapping("/{movieId}")
    public ResponseEntity<ResponseDTO<MovieDetailDTO>> getMovieDetails(@PathVariable Long movieId) {
        Long loginUserId = getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        MovieDetailDTO movieDetail = movieService.findMovieDetails(movieId, loginUserId);

        return ResponseEntity.ok(
                ResponseDTO.<MovieDetailDTO>builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(movieDetail)
                        .build()
        );
    }

}
