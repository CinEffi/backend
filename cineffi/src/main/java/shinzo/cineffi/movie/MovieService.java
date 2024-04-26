package shinzo.cineffi.movie;

import com.google.common.collect.Lists;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import shinzo.cineffi.domain.entity.movie.*;
import shinzo.cineffi.domain.enums.Genre;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import static shinzo.cineffi.domain.enums.Genre.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepo;
    private final MovieGenreRepository movieGenreRepo;
    private final DirectorRepository directorRepo;
    private final ActorMovieRepository actorMovieRepo;
    private final ActorRepository actorRepo;
    private final AvgScoreRepository avgScoreRepo;

    @Value("${tmdb.access_token}")
    private String accessToken;
    @Value("${tmdb.api_key}")
    private String apiKey;
    @Value("${tmdb.base_url}")
    private String baseURL;
    @Value("${tmdb.path_image}")
    private String pathImage;
    @Value("${tmdb.path_data}")
    private String pathData;

    @PersistenceContext
    private EntityManager entityManager;
    private static final int MAX_PAGES = 500;
    private WebClient wc = WebClient.builder()
            .baseUrl(baseURL)
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();

    public void fetchTMDBIdsByDate() {
        int startYear = 2024; // 예를 들어 2000년부터 시작
        int endYear = LocalDate.now().getYear(); // 현재 연도까지
//        int endYear =  2024; //원하는 년도까지(수동수정)

        for (int year = startYear; year <= endYear; year++) {
            for (int month = 1; month <= 1; month++) {
                String startDate = String.format("%d-%02d-01", year, month);
//                String endDate = String.format("%d-%02d-%02d", year, month, YearMonth.of(year, month).lengthOfMonth());
                String endDate = String.format("%d-%02d-05", year, month); //5일치만 테스트

                List<Movie> movies = requestTMDBIds(startDate, endDate);
                initMovieData(movies);
            }
        }

    }

    private List<Movie> requestTMDBIds(String startDate, String endDate) {
        return Flux.range(1, MAX_PAGES) // 최대 페이지 수까지 Flux 생성
                .flatMap(page -> wc.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(pathData + "/discover/movie")
                                .queryParam("sort_by", "popularity.asc")
                                .queryParam("primary_release_date.gte", startDate)
                                .queryParam("primary_release_date.lte", endDate)
                                .queryParam("page", page)
                                .queryParam("api_key", apiKey)
                                .queryParam("with_runtime.gte", "40")
                                .queryParam("language", "en-US")
                                .build())
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(response -> new AbstractMap.SimpleEntry<>(page, response))
                        .onErrorResume(e -> Mono.empty())
                )
                .takeWhile(entry -> {
                    Map<String, Object> response = entry.getValue();
                    if (response == null) return false;
                    int totalPages = (int) response.get("total_pages");
                    return entry.getKey() <= (totalPages > MAX_PAGES ? MAX_PAGES : totalPages);
                })
                .flatMap(entry -> Flux.fromIterable((List<Map<String, Object>>) entry.getValue().get("results")))
                .map(result -> (Integer) result.get("id"))
                .filter(id -> !movieRepo.existsByTmdbId(id)) // 중복되는 ID 거르기
                .map(id -> Movie.builder().tmdbId(id).build())
                .map(movie -> (Movie) movieRepo.save(movie))
                .collectList()
                .block();
    }

    //영화 데이터 init 하기
    public void initMovieData(List<Movie> movieEmptys) {
        for (Movie movieEmpty : movieEmptys){
            Map<String, Object> detailData = getMovieDetailData(movieEmpty.getTmdbId());
            Movie movie = null;
            if (detailData != null) movie = makeMovieData(detailData, movieEmpty);

        }
    }

    //영화 상세정보 요청하기 for initMovieData()
    public Map<String, Object> getMovieDetailData (int tmdbId){
        try {
            return wc.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(pathData + "/movie/" + tmdbId)
                            .queryParam("language", "ko-KR")
                            .queryParam("append_to_response", "credits")
                            .queryParam("api_key", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            return null;  // 에러 발생 시 null 반환
        }
    }

    //이미지 데이터 요청하기 for makeMovieData()
    private byte[] getImg(String imagePath) {
        Mono<byte[]> poster = wc
                .get()
                .uri(pathImage + imagePath)
                .retrieve()
                .bodyToMono(byte[].class);
        // 이미지를 byte[]로 변환하여 반환
        return poster.block();
    }

    //한국어 문자열 장르를 이넘값으로
    private Genre genreKorToEnum(String genreStr){
        Map<String, Genre> genreMap = Map.ofEntries(
                Map.entry("액션", ACTION),
                Map.entry("모험", ADVENTURE),
                Map.entry("애니메이션", ANIMATION),
                Map.entry("코미디", COMEDY),
                Map.entry("범죄", CRIME),
                Map.entry("다큐멘터리", DOCUMENTARY),
                Map.entry("드라마", DRAMA),
                Map.entry("가족", FAMILY),
                Map.entry("판타지", FANTASY),
                Map.entry("역사", HISTORY),
                Map.entry("공포", HORROR),
                Map.entry("음악", MUSIC),
                Map.entry("미스터리", MYSTERY),
                Map.entry("로맨스", ROMANCE),
                Map.entry("SF", SCIENCE_FICTION),
                Map.entry("TV 영화", TV_MOVIE),
                Map.entry("스릴러", THRILLER),
                Map.entry("전쟁", WAR),
                Map.entry("서부", WESTERN)
        );
        if(genreMap.containsKey(genreStr)) return genreMap.get(genreStr);
        else throw new CustomException(ErrorMsg.FAILED_TO_MOVIE_PROCESS);
    }

    private List<MovieGenre> makeMovieGenre(List<String> genreStrs, Movie movie) {
        if( genreStrs.size() > 0 ) {
            return genreStrs.stream()
                    .map(genreStr -> MovieGenre.builder().movie(movie).genre(genreKorToEnum(genreStr)).build())
                    .map(movieGenreRepo::save)
                    .collect(Collectors.toList());
        }
        else {
            return new ArrayList<>();
        }
    }


    private Director saveDirector(List<Map<String, Object>> crews){
        Map<String, Object> crew = new HashMap<>();
        for (Map<String, Object> crewInMap : crews){
            if(crewInMap.get("job").equals("Director")) {
                crew = crewInMap;
                break;
            }
        }

        Optional<Director> directorOpt = directorRepo.findByName((String) crew.get("name"));

        if (directorOpt.isPresent()) return directorOpt.get();
        else{
            Director director = null;
            director = Director.builder()
                    .name((String) crew.get("name"))
                    .build();
            if ((String) crew.get("profile_path") != null){
                director = director.toBuilder()
                        .profileImage(getImg((String) crew.get("profile_path")))
                        .build();
            }
            return directorRepo.save(director);
        }
    }

    //응답 받은 데이터 가공 및 저장 for initMovieData()
    @Transactional
    public Movie makeMovieData(Map<String, Object> movieDetailData, Movie movie){
        //데이터 가공
        String newTitle = (String) movieDetailData.get("title");
        LocalDate newReleaseDate = LocalDate.parse((String) movieDetailData.get("release_date"));
        List<String> newOriginCountrys = (List<String>) movieDetailData.get("origin_country");
        List<Map<String, Object>> genres = (List<Map<String, Object>>) movieDetailData.get("genres");
        List<String> genreStrs = genres.stream().map(obj-> (String) obj.get("name")).collect(Collectors.toList());
        int newRuntime = (int) movieDetailData.get("runtime");
        String newIntroduction = (String) movieDetailData.get("overview");
        List<Map<String, Object>> crews = (List<Map<String, Object>>) ((Map<String, Object>) movieDetailData.get("credits")).get("crew");
        List<Map<String, Object>> casts = (List<Map<String, Object>>) ((Map<String, Object>) movieDetailData.get("credits")).get("cast");
        AvgScore avgScore = AvgScore.builder().build();
        avgScoreRepo.save(avgScore);

        //영화 + 감독 데이터 저장
        Movie newMovie = movie.toBuilder()
                .title(newTitle)
                .releaseDate(newReleaseDate)
                .originCountry(newOriginCountrys.size() != 0 ? newOriginCountrys.get(0) : null)
                .runtime(newRuntime)
                .introduction(newIntroduction)
                .director(saveDirector(crews))
                .avgScore(avgScore)
                .build();

        if((String) movieDetailData.get("poster_path") != null){
            byte[] newPoster = getImg((String) movieDetailData.get("poster_path"));
            newMovie = newMovie.toBuilder()
                    .poster(newPoster)
                    .build();
        }

        newMovie = newMovie.toBuilder()
                .genreList(makeMovieGenre(genreStrs, newMovie))
                .build();
        movieRepo.save(newMovie);

        //배우 데이터 저장
        saveActor(casts, newMovie);

        return newMovie;
    }

    private void saveActor(List<Map<String, Object>> casts, Movie movie){
        for (Map<String, Object> cast : casts){
            if((int) cast.get("order") < 8){
                Optional<Actor> present = actorRepo.findByName((String) cast.get("name"));
                Actor actor;
                if(present.isPresent()) actor = present.get();
                else{
                    actor = Actor.builder()
                            .name((String) cast.get("name"))
                            .build();

                    if ((String) cast.get("profile_path") != null) {
                        actor = actor.toBuilder()
                                .profileImage(getImg((String) cast.get("profile_path")))
                                .build();
                    }
                    actorRepo.save(actor);
                }

                ActorMovie actorMovie = ActorMovie.builder()
                        .character((String) cast.get("character"))
                        .orders((int) cast.get("order"))
                        .actor(actor)
                        .movie(movie)
                        .build();
                actorMovieRepo.save(actorMovie);
            }
        }
    }



    private final BoxOfficeApi boxOfficeApi;

    public void insertDailyBoxOffice() {
        boxOfficeApi.dailyBoxOffice();
    }
}
