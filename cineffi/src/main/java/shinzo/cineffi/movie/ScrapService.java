package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.dto.GetScrapRes;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.Scrap;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.movie.repository.ScrapRepository;
import shinzo.cineffi.domain.dto.ScrapDto;
import shinzo.cineffi.score.repository.ScoreRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.crypto.codec.Utf8.decode;
import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;
    private final ScoreRepository scoreRepository;

    @Transactional(readOnly = true)
    public GetScrapRes getUserScrapList(Long userId, Long loginUserId, Pageable pageable) {
        Page<Scrap> userScrapList = scrapRepository.findAllByUserId(userId, pageable);
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

}
