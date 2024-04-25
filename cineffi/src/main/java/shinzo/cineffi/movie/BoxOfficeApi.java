package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import shinzo.cineffi.movie.repository.DailyBoxOfficeRepository;
import kr.or.kobis.kobisopenapi.consumer.rest.KobisOpenAPIRestService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class BoxOfficeApi {

    private final DailyBoxOfficeRepository dailyBoxOfficeRepository;

    String key = "5b5979d8f9822605465799f4de0d864a";

    public void dailyBoxOffice() {

        String dailyResponse = "";

        //요청 인터페이스들
        //전날 박스오피스 조회
        LocalDateTime time = LocalDateTime.now().minusDays(1);
        String targetDt = time.format(DateTimeFormatter.ofPattern("yyyMMdd"));

        //ROW 개수
        String itemPerPage = "10";

        //다양성영화(Y)/상업영화(N)/전체(default)
        String multiMovieYn = "";

        //한국영화(K)/외국영화(F)/전체(default)
        String repNationCd = "";

        //상영지역별 코드/전체(default)
        String wideAreaCd = "";


        try {
            //KOBIS 오픈 API Rest Client를 통해 호출
            KobisOpenAPIRestService service = new KobisOpenAPIRestService(key);
        }

    }
}
