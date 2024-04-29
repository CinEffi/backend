package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.movie.DailyMovie;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.movie.repository.DailyMovieRepository;
import shinzo.cineffi.movie.repository.MovieRepository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BoxOfficeDataHandler {

    private final DailyMovieRepository dailyMovieRepository;
    private final MovieRepository movieRepository;

    @Value("${kobis.api_key}")
    private String apiKey;



    public void dailyBoxOffice() {
        //요청 인터페이스들
        //전날 박스오피스 조회
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        String targetDt = time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));


        HttpClient client = HttpClient.newHttpClient();
        String url = String.format("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=%s&targetDt=%s", apiKey, targetDt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();


        try {
            dailyMovieRepository.deleteByTargetDt(targetDt);  //해당날의 데이터를 삭제하고 시작


            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
            JSONObject boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");
            JSONArray dailyBoxOfficeList = (JSONArray) boxOfficeResult.get("dailyBoxOfficeList");


            int limit = Math.min(dailyBoxOfficeList.size(), 10);
            for (int i = 0; i < limit; i++) {
                JSONObject dailyBoxOffice = (JSONObject) dailyBoxOfficeList.get(i);

                //JSON obiject -> java Object(Entity) 변환
                DailyMovie dailyMovie = DailyMovie.builder()
                        .rank(dailyBoxOffice.get("rank").toString())
                        .title(dailyBoxOffice.get("movieNm").toString())
                        .targetDt(targetDt)
                        .build();

                dailyMovieRepository.save(dailyMovie);
            }

            processDailyBoxOfficeData(); //데이터 병합 메서드 호출

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    //KOBIS에서 가져온 박스오피스 데이터를 기존DB에 저장된 데이터와 합치기
    public void processDailyBoxOfficeData() {
        List<DailyMovie> dailyMovies = dailyMovieRepository.findAll(); //일단 전체 일별 박스오피스 가져옴
        for (DailyMovie dailyMovie : dailyMovies) {
            List<Movie> movies = movieRepository.findByTitleIgnoringSpaces(dailyMovie.getTitle());
            if (!movies.isEmpty()) {
                Movie movie = movies.get(0); //일피하는 첫 번째 영화 선택 (가정: 가장 관련성 높은 영화)
                DailyMovie updateDailyMovie = DailyMovie.builder()
                        .id(dailyMovie.getId()) //기존 DailyMovie의 id 유지
                        .rank(dailyMovie.getRank())
                        .title(dailyMovie.getTitle())
                        .targetDt(dailyMovie.getTargetDt())
                        .releaseDate(movie.getReleaseDate())
                        .poster(movie.getPoster())
                        .build();

                dailyMovieRepository.save(updateDailyMovie);
            }

        }
    }

}
