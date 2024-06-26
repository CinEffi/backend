package shinzo.cineffi.score;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.entity.movie.AvgScore;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.MovieGenre;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.entity.user.UserAnalysis;
import shinzo.cineffi.domain.enums.ScoreTypeEvent;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.AvgScoreRepository;
import shinzo.cineffi.movie.repository.MovieRepository;
import shinzo.cineffi.score.repository.ScoreRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final AvgScoreRepository avgScoreRepository;

    public void scoreMovie(Float score, Long movieId, Long userId) {

        if (score == null || score < 0.5 || 5 < score || score % 0.5 != 0)
            throw new CustomException(ErrorMsg.INVALID_SCORE_VALUE);
        // 유저 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        // 평점 매길 영화 조회, 찾기
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));
        // 평점 생성, db에 저장하기, 평균 평점 인풋 준비하기
        Float deltaScoreSum = score;
        Score existingScoreData = scoreRepository.findByMovieAndUser(movie, user);
        boolean isUpdate = (existingScoreData != null);
        Integer deltaScoreCount = isUpdate ? 0 : 1;
        if (isUpdate) {
            deltaScoreSum -= existingScoreData.getScore();
            scoreRepository.save(existingScoreData.toBuilder().score(score).build());
        }
        else scoreRepository.save(Score.builder().score(score).user(user).movie(movie).build());
        // 유저 통계 갱신하기
        user.getUserAnalysis().updateScoreTendency(deltaScoreSum, deltaScoreCount);
        for (MovieGenre genre : movie.getGenreList())
            user.getUserAnalysis().updateGenreTendency(genre.getGenre(), Math.round(deltaScoreSum * UserAnalysis.scorePoint));
        // 평균 평점 저장하기
        AvgScore avgScore = movie.getAvgScore();
        avgScore.setAllAvgScore(deltaScoreSum, deltaScoreCount);
        if (10 <= user.getLevel()) avgScore.setLevelAvgScore(deltaScoreSum, deltaScoreCount);
        if (user.getIsCertified()) avgScore.setCinephileAvgScore(deltaScoreSum, deltaScoreCount);
        avgScoreRepository.save(avgScore);
    }

    public void scoreTypeRefresh(Long userId, ScoreTypeEvent scoreTypeEvent) {

        if (scoreTypeEvent == ScoreTypeEvent.NOTHING)
            return ;
        List<Score> scoreListToRefresh = scoreRepository.findAllByUserId(userId);
        for (Score score : scoreListToRefresh) {
            AvgScore avgScore = score.getMovie().getAvgScore();
            if (scoreTypeEvent == ScoreTypeEvent.UP_LV10) {
                avgScore.setLevelAvgScore(score.getScore(), 1);
            }
            else if (scoreTypeEvent == ScoreTypeEvent.DOWN_LV10) {
                avgScore.setLevelAvgScore(-score.getScore(), -1);
            }
            else if (scoreTypeEvent == ScoreTypeEvent.CINEPHIL) {
                avgScore.setCinephileAvgScore(score.getScore(), 1);
            }
            else if (scoreTypeEvent == ScoreTypeEvent.UN_CINEPHIL) {
                avgScore.setCinephileAvgScore(-score.getScore(), -1);
            }
            avgScoreRepository.save(avgScore);
            avgScore = score.getMovie().getAvgScore();
        }
    }
}