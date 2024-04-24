package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
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
    @Value("${tmdb.path_data}")
    private String pathData;

    private WebClient wc = WebClient.builder()
            .baseUrl(baseURL)
            .defaultHeader(HttpHeaders.ACCEPT, "application/json")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .build();

    //영화 데이터 init 하기
    public List<Movie> initMovieData(){
        //테스트 영화id 저장
        for (int i = 3; i < 12; i++) {
            Movie movie = Movie.builder().tmdbId(i).build();
            movieRepo.save(movie);
        }

        List<Movie> movies = movieRepo.findAll();
        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);
            Map<String, Object> detailData = getMovieDetailData(movie.getTmdbId());

            Movie makeMovie = null;
            if(detailData == null) continue;
            else makeMovie = makeMovieData(detailData, movie);

        }
        movieRepo.flush();
        return movieRepo.findAll();
    }

    //영화 상세정보 요청하기 for initMovieData()
    public Map<String, Object> getMovieDetailData(int tmdbId){
        WebClient.ResponseSpec responseSpec = wc.get()
                .uri(uriBuilder -> uriBuilder
                        .path(pathData + "/movie/" + tmdbId)
                        .queryParam("language", "ko-KR")
                        .queryParam("append_to_response", "credits")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve();


        try {
            return responseSpec.bodyToMono(Map.class).block();
        } catch (Exception e) {
            return null;
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
            List<MovieGenre> nullList = new ArrayList<>();
            return nullList;
        }
    }

    private Director getDirector(List<Map<String, Object>> crews){
        Map<String, Object> crew = new HashMap<>();
        for (Map<String, Object> crewInMap : crews){
            if(crewInMap.get("job").equals("Director")) {
                crew = crewInMap;
                break;
            }
        }

        Director director = null;
        if(crew.size() != 0) {
            director = Director.builder()
                    .name((String) crew.get("name"))
                    .build();
            if ((String) crew.get("profile_path") != null){
                director = director.toBuilder()
                        .profileImage(getImg((String) crew.get("profile_path")))
                        .build();
            }
        }
        if(directorRepo.findByName(director.getName()).isEmpty()) return directorRepo.save(director);
        else return director;
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
                .director(getDirector(crews))
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


}
