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

@Component
@RequiredArgsConstructor
public class BoxOfficeDataHandler {

    private final BoxOfficeMovieRepository boxOfficeMovieRepository;
    private final MovieRepository movieRepository;

    @Value("${kobis.api_key1}")
    private String API_KEY;



    public void dailyBoxOffice() {
        //요청 인터페이스들
        //전날 박스오피스 조회
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        String targetDt = time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));


        System.out.println("==================================================");
        System.out.println("박스오피스 BoxOfficeDataHandler.dailyBoxOffice() 시작");
        HttpClient client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("krmp-proxy.9rum.cc", 3128)))  // 프록시 호스트 및 포트
                .build();
        String url = String.format("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=%s&targetDt=%s", API_KEY, targetDt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();


        try {
            boxOfficeMovieRepository.deleteByTargetDt(targetDt);  //해당날의 데이터를 삭제하고 시작

            System.out.println("박스오피스 BoxOfficeDataHandler 요청 보내기");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("박스오피스 BoxOfficeDataHandler 요청 보내기 완료");
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());
            JSONObject boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");
            JSONArray dailyBoxOfficeList = (JSONArray) boxOfficeResult.get("dailyBoxOfficeList");


            int limit = Math.min(dailyBoxOfficeList.size(), 10);
            for (int i = 0; i < limit; i++) {
                JSONObject dailyBoxOffice = (JSONObject) dailyBoxOfficeList.get(i);

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
            List<Movie> movies = movieRepository.findByTitleIgnoringSpaces(boxOfficeMovie.getTitle());
            if (!movies.isEmpty()) {
                Movie movie = movies.get(0); //일피하는 첫 번째 영화 선택 (가정: 가장 관련성 높은 영화)
                AvgScore avgScore = movie.getAvgScore();

                BoxOfficeMovie updateBoxOfficeMovie = BoxOfficeMovie.builder()
                        .id(boxOfficeMovie.getId()) //기존 DailyMovie의 id 유지
                        .rank(boxOfficeMovie.getRank())
                        .title(boxOfficeMovie.getTitle())
                        .targetDt(boxOfficeMovie.getTargetDt())
                        .releaseDate(movie.getReleaseDate())
                        .poster(encodeImage(movie.getPoster()))
                        .cinephileAvgScore(avgScore != null ? avgScore.getCinephileAvgScore() : null)
                        .levelAvgScore(avgScore != null ? avgScore.getLevelAvgScore() : null)
                        .build();

                boxOfficeMovieRepository.save(updateBoxOfficeMovie);
            }

        }
    }

    public String encodeImage(byte[] imageData) {
        String baseImgStr = "data:image/png;base64,";
        String result = Base64.getEncoder().encodeToString(imageData);

        return (baseImgStr + result);
    }

}
