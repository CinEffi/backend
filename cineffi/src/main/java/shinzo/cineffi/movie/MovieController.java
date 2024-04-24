package shinzo.cineffi.movie;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import shinzo.cineffi.domain.entity.movie.Movie;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MovieController {
    private final MovieService movieService;

    @GetMapping("/api/movie/init")
    public ResponseEntity<List<Movie>> init(){
        List<Movie> result = movieService.initMovieData();
        return ResponseEntity.ok(result);
    }


}
