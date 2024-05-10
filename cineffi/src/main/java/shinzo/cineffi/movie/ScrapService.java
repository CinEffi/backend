package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.GetScrapRes;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.MovieGenre;
import shinzo.cineffi.domain.entity.movie.Scrap;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.entity.user.UserAnalysis;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.MovieRepository;
import shinzo.cineffi.movie.repository.ScrapRepository;
import shinzo.cineffi.domain.dto.ScrapDto;
import shinzo.cineffi.score.repository.ScoreRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final EncryptUtil encryptUtil;
    @Transactional(readOnly = true)
    public GetScrapRes getUserScrapList(Long userId, Long loginUserId, Pageable pageable) {
        // 존재하는 유저인지 검증
        if(!userRepository.existsById(userId)) throw new CustomException(ErrorMsg.EMPTY_USER);
        Page<Scrap> userScrapList = scrapRepository.findAllByUserIdOrderByIdDesc(userId, pageable);
        List<ScrapDto> scrapList = new ArrayList<>();

        int totalPageNum = userScrapList.getTotalPages();

        for (Scrap scrap : userScrapList) {
            // 스크랩에서 영화, 유저 객체 빼놓기
            Movie movie = scrap.getMovie();
            User user = scrap.getUser();

            // 로그인 유저가 해당 영화를 스크랩 했는지?
            boolean isScrap = scrapRepository.existsByMovieIdAndUserId(movie.getId(), loginUserId);

            // 마이페이지 유저가 해당 영화에 준 평점
            Float userScore = null;
            Score foundUserScore = scoreRepository.findByMovieAndUser(movie, user);
            if (foundUserScore != null)
                userScore = foundUserScore.getScore();

            // Dto 꾸리기
            scrapList.add(ScrapDto.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .poster(decodeImage(movie.getPoster()))
                    .userScore(userScore)
                    .releaseDate(movie.getReleaseDate())
                    .isScrap(isScrap)
                    .build());
        }

        return GetScrapRes.builder()
                .scrapList(scrapList)
                .totalPageNum(totalPageNum).build();

    }


    //영화 스크랩
    public void scrapMovie(Long movieId, Long userId) {

        //유저 찾기
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));

        //영화 찾기
        Movie movie = movieRepository.findById(movieId).orElseThrow(
                () -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));

        scrapRepository.findByMovieAndUser(movie, user).ifPresent(s -> {
            throw new CustomException(ErrorMsg.SCRAP_EXIST);
        });

        Scrap newScrap = scrapRepository.save(Scrap.builder()
                .movie(movie)
                .user(user)
                .build());

        // 통계 및 액티비티 값 변경
        for (MovieGenre genre : movie.getGenreList())
            user.getUserAnalysis().updateGenreTendency(genre.getGenre(), UserAnalysis.reviewPoint);
        user.getUserActivityNum().addScrapNum();

        userRepository.save(user);
        scrapRepository.save(newScrap);
    }

    //영화 스크랩 취소
    public void unScrap(Long movieId, Long userId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        Movie movie = movieRepository.findById(movieId).orElseThrow(
                () -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));

        Scrap scrap = scrapRepository.findByMovieAndUser(movie, user).orElseThrow(
                () -> new  CustomException(ErrorMsg.SCRAP_NOT_EXIST));

        // 통계 및 액티비티 값 변경
        for (MovieGenre genre : movie.getGenreList())
            user.getUserAnalysis().updateGenreTendency(genre.getGenre(), -UserAnalysis.reviewPoint);
        user.getUserActivityNum().subScrapNum();

        userRepository.save(user);
        scrapRepository.delete(scrap);
    }
}
