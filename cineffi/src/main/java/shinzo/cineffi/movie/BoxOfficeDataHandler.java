package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import shinzo.cineffi.domain.entity.movie.AvgScore;
import shinzo.cineffi.domain.entity.movie.BoxOfficeMovie;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.movie.repository.BoxOfficeMovieRepository;
import shinzo.cineffi.movie.repository.MovieRepository;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Component
@RequiredArgsConstructor
public class BoxOfficeDataHandler {

    private final BoxOfficeMovieRepository boxOfficeMovieRepository;
    private final MovieRepository movieRepository;

    @Value("${kobis.api_key1}")
    private String KOBIS_API_KEY1;
    @Value("${kobis.api_key2}")
    private String KOBIS_API_KEY2;
    @Value("${kobis.api_key3}")
    private String KOBIS_API_KEY3;
    @Value("${kobis.api_key4}")
    private String KOBIS_API_KEY4;
    @Value("${kobis.api_key5}")
    private String KOBIS_API_KEY5;
    @Value("${kobis.api_key6}")
    private String KOBIS_API_KEY6;
    @Value("${kobis.api_key7}")
    private String KOBIS_API_KEY7;
    @Value("${kobis.api_key8}")
    private String KOBIS_API_KEY8;

    public void dailyBoxOffice() {
        //요청 인터페이스들
        //전날 박스오피스 조회
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        String targetDt = time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));


//        System.out.println("==================================================");
//        System.out.println("박스오피스 BoxOfficeDataHandler.dailyBoxOffice() 시작");
        HttpClient client = HttpClient.newBuilder()
                .build();
        String url = String.format("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=%s&targetDt=%s", KOBIS_API_KEY6, targetDt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();


        try {
            boxOfficeMovieRepository.deleteByTargetDt(targetDt);  //해당날의 데이터를 삭제하고 시작

//            System.out.println("박스오피스 BoxOfficeDataHandler 요청 보내기");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//            System.out.println("박스오피스 BoxOfficeDataHandler 요청 보내기 완료");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
            JSONObject boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");
            JSONArray dailyBoxOfficeList = (JSONArray) boxOfficeResult.get("dailyBoxOfficeList");


            int limit = Math.min(dailyBoxOfficeList.size(), 10);
            for (int i = 0; i < limit; i++) {
                JSONObject dailyBoxOffice = (JSONObject) dailyBoxOfficeList.get(i);

                if(!movieRepository.existsMovieByTitle(dailyBoxOffice.get("movieNm").toString())) continue;

                //JSON obiject -> java Object(Entity) 변환
                BoxOfficeMovie boxOfficeMovie = BoxOfficeMovie.builder()
                        .rank(dailyBoxOffice.get("rank").toString())
                        .title(dailyBoxOffice.get("movieNm").toString())
                        .targetDt(targetDt)
                        .build();

                boxOfficeMovieRepository.save(boxOfficeMovie);
            }

            processDailyBoxOfficeData(); //데이터 병합 메서드 호출

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //KOBIS에서 가져온 박스오피스 데이터를 기존DB에 저장된 데이터와 합치기
    public void processDailyBoxOfficeData() {
        List<BoxOfficeMovie> boxOfficeMovies = boxOfficeMovieRepository.findAll(); //일단 전체 일별 박스오피스 가져옴
        for (BoxOfficeMovie boxOfficeMovie : boxOfficeMovies) {
            Optional<Movie> movieOpt = movieRepository.findByTitle(boxOfficeMovie.getTitle());
           if (!movieOpt.isEmpty()) {
                Movie movie = movieOpt.get(); //일피하는 첫 번째 영화 선택 (가정: 가장 관련성 높은 영화)
                AvgScore avgScore = movie.getAvgScore();

                BoxOfficeMovie updateBoxOfficeMovie = BoxOfficeMovie.builder()
                        .id(boxOfficeMovie.getId())
                        .movieId(movie.getId())
                        .rank(boxOfficeMovie.getRank())
                        .title(boxOfficeMovie.getTitle())
                        .targetDt(boxOfficeMovie.getTargetDt())
                        .releaseDate(movie.getReleaseDate())
                        .poster(decodeImage(movie.getPoster()))
                        .cinephileAvgScore(avgScore != null ? avgScore.getCinephileAvgScore() : null)
                        .levelAvgScore(avgScore != null ? avgScore.getLevelAvgScore() : null)
                        .build();

                boxOfficeMovieRepository.save(updateBoxOfficeMovie);
            }

        }
    }

}
