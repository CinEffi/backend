package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
    private final BoxOfficeDataHandler boxOfficeDataHandler;

//    @GetMapping("/update") //미완성: 어차피 안쓸 로직인데 이시간에 딴걸 만들자
//    public ResponseEntity<ResponseDTO<?>> update() {
//        int nowYear = LocalDate.now().getYear();
//        int nextYear = nowYear + 1;
//        int initYear = LocalDate.now().getMonthValue() > 11 ? nextYear : nowYear;
//
//        List<Movie> TMDBBasicDatas = newMovieInitService.getTMDBBasicDatasByDate(initYear);
//        List<Movie> kobisBasicDatas = newMovieInitService.requestKobisDatas(initYear);
//
//        List<Movie> mixBasicDatas = newMovieInitService.returnMIxDatas(TMDBBasicDatas, kobisBasicDatas);
//        newMovieInitService.requestDetailDatas(mixBasicDatas);
//        boxOfficeDataHandler.dailyBoxOffice();
//
//        return ResponseEntity.ok(
//                ResponseDTO.builder()
//                        .message(SuccessMsg.SUCCESS.getDetail())
//                        .result(null)
//                        .build());
//    }

    @GetMapping("/init")
    public ResponseEntity<ResponseDTO<?>> init(@RequestParam int year) {
        long beforeTime = System.currentTimeMillis();

        newMovieInitService.initData(year);

        long afterTime = System.currentTimeMillis();
        float diffTime = (afterTime - beforeTime) /1000;

        return ResponseEntity.ok(
                ResponseDTO.builder()
                        .message(SuccessMsg.SUCCESS.getDetail())
                        .result(diffTime)
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
