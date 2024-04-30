package shinzo.cineffi.movie;


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
    @Value("${tmdb.path_movie}")
    private String pathMovie;

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
                String startDate = String.format("%d-%02d-16", year, month);
//                String endDate = String.format("%d-%02d-%02d", year, month, YearMonth.of(year, month).lengthOfMonth());
                String endDate = String.format("%d-%02d-18", year, month); //테스트

                List<Movie> movies = requestTMDBIds(startDate, endDate);
                initMovieData(movies);
            }
        }

    }

    private List<Movie> requestTMDBIds(String startDate, String endDate) {
        return Flux.range(1, MAX_PAGES) // 최대 페이지 수까지 Flux 생성
                .flatMap(page -> wc.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(pathMovie + "/discover/movie")
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

    private Map<String, Object> requestPeople(int personId){
        return wc.get()
                .uri(uriBuilder -> uriBuilder
                        .path(pathMovie + "/person/" + personId)
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e-> {
                    return Mono.just(new HashMap<String, Object>());
                })
                .block();
    }

    //영화 데이터 init 하기
    public void initMovieData(List<Movie> movieEmptys) {
        for (Movie movieEmpty : movieEmptys){
            Map<String, Object> detailData = requestMovieDetailData(movieEmpty.getTmdbId());
            Movie movie = null;
            if (detailData != null && (int) detailData.get("runtime") != 0) movie = makeMovieData(detailData, movieEmpty);
            else movieRepo.delete(movieEmpty);
        }
    }

    //영화 상세정보 요청하기 for initMovieData()
    private Map<String, Object> requestMovieDetailData(int tmdbId){
        return wc.get()
                .uri(uriBuilder -> uriBuilder
                        .path(pathMovie + "/movie/" + tmdbId)
                        .queryParam("language", "ko-KR")
                        .queryParam("append_to_response", "credits")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(e -> {
                    return Mono.just(new HashMap<String, Object>());
                })
                .block();
    }

    //이미지 데이터 요청하기 for makeMovieData()
    public byte[] requestImg(String imagePath) {
        return wc.get()
                .uri(uriBuilder -> uriBuilder
                        .path(pathImage + imagePath)
                        .build())
                .retrieve()
                .bodyToMono(byte[].class)
                .onErrorResume(e -> {
                    return Mono.just(new byte[1]);
                })
                .block();
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
        if( genreStrs.size() <= 0 ) return new ArrayList<>();

        return genreStrs.stream()
                .map(genreStr -> MovieGenre.builder().movie(movie).genre(genreKorToEnum(genreStr)).build())
                .map(movieGenreRepo::save)
                .collect(Collectors.toList());

    }

    private Director saveDirector(List<Map<String, Object>> crews){
        Map<String, Object> crew = new HashMap<>();
        for (Map<String, Object> crewInMap : crews){
            if(crewInMap.get("job").equals("Director")) {
                crew = crewInMap;
                break;
            }
        }
        if(crew.isEmpty())return null;

        int tmdbId = (int) crew.get("id");
        Optional<Director> directorOpt = directorRepo.findByTmbdId(tmdbId);
        if (directorOpt.isPresent()) return directorOpt.get();
        else{
            Director director = null;
            director = Director.builder()
                    .name(nameToKor(tmdbId))
                    .tmdbId(tmdbId)
                    .build();

            byte[] img = requestImg((String) crew.get("profile_path"));
            if (img != null){
                director = director.toBuilder()
                        .profileImage(img)
                        .build();
            }
            return directorRepo.save(director);
        }
    }

    private String nameToKor(int tmdbId) {
        Map<String, Object> people = requestPeople(tmdbId);
        if (people == null || people.isEmpty()) return "이름없음";

        String defaultName = (String) people.get("name");
        if (defaultName == null) return "이름없음";

        List<String> also_known_as = (List<String>) people.get("also_known_as");
        if (also_known_as != null && !also_known_as.isEmpty()) {
            for (String name : also_known_as) {
                if (name.matches("^[가-힣]+$")) {
                    return name;  // 한글 이름 발견 시 반환
                }
            }
        }

        return defaultName;  // 한글 이름이 없다면 기본 이름 반환
    }

    //응답 받은 데이터 가공 및 저장 for initMovieData()
    private Movie makeMovieData(Map<String, Object> movieDetailData, Movie movie){
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

        byte[] img = requestImg((String) movieDetailData.get("poster_path"));
        if(img != null){
            newMovie = newMovie.toBuilder()
                    .poster(img)
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
                int tmdbId = (int) cast.get("id");
                Optional<Actor> actorOpt = actorRepo.findByTmdbId(tmdbId);
                Actor actor;
                if(actorOpt.isPresent()) actor = actorOpt.get();
                else{
                    actor = Actor.builder()
                            .name(nameToKor(tmdbId))
                            .tmdbId(tmdbId)
                            .build();


                    byte[] img = requestImg((String) cast.get("profile_path"));
                    if (img != null) {
                        actor = actor.toBuilder()
                                .profileImage(img)
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


    private final BoxOfficeDataHandler boxOfficeDataHandler;
    private final DailyMovieRepository dailyMovieRepository;

    public void insertDailyBoxOffice() {
        boxOfficeDataHandler.dailyBoxOffice();

    }

    public List<DailyMovie> getEnhancedDailyMovies() {
        return dailyMovieRepository.findAll();
    }

}
