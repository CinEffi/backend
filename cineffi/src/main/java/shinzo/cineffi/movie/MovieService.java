package shinzo.cineffi.movie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.movie.*;
import shinzo.cineffi.domain.enums.Genre;
import shinzo.cineffi.movie.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import static shinzo.cineffi.domain.enums.Genre.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MovieService {
    private final MovieRepository movieRepo;
    private final BoxOfficeDataHandler boxOfficeDataHandler;
    private final BoxOfficeMovieRepository boxOfficeMovieRepository;

    public static Genre getEnumGenreBykorGenre(String korName) {
        for (Genre genre : Genre.values()) {
            if (genre.getGenre().equals(korName)) return genre;
        }
        return null;
    }

    public List<UpcomingMovieDTO> findUpcomingList(){
        List<Movie> upcomingList = movieRepo.findUpcomingList();
        List<UpcomingMovieDTO> result = new ArrayList<>();

        for (Movie movie : upcomingList){
            UpcomingMovieDTO dto = UpcomingMovieDTO.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .releaseDate(movie.getReleaseDate())
                    .poster(movie.getPoster())
                    .build();
            result.add(dto);
        }
        return result;
    }

    public GenreMovieListDTO findGenreList(){
        int randomPick = new Random().nextInt(values().length);
        Genre genre = Genre.values()[randomPick];
        Pageable pageable = (Pageable) PageRequest.of(0, 20);

        List<InListMoviveDTO> dtoList = new ArrayList<>();

        List<Movie> movieList = movieRepo.findGenreList(genre, pageable);
        for (Movie movie : movieList){
            InListMoviveDTO dto = InListMoviveDTO.builder()
                    .movieId(movie.getId())
                    .title(movie.getTitle())
                    .releaseDate(movie.getReleaseDate())
                    .poster(movie.getPoster())
                    .cinephileAvgScore(movie.getAvgScore().getCinephileAvgScore())
                    .levelAvgScore(movie.getAvgScore().getLevelAvgScore())
                    .build();
            dtoList.add(dto);
        }

        return new GenreMovieListDTO(genre, dtoList);
    }

    public void insertDailyBoxOffice() {
        boxOfficeDataHandler.dailyBoxOffice();

    }

    public List<BoxOfficeMovie> getEnhancedDailyMovies() {
        boxOfficeDataHandler.processDailyBoxOfficeData();
        return boxOfficeMovieRepository.findAll();
    }

    public MovieSearchRespon findSearchList(String q, int page, int size){
        Pageable pageable = PageRequest.of(page, size);
        q=q.trim();

        Genre genre = getEnumGenreBykorGenre(q);
        if(genre != null) q = genre.toString();

        Page<Movie> pageList = movieRepo.findSearchList(q, pageable);
        List<MovieDTO> dtoList = pageList.getContent().stream().map(movie -> MovieDTO.builder()
                        .movieId(movie.getId())
                        .title(movie.getTitle())
                        .releaseDate(movie.getReleaseDate())
                        .poster(movie.getPoster())
                        .levelAvgScore(movie.getAvgScore().getLevelAvgScore())
                        .cinephileAvgScore(movie.getAvgScore().getCinephileAvgScore())
                        .build())
                .collect(Collectors.toList());

        return MovieSearchRespon.builder()
                .movieList(dtoList)
                .totalPageNum(pageList.getTotalPages())
                .build();

    }

}