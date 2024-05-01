package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.Scrap;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.movie.repository.ScrapRepository;
import shinzo.cineffi.user.GetScrapRes;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.crypto.codec.Utf8.decode;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;

    public List<GetScrapRes> getUserScrapList(Long userId, Long loginUserId) {
        List<GetScrapRes> getScrapResList = new ArrayList<>();
        scrapRepository.findAllByUserId(userId).forEach(scrap -> {
            Movie movie = scrap.getMovie();
            boolean isScrap = scrapRepository.existsByMovieIdAndUserId(movie.getId(), loginUserId);

            getScrapResList.add(GetScrapRes.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .poster(decode(movie.getPoster()))
                    .userScore(4) // score 엔티티 생성되면 수정 필요
                    .releaseDate(movie.getReleaseDate())
                    .isScrap(isScrap)
                    .build());
        });

        return getScrapResList;

    }

}
