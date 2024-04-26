package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final BoxOfficeApi boxOfficeApi;

    public void insertDailyBoxOffice() {
        boxOfficeApi.dailyBoxOffice();
    }
}
