package shinzo.cineffi.movie;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Component;
import shinzo.cineffi.domain.entity.movie.AvgScore;
import shinzo.cineffi.domain.entity.movie.BoxOfficeMovie;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.movie.repository.BoxOfficeMovieRepository;
import shinzo.cineffi.movie.repository.MovieRepository;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Slf4j
@Component
@RequiredArgsConstructor
public class BoxOfficeDataHandler {

    private final BoxOfficeMovieRepository boxOfficeMovieRepository;
    private final MovieRepository movieRepository;
    private final KobisService kobisService;

    public void dailyBoxOffice() {
        //요청 인터페이스들
        //전날 박스오피스 조회
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        String targetDt = time.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<BoxOfficeMovie> paramList = new ArrayList<>();
        boxOfficeMovieRepository.deleteByTargetDt(targetDt);  //해당날의 데이터를 삭제하고 시작

        JSONArray dailyBoxOfficeList = request(targetDt);

        int limit = Math.min(dailyBoxOfficeList.size(), 10);
        for (int i = 0; i < limit; i++) {
            JSONObject dailyBoxOffice = (JSONObject) dailyBoxOfficeList.get(i);

            LocalDate openDt = LocalDate.parse(dailyBoxOffice.get("openDt").toString(), DateTimeFormatter.ISO_DATE);
            if(!movieRepository.existsByTitleAndReleaseDate(dailyBoxOffice.get("movieNm").toString(), openDt)) continue;

            //JSON obiject -> java Object(Entity) 변환
            BoxOfficeMovie boxOfficeMovie = BoxOfficeMovie.builder()
                    .rank(dailyBoxOffice.get("rank").toString())
                    .title(dailyBoxOffice.get("movieNm").toString())
                    .releaseDate(openDt)
                    .targetDt(targetDt)
                    .build();

            paramList.add(boxOfficeMovie);
        }

        processDailyBoxOfficeData(paramList); //데이터 병합 메서드 호출

    }

    private JSONArray request(String targetDt) {
        HttpClient client = HttpClient.newBuilder()
                .build();
        String url = String.format("http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.json?key=%s&targetDt=%s", kobisService.curKobisKey, targetDt);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        JSONArray dailyBoxOfficeList = null;
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.body());

            boolean isfault = jsonObject.containsKey("faultInfo");
            while (isfault){
                kobisService.nextKobisKey();
                response = client.send(request, HttpResponse.BodyHandlers.ofString());

                jsonParser = new JSONParser();
                jsonObject = (JSONObject) jsonParser.parse(response.body());
                isfault = jsonObject.containsKey("faultInfo");
            }

            JSONObject boxOfficeResult = (JSONObject) jsonObject.get("boxOfficeResult");
            dailyBoxOfficeList = (JSONArray) boxOfficeResult.get("dailyBoxOfficeList");

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return dailyBoxOfficeList;
    }

    //KOBIS에서 가져온 박스오피스 데이터를 기존DB에 저장된 데이터와 합치기
    @Transactional
    public void processDailyBoxOfficeData(List<BoxOfficeMovie> boxOfficeMovies) {
//        List<BoxOfficeMovie> boxOfficeMovies = boxOfficeMovieRepository.findAll(); //일단 전체 일별 박스오피스 가져옴
        for (BoxOfficeMovie boxOfficeMovie : boxOfficeMovies) {
            Optional<Movie> movieOpt = movieRepository.findByTitleAndReleaseDate(boxOfficeMovie.getTitle(), boxOfficeMovie.getReleaseDate());
            if (movieOpt.isEmpty()) continue;

            Movie movie = movieOpt.get(); //일피하는 첫 번째 영화 선택 (가정: 가장 관련성 높은 영화)
            AvgScore avgScore = movie.getAvgScore();
            BoxOfficeMovie updateBoxOfficeMovie = BoxOfficeMovie.builder()
                    .movieId(movie.getId())
                    .rank(boxOfficeMovie.getRank())
                    .title(boxOfficeMovie.getTitle())
                    .targetDt(boxOfficeMovie.getTargetDt())
                    .releaseDate(movie.getReleaseDate())
                    .poster(decodeImage(movie.getPoster()))
                    .cinephileAvgScore(avgScore.getCinephileAvgScore() != null ? avgScore.getCinephileAvgScore() : null)
                    .levelAvgScore(avgScore.getLevelAvgScore() != null ? avgScore.getLevelAvgScore() : null)
                    .build();
            boxOfficeMovieRepository.save(updateBoxOfficeMovie);

        }
    }

}
