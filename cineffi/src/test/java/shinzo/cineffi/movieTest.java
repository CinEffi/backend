package shinzo.cineffi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.movie.AvgScore;
import shinzo.cineffi.domain.entity.movie.Director;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.movie.repository.AvgScoreRepository;
import shinzo.cineffi.movie.repository.DirectorRepository;
import shinzo.cineffi.movie.repository.MovieRepository;

import java.time.LocalDate;

@SpringBootTest
public class movieTest {

    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private AvgScoreRepository avgScoreRepository;
    @Autowired
    private DirectorRepository directorRepository;

    @Rollback(value = false)
    @Test
    @Transactional
    void test () {
        AvgScore avgScore = avgScoreRepository.save(AvgScore.builder().allAvgScore(4.0f).cinephileAvgScore(3.0f).levelAvgScore(2.0f).build());
        Director 유명한_감독 = directorRepository.save(Director.builder().name("유명한 감독").build());
        movieRepository.save(Movie.builder()
                .avgScore(avgScore)
                .title("데드풀")
                .director(유명한_감독)
                .introduction("재밌는 영화")
                .originCountry("KO")
                .releaseDate(LocalDate.now())
                .runtime(100)
                .tmdbId(1)
                .build());
    }
}
