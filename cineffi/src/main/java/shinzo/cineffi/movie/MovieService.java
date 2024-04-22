package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.MovieRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepo;
    @Value("${tmdb.access_token}")
    private String accessToken;
    @Value("${tmdb.api_key}")
    private String apiKey;
    private WebClient wc = WebClient.builder()
            .baseUrl("https://api.themoviedb.org/3")
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();

    public List<Integer> testMovieId() {
        int minPage = 1;
        List<Integer> ids = new ArrayList<>();

        while(true) {
            final int currentPage = minPage;
            List<Integer> pageIds = wc.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/discover/movie")
                            .queryParam("include_adult", false)
                            .queryParam("include_video", false)
                            .queryParam("language", "en-US")
                            .queryParam("page", currentPage)
                            .queryParam("sort_by", "popularity.desc")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMapMany(response -> {
                        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                        return Flux.fromIterable(results)
                                .map(result -> (int) result.get("id"));
                    })
                    .collectList()
                    .block();

            if (pageIds != null) {
                ids.addAll(pageIds);
            }
            else break;

            if(currentPage % 49 == 0) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    new CustomException(ErrorMsg.DUPLICATE_EMAIL);
                }
            }

            minPage++;
        }
        return ids;
    }
}
