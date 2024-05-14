package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import shinzo.cineffi.domain.dto.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.domain.dto.GenreMovieListDTO;
import shinzo.cineffi.domain.dto.MovieDetailDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.dto.UpcomingMovieDTO;
import shinzo.cineffi.domain.entity.movie.BoxOfficeMovie;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.exception.message.SuccessMsg;

import static shinzo.cineffi.auth.AuthService.getLoginUserId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;
    private final ScrapService scrapService;
    private final MovieInitService movieInitService;
    private final NewMovieInitService newMovieInitService;

    @GetMapping("/init")
    public ResponseEntity<ResponseDTO<?>> init() {
        long beforeTime = System.currentTimeMillis();

        newMovieInitService.initData();

        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime)/1000;

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(secDiffTime)
                        .build());
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
    public ResponseEntity<ResponseDTO<?>> findsearchMovieList(@RequestParam String q){
        List<MovieDTO> response = movieService.findSearchList(q);

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


    //영화 상세정보 조회
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

    //영화 스크랩
    @PostMapping("/{movieId}/likes")
    public ResponseEntity<ResponseDTO<?>> scrapMovie(@PathVariable Long movieId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        scrapService.scrapMovie(movieId, userId);
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .build());
    }

    //영화 스크랩 취소
    @DeleteMapping("/{movieId}/likes")
    public ResponseEntity<ResponseDTO<?>> unscrapMovie(@PathVariable Long movieId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        scrapService.unScrap(movieId, userId);
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .build());
    }


}
