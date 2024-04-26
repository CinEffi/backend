package shinzo.cineffi.movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.movie.DailyMovie;
import shinzo.cineffi.movie.repository.DailyBoxOfficeRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;


@Component
@RequiredArgsConstructor
@Transactional
public class BoxOfficeApi {

    private final DailyBoxOfficeRepository dailyBoxOfficeRepository;

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
            //해당날의 데이터를 삭제하고 시작
            dailyBoxOfficeRepository.deleteByTargetDt(targetDt);

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
                        .movieNm(dailyBoxOffice.get("movieNm").toString())
                        .targetDt(targetDt)
                        .build();

                dailyBoxOfficeRepository.save(dailyMovie);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
