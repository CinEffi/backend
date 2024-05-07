package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.dto.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import shinzo.cineffi.config.EncryptUtil;
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
    private final ScrapService scrapService;
    private final EncryptUtil encryptUtil;
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


    //영화 상세정보 조회
    @GetMapping("/{encryptedMovieId}")
    public ResponseEntity<ResponseDTO<MovieDetailDTO>> getMovieDetails(@PathVariable String encryptedMovieId) {
        String decryptedMovieId = encryptUtil.decrypt(encryptedMovieId); //복호화
        Long movieId = Long.parseLong(decryptedMovieId); //String을 Long으로 변환

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


    //테스트용 숫자를 넣으면 -> 암호회된 id로 만들어주는 메서드
    @GetMapping("/encrypt-test/{id}")
    public ResponseEntity<ResponseDTO> encryptTestId(@PathVariable("id") Long id) {

        String encryptedId = encryptUtil.encrypt(String.valueOf(id));
        return ResponseEntity.ok(ResponseDTO.builder()
                .message(SuccessMsg.SUCCESS.getDetail())
                .result(encryptedId)
                .build());
    }
}
