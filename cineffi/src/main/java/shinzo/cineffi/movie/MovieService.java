package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.MovieRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<Integer> fetchAllMovieIds() {
        List<Integer> ids = new ArrayList<>();
        int startYear = 2023; // 예를 들어 2000년부터 시작
        int endYear = LocalDate.now().getYear(); // 현재 연도까지

        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 12; month++) {
                String startDate = String.format("%d-%02d-01", year, month);
                String endDate = String.format("%d-%02d-%02d", year, month, YearMonth.of(year, month).lengthOfMonth());
                ids.addAll(fetchMovieIdsByDate(startDate, endDate));
            }
        }

        return ids;
    }

    private List<Integer> fetchMovieIdsByDate(String startDate, String endDate) {
        List<Integer> ids = new ArrayList<>();
        int currentPage = 1;
        int totalPages = Integer.MAX_VALUE;

        while (currentPage <= totalPages) {
            int finalCurrentPage = currentPage;
            WebClient.ResponseSpec responseSpec = wc.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/discover/movie")
                            .queryParam("primary_release_date.gte", startDate)
                            .queryParam("primary_release_date.lte", endDate)
                            .queryParam("page", finalCurrentPage)
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve();

            Map<String, Object> response = responseSpec.bodyToMono(Map.class).block();
            if (response == null) break;

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            totalPages = (int) response.get("total_pages");
            ids.addAll(results.stream().map(result -> (int) result.get("id")).collect(Collectors.toList()));

            currentPage++;
            if (currentPage > totalPages) break;
        }
        return ids;
    }




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
                    .onErrorResume(WebClientResponseException.class, e -> {
                        // 로깅 또는 기타 에러 처리 로직
                        System.err.println("Error retrieving page " + currentPage + ": " + e.getMessage());
                        return Mono.just(new ArrayList<Integer>());
                    })
                    .retry(3)  // 네트워크 에러 발생 시 최대 3회 재시도
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
