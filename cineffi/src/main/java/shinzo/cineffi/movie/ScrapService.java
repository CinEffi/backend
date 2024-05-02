package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import shinzo.cineffi.domain.dto.GetScrapRes;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.movie.Scrap;
import shinzo.cineffi.movie.repository.ScrapRepository;
import shinzo.cineffi.domain.dto.ScrapDto;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.crypto.codec.Utf8.decode;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final ScrapRepository scrapRepository;

    public GetScrapRes getUserScrapList(Long userId, Long loginUserId, Pageable pageable) {
        Page<Scrap> userScrapList = scrapRepository.findAllByUserId(userId, pageable);
        List<ScrapDto> scrapList = new ArrayList<>();

        int totalPageNum = userScrapList.getTotalPages();

        for (Scrap scrap : userScrapList) {
            Movie movie = scrap.getMovie();
            boolean isScrap = scrapRepository.existsByMovieIdAndUserId(movie.getId(), loginUserId);

            scrapList.add(ScrapDto.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .poster(decode(movie.getPoster()))
                    .userScore(4) // score 엔티티 생성되면 수정 필요
                    .releaseDate(movie.getReleaseDate())
                    .isScrap(isScrap)
                    .build());
        }

        return GetScrapRes.builder()
                .scrapList(scrapList)
                .totalPageNum(totalPageNum).build();

    }

}
