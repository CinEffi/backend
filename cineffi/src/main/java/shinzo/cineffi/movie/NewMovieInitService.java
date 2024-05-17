package shinzo.cineffi.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import shinzo.cineffi.domain.entity.movie.*;
import shinzo.cineffi.domain.enums.Genre;
import shinzo.cineffi.domain.enums.ImageType;
import shinzo.cineffi.domain.enums.InitType;
import shinzo.cineffi.movie.repository.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

import static shinzo.cineffi.domain.enums.ImageType.POSTER;
import static shinzo.cineffi.domain.enums.ImageType.PROFILE;
import static shinzo.cineffi.domain.enums.InitType.*;

@Service
@RequiredArgsConstructor
public class NewMovieInitService {
    private final MovieRepository movieRepo;
    private final MovieGenreRepository movieGenreRepo;
    private final DirectorRepository directorRepo;
    private final ActorMovieRepository actorMovieRepo;
    private final ActorRepository actorRepo;
    private final AvgScoreRepository avgScoreRepo;
    private final BoxOfficeDataHandler boxOfficeDataHandler;
    private final RestTemplate restTemplate;

    @Value("${tmdb.access_token}")
    private String TMDB_ACCESS_TOKEN;
    @Value("${tmdb.api_key}")
    private String TMDB_API_KEY;
    @Value("${tmdb.base_url}")
    private String TMDB_BASEURL;
    @Value("${tmdb.path_poster}")
    private String TMDB_PATH_POSTER;
    @Value("${tmdb.path_profile}")
    private String TMDB_PATH_PROFILE;
    @Value("${tmdb.path_movie}")
    private String TMDB_PATH_MOVIE;
    private static final int MAX_PAGES = 500;

    @Value("${kobis.api_key}")
    private String KOBIS_API_KEY;
    private final String KOBIS_BASEURL = "http://www.kobis.or.kr/kobisopenapi/webservice/rest";
    private final DateTimeFormatter KOBIS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final int THREAD_MAX = 100; // 동시에 처리할 스레드 수

    public void initData(int year){
        List<Movie> TMDBBasicDatas = getTMDBBasicDatasByDate(year);
        List<Movie> kobisBasicDatas = requestKobisDatas(year);
        List<Movie> mixBasicDatas = returnMIxDatas(TMDBBasicDatas, kobisBasicDatas);
        requestDetailDatas(mixBasicDatas);

        if(LocalDate.now().getYear() == year) {
            boxOfficeDataHandler.dailyBoxOffice();
        }

    }

    private List<Movie> getTMDBBasicDatasByDate(int year) {
        List<Movie> result = new ArrayList<>();
        List<Future<List<Movie>>> futures = new ArrayList<>();

        ExecutorService executorService = null;
        for (int month = 1; month <= 12; month++) {
            executorService = Executors.newFixedThreadPool(THREAD_MAX);
            final int finalMonth = month;
            String startDate = String.format("%d-%02d-01", year, finalMonth);
            String endDate = String.format("%d-%02d-%02d", year, finalMonth, YearMonth.of(year, finalMonth).lengthOfMonth());

            // 각 월별 데이터 요청을 스레드풀에 제출
            Future<List<Movie>> future = executorService.submit(() -> {
                return getTMDBBasicDatasInThread(startDate, endDate);
            });
            futures.add(future);
        }

        // 모든 Future 결과를 수집
        for (Future<List<Movie>> future : futures) {
            try {
                result.addAll(future.get());
            } catch (Exception e) {
                System.err.println("TMDB 기본 데이터 : " + e.getMessage());
            }
        }
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return result;
    }

    private List<Movie> getTMDBBasicDatasInThread(String startDate, String endDate) {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_MAX);
        List<Movie> movies = new ArrayList<>();
        List<Future<List<Movie>>> futures = new ArrayList<>();
        int maxPage = MAX_PAGES;

        //첫 요청은 그냥 보내서 응답의 총페이지 확인
        Object[] firstPageMovies = requestTMDBBasicData(1, startDate, endDate);
        futures.add(executorService.submit(() -> (List<Movie>) firstPageMovies[0]));

        //위 응답의 총페이지 값을 확인하고 maxPage를 업데이트
        int totalPage = (int) firstPageMovies[1]; // API 응답에서 max_page 추출
        maxPage = Math.min(MAX_PAGES, totalPage);

        //남은 페이지 모두 요청
        if(totalPage >= 2) {
            for (int page = 2; page <= maxPage; page++) {
                final int currentPage = page;
                Callable<List<Movie>> task = () -> (List<Movie>) requestTMDBBasicData(currentPage, startDate, endDate)[0];
                futures.add(executorService.submit(task));
            }
        }

        for (Future<List<Movie>> future : futures) {
            try {
                movies.addAll(future.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        return movies;
    }
    private Object[] requestTMDBBasicData(int page, String startDate, String endDate) {
        List<Movie> resultMovie = new ArrayList<>();
        int maxPage = 0;
        Map<String, Object> response = (Map<String, Object>) requestData(String.format("%s%s/discover/movie?api_key=%s&language=ko-KR&include_adult=false&page=%d&release_date.gte=%s&release_date.lte=%s&with_runtime.gte=40&region=KR",
                TMDB_BASEURL, TMDB_PATH_MOVIE, TMDB_API_KEY, page, startDate, endDate), TMDB_MOVIE);

        if (response != null) {
            List<Map<String, Object>> maps = (List<Map<String, Object>>) response.get("results");
            maxPage = (int) response.get("total_pages");
            for (Map<String, Object> map : maps) {
                if(map == null) continue;
                Movie movie = TMDBMapToMovie(map);
                if (!movieRepo.existsByTmdbId(movie.getTmdbId())) {
                    resultMovie.add(movie);
                }
            }
        }
        return new Object[]{resultMovie, maxPage};
    }
    private List<Movie> requestKobisDatas(int year) {
        List<Movie> result = new ArrayList<>();
        int curPage = 1;
        int totalPage = 100; // 초기 추정치

        while (curPage <= totalPage) {
            Map<String, Object> response = (Map<String, Object>) requestData(String.format("%s/movie/searchMovieList.json?key=%s&openStartDt=%s&openEndDt=%s&itemPerPage=100&curPage=%d",
                    KOBIS_BASEURL, KOBIS_API_KEY, year, year, curPage), KOBIS);

            Map<String, Object> results = (Map<String, Object>) response.get("movieListResult");
            int totCnt = (int) results.get("totCnt"); // 전체 콘텐트 개수
            totalPage = (totCnt + 99) / 100; // 전체 페이지 수 계산 (올림 처리)

            List<Map<String, Object>> movieMapList = (List<Map<String, Object>>) results.get("movieList");
            if (movieMapList != null && !movieMapList.isEmpty()) {
                for (Map<String, Object> movieMap : movieMapList) {
                    if(((String) movieMap.get("genreAlt")).contains("에로")) continue;
                    Movie kobisMovie = kobisMapToMovie(movieMap);
                    result.add(kobisMovie);
                }
            }

            curPage++; // 다음 페이지로
        }

        return result;
    }

    private List<Movie> returnMIxDatas(List<Movie> TMDBMovies, List<Movie> kobisMovies) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_MAX);
        Set<String> deDuplication = Collections.newSetFromMap(new ConcurrentHashMap<>());

        List<Future<Movie>> futures = new ArrayList<>();
        for (Movie TMDBMovie : TMDBMovies) {
            Future<Movie> future = executor.submit(() -> {
                return processMovie(TMDBMovie, kobisMovies, deDuplication);
            });
            futures.add(future);
        }

        List<Movie> result = new ArrayList<>();
        for (Future<Movie> future : futures) {
            try {
                Movie movie = future.get();
                if (movie != null) {
                    result.add(movie);
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        return totalSave(result); // 데이터베이스 저장 호출
    }

    private Movie processMovie(Movie TMDBMovie, List<Movie> kobisMovies, Set<String> deDuplication) {
        String korTitleMapKey = makeNoBlankStr(TMDBMovie.getTitle());
        if (deDuplication.contains(korTitleMapKey)) return null;
        Set<Integer> yearSet = new HashSet<>();
        if(!yearSet.contains(TMDBMovie.getReleaseDate().getYear()) && TMDBMovie.getReleaseDate().getYear() < LocalDate.now().getYear()){
            yearSet.add(TMDBMovie.getReleaseDate().getYear());
            List<Movie> newKobisMovies = requestKobisDatas(TMDBMovie.getReleaseDate().getYear());
            kobisMovies.addAll(newKobisMovies);
        }
        //코비스 데이터 규격화
        Map<String, Movie> kobisKorTitleMap = new ConcurrentHashMap<>();
        Map<String, Movie> kobisEngTitleMap = new ConcurrentHashMap<>();
        for (Movie kobisMovie : kobisMovies) {
            kobisKorTitleMap.put(makeNoBlankStr(kobisMovie.getTitle()), kobisMovie);
            if (kobisMovie.getEngTitle() != null) {
                kobisEngTitleMap.put(makeNoBlankStr(kobisMovie.getEngTitle()).toLowerCase(), kobisMovie);
            }
        }

        synchronized (deDuplication) {
            if (kobisKorTitleMap.containsKey(korTitleMapKey)) {
                deDuplication.add(korTitleMapKey);
                return kobisKorTitleMap.get(korTitleMapKey)
                        .toBuilder()
                        .poster(TMDBMovie.getPoster())
                        .tmdbId(TMDBMovie.getTmdbId())
                        .introduction(TMDBMovie.getIntroduction())
                        .build();
            }
            if (TMDBMovie.getEngTitle() != null) {
                String engTitleMapKey = makeNoBlankStr(TMDBMovie.getEngTitle()).toLowerCase();
                if (kobisEngTitleMap.containsKey(engTitleMapKey)) {
                    deDuplication.add(engTitleMapKey);
                    return kobisEngTitleMap.get(engTitleMapKey)
                            .toBuilder()
                            .poster(TMDBMovie.getPoster())
                            .tmdbId(TMDBMovie.getTmdbId())
                            .build();
                }
            }
        }
        return null;
    }

    private void requestDetailDatas(List<Movie> movies) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_MAX);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();
        List<Movie> result = Collections.synchronizedList(new ArrayList<>());
        Map<String, Movie> movieMap = new ConcurrentHashMap<>();

        for (Movie movie : movies) {
            movieMap.put(movie.getKobisCode(), movie);
            Future<Map<String, Object>> future = executor.submit(() -> requestDetailData(movie));
            futures.add(future);
        }

        futures.parallelStream().forEach(future -> {
            try {
                Map<String, Object> data = future.get(30, TimeUnit.SECONDS); // 결과가 준비될 때까지 기다림
                if (data == null || data.isEmpty()) return;

                String movieMapKey = (String) ((Map<String, Object>) ((Map<String, Object>) data.get("kobisDetails")).get("movieInfo")).get("movieCd");
                Movie updatedMovie = detailDataMapToMovie(data, movieMap.get(movieMapKey));
                if (updatedMovie != null) result.add(updatedMovie);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태를 복원
            } catch (ExecutionException e) {
            } catch (TimeoutException e) {
            }
        });

        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                }
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        totalSave(result);
    }

    private Map<String, Object> requestDetailData(Movie movie){
        Map<String, Object> result = new HashMap<>();
        String kobisCode = movie.getKobisCode();
        int tmdbId = movie.getTmdbId();

        // KOBIS 요청 수행
        Map<String, Object> kobisDetails = (Map<String, Object>) ((Map<String, Object>) requestData(KOBIS_BASEURL + "/movie/searchMovieInfo.json?key=" + KOBIS_API_KEY + "&movieCd=" + kobisCode, KOBIS)).get("movieInfoResult");

        // TMDB 요청 수행
        Map<String, Object> tmdbDetails = (Map<String, Object>) requestData(TMDB_BASEURL + TMDB_PATH_MOVIE + "/movie/" + tmdbId + "?api_key=" + TMDB_API_KEY + "&language=ko-KR&append_to_response=credits", TMDB_MOVIE);

        if (!tmdbDetails.isEmpty() && !kobisDetails.isEmpty()) {
            result.put("kobisDetails", kobisDetails);
            result.put("tmdbDetails", tmdbDetails);
        }

        return result;
    }
    private Object requestData(String urlString, InitType type) {
        Object result = new HashMap<>();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            headers.set("Connection", "keep-alive");

            if (type.equals(TMDB_MOVIE)) headers.set("Authorization", "Bearer " + TMDB_ACCESS_TOKEN);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            Object response = null;
            if(type.equals(TMDB_IMG)) response = restTemplate.getForObject(urlString, byte[].class, entity);
            else response = parseJson(restTemplate.getForObject(urlString, String.class, entity));

            if (response != null) result = response;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    public List<Movie> totalSave(List<Movie> dataList){
        for (Movie data : dataList){
            if(data != null) movieRepo.save(data);

        }
        return dataList;
    }

    //문자열 검사 혹은 규격에 맞추는 메서드들
    private String makeURLStr(String blankStr){
        if(blankStr == null) return null;
        String result = "";
        for (int i = 0; i < blankStr.length(); i++) {
            if(blankStr.charAt(i) != ' ') result += blankStr.charAt(i);
            else result += "%20";
        }
        return result;
    }
    private String makeNoBlankStr(String blankStr){
        if(blankStr == null) return null;
        String result = "";
        for (int i = 0; i < blankStr.length(); i++) {
            if(blankStr.charAt(i) != ' ') result += blankStr.charAt(i);
        }
        return result;
    }
    private Map<String, Object> parseJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, Map.class);
    }
    private boolean isKorStr(String str){
        if(str == null) return false;
        for (char ch : str.toCharArray()) {
            if (!(ch >= 0xAC00 && ch <= 0xD7A3)) {
                return false; // 한글 범위를 벗어나는 문자가 있다면 false 반환
            }
        }
        return true;
    }

    //요청으로 받은 데이터를 우리 영화 데이터로
    private Movie TMDBMapToMovie(Map<String, Object> map) {
        Integer id = (Integer) map.get("id");
        String title = (String) map.get("title");
        String introduction = (String) map.get("overview");
        LocalDate releaseDate = LocalDate.parse((String) map.get("release_date"));
        String engTitle = "";
        if(((String) map.get("original_language")).equals("en")) engTitle = (String) map.get("original_title");

        Movie result = Movie.builder()
                .tmdbId(id)
                .title(title)
                .introduction(introduction)
                .releaseDate(releaseDate).build();
        if(!engTitle.equals("")) result = result.toBuilder().engTitle(engTitle).build();

        return result;
    }
    private Movie kobisMapToMovie(Map<String, Object> map) {
        String movieCode = (String) map.get("movieCd");
        String title = (String) map.get("movieNm");

        Movie movie = Movie.builder()
                .kobisCode(movieCode)
                .title(title)
                .build();

        if(map.get("movieNmEn") != null && !map.get("movieNmEn").equals("")) {
            String engTitle = (String) map.get("movieNmEn");
            movie = movie.toBuilder().engTitle(engTitle).build();
        }
        if(map.get("openDt") != null && !map.get("openDt").equals("")) {
            LocalDate releaseDate = LocalDate.parse((String) map.get("openDt"), KOBIS_DATE_FORMATTER);
            movie =  movie.toBuilder().releaseDate(releaseDate).build();
        }
        return movie;

    }

    @Transactional
    public Movie detailDataMapToMovie(Map<String, Object> map, Movie data){
        //반환값
        Movie result = data;
        Map<String, Object> kobisResponse = (Map<String, Object>) ((Map<String, Object>) map.get("kobisDetails")).get("movieInfo");
        Map<String, Object> tmdbResponse = (Map<String, Object>) map.get("tmdbDetails");
        Integer runtime = (Integer) tmdbResponse.get("runtime");
        if(runtime == 0) return null;
        List<Map<String, Object>> nations = (List<Map<String, Object>>) kobisResponse.get("nations");
        String nation = nations.size() == 0 ? null : (String) nations.get(0).get("nationNm");
        List<Map<String, Object>> kobisDirectors = kobisResponse.get("directors") == null ? new ArrayList<>() : (List<Map<String, Object>>) kobisResponse.get("directors");
        List<Map<String, Object>> kobisActors = kobisResponse.get("actors") == null ? new ArrayList<>() : (List<Map<String, Object>>) kobisResponse.get("actors");
        Map<String, Object> credits = (Map<String, Object>) tmdbResponse.get("credits");
        List<Map<String, Object>> castMaps = credits.get("cast") == null ? new ArrayList<>() : (List<Map<String, Object>>) credits.get("cast");
        List<Map<String, Object>> crewMaps = credits.get("crew") == null ? new ArrayList<>() : (List<Map<String, Object>>) credits.get("crew");
        List<Map<String, Object>> genreMaps = tmdbResponse.get("genres") == null ? new ArrayList<>() : (List<Map<String, Object>>) tmdbResponse.get("genres");
        byte[] poster = requestImg((String) tmdbResponse.get("poster_path"), POSTER);

        AvgScore avgScore = AvgScore.builder().build();
        avgScoreRepo.save(avgScore);

        List<MovieGenre> genres = new ArrayList<>();
        if(!genreMaps.isEmpty()) {
            for (Map<String, Object> genreMap : genreMaps) {
                Genre genre = Genre.getEnum((String) genreMap.get("name"));
                MovieGenre mg = MovieGenre.builder()
                        .genre(genre)
                        .movie(data)
                        .build();
                movieGenreRepo.save(mg);
                genres.add(mg);
            }
        }

        result = result.toBuilder()
                .originCountry(nation)
                .runtime(runtime)
                .avgScore(avgScore)
                .poster(poster)
                .genreList(genres)
                .build();

        if(kobisDirectors.size() != 0) {
            String directorName = (String) kobisDirectors.get(0).get("peopleNm");
            String directorEngName = (String) kobisDirectors.get(0).get("peopleNmEn");

            Director director = Director.builder()
                    .name(directorName)
                    .engname(directorEngName)
                    .build();

            for(Map<String, Object> crewMap : crewMaps) {
                if ((crewMap.get("job")).equals("Director")) {
                    byte[] profileImg = requestImg((String) crewMap.get("profile_path"), PROFILE);
                    director = director.toBuilder()
                            .tmdbId((Integer) crewMap.get("id"))
                            .engname((String) crewMap.get("original_name"))
                            .profileImage(profileImg)
                            .build();
                    break;
                }
            }
            directorRepo.save(director);

            result = result.toBuilder()
                    .director(director)
                    .build();
        }
        if(kobisActors.size() != 0){
            for (int i = 0; i < kobisActors.size(); i++) {
                String actorKorName = (String) kobisActors.get(i).get("peopleNm");
                String actorEngName = (String) kobisActors.get(i).get("peopleNmEn");
                String cast = (String) kobisActors.get(i).get("cast");

                Actor actor = Actor.builder()
                        .name(actorKorName)
                        .engName(actorEngName)
                        .build();
                ActorMovie actorMovie = ActorMovie.builder()
                        .character(cast)
                        .movie(result)
                        .build();
                for (Map<String, Object> castMap : castMaps){
                    if(!castMap.containsKey("order")) continue;

                    Integer tmdbId = (Integer) castMap.get("id");
                    Integer TMDBOrder = (Integer) castMap.get("order");
                    String TMDBKorName = makeNoBlankStr((String) castMap.get("name"));
                    String TMDBProfilePath = (String) castMap.get("profile_path");
                    String TMDBEngName = castMap.get("original_name") == null ? null : makeNoBlankStr((String) castMap.get("original_name")).toLowerCase();

                    if((TMDBKorName.equals(makeNoBlankStr(actor.getName()))) ||
                            (TMDBEngName != null && TMDBEngName.equals(makeNoBlankStr(actor.getEngName()).toLowerCase()))){
                        actor = actor.toBuilder()
                                .tmdbId(tmdbId)
                                .profileImage(requestImg(TMDBProfilePath, PROFILE))
                                .build();
                        actorMovie = actorMovie.toBuilder()
                                .actor(actor)
                                .orders(TMDBOrder)
                                .build();
                        actorRepo.save(actor);
                        actorMovieRepo.save(actorMovie);
                        break;
                    }

                }
            }
        }

        return result;
    }

    //이미지 데이터 요청하기
    private byte[] requestImg(String imagePath, ImageType type) {
        if (imagePath == null) return returnDefaultImg(type);

        try {
            // Directly get byte array from the restTemplate
            byte[] imageBytes = (byte[]) requestData(TMDB_BASEURL + (type.equals(POSTER) ? TMDB_PATH_POSTER : TMDB_PATH_PROFILE) + imagePath + "?key=" + KOBIS_API_KEY, TMDB_IMG);
            if(imageBytes != null && imageBytes.length > 0) return imageBytes;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnDefaultImg(type);
    }
    private byte[] returnDefaultImg(ImageType type) {
        if (type.equals(POSTER)) {
            String defaultPoster1 = "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AROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEw4VoGjIARMAJGwAhUgIAJtwKQHYURMAJGwAgYAROuZcAIGAEjYASMQAUImHArANlRGAEjYASMgBEYSML985//7JI1AkbACBgBI5DOOuus2qBgwq1NUTghRsAIGAEj0E0Eek2+A0G4zUC0lttNsXXYRsAIGIH6I1DkgWZcUWUu+opwc7CKQOag5f5yd98bASNgBIzA4CMAB4gjsHVPzvNnVXNF7QhXwORAcP+d73wn/f73v0+ffPJJOnPmTFzcf/755+lPf/pTAKp3JU55GHKzbQSMgBEwAoODgPgBjjj33HPT+eefnyZMmJAuvPDCdMEFF6RzzjlniHB7zRG1I1zIUwBy/+WXX6ZPP/00vf/+++nkyZNhf/TRR+H22WefpS+++CIIF/HhPS5AxbYx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";
            String defaultPoster2 = "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";
            return Base64.getDecoder().decode(defaultPoster1 + defaultPoster2);
        }
        else {
            String defaultProfileImage = "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";
            return Base64.getDecoder().decode(defaultProfileImage);

        }
    }
    private byte[] resizeImg(byte[] img){
        byte[] result = null;
        //원본 이미지 가져오기
        try {
            BufferedImage originalImg = ImageIO.read(new ByteArrayInputStream(img));

            //이미지 리사이즈
            Image resizeImage = originalImg.getScaledInstance(200, 200, Image.SCALE_SMOOTH);

            //새 이미지 저장
            BufferedImage newImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
            Graphics g = newImage.getGraphics();
            g.drawImage(resizeImage, 0, 0, null);
            g.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(newImage, "jpg", output);
            return output.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();

        }

        return result;
    }

}