package shinzo.cineffi.score;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.MovieRepository;
import shinzo.cineffi.score.repository.ScoreRepository;
import shinzo.cineffi.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public Long scoreMovie(Float score, Long movieId, Long userId) {

        if (score == null || score < 0.5 || 5 < score || score % 0.5 != 0)
            throw new CustomException(ErrorMsg.INVALID_SCORE_VALUE);
        // 유저 확인
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorMsg.UNAUTHORIZED_MEMBER));
        // 평점 매길 영화 조회, 찾기
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));
        // 평론 생성 + DB에 저장하기

        Score scoreData = scoreRepository.findByMovieAndUser(movie, user);
        if (scoreData == null) scoreData = Score.builder().score(score).user(user).movie(movie).build();
        else {
            scoreRepository.save(scoreData.toBuilder()
                    .score(score)
                    .build());
        }
        scoreRepository.save(scoreData);
        return scoreData.getId();
    }
}