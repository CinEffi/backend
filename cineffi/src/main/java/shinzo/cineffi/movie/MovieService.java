package shinzo.cineffi.movie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.movie.*;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.enums.Genre;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.*;
import shinzo.cineffi.score.repository.ScoreRepository;

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
    private final ScoreRepository scoreRepo;
    private final ScrapRepository scrapRepo;
    private final ActorMovieRepository actorMovieRepo;



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
                    .poster(encodeImage(movie.getPoster()))
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
                    .poster(encodeImage(movie.getPoster()))
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
                        .poster(encodeImage(movie.getPoster()))
                        .levelAvgScore(movie.getAvgScore().getLevelAvgScore())
                        .cinephileAvgScore(movie.getAvgScore().getCinephileAvgScore())
                        .build())
                .collect(Collectors.toList());

        return MovieSearchRespon.builder()
                .movieList(dtoList)
                .totalPageNum(pageList.getTotalPages())
                .build();
    }


    public MovieDetailDTO findMovieDetails(Long movieId, Long userId) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));
        boolean isScrap = (userId != null) && scrapRepo.existsByMovieIdAndUserId(movieId, userId);

        List<CrewListDTO> crewList = getActorAndDorectorList(movieId);
        Float myScore = (userId != null) ? getUserScoreForMovie(movieId, userId) : null;

        InMovieDetailDTO inMovieDetail = InMovieDetailDTO.builder()
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .poster(encodeImage(movie.getPoster()))
                .originCountry(movie.getOriginCountry())
                .genre(movie.getGenreList().stream().map(MovieGenre::getGenre).map(Enum::name).collect(Collectors.toList()))
                .build();

        return MovieDetailDTO.builder()
                .movie(inMovieDetail)
                .runtime(movie.getRuntime())
                .introduction(movie.getIntroduction())
                .cinephileAvgScore(movie.getAvgScore().getCinephileAvgScore())
                .levelAvgScore(movie.getAvgScore().getLevelAvgScore())
                .allAvgScore(movie.getAvgScore().getAllAvgScore())
                .myScore(myScore)
                .isScrap(isScrap)
                .crewList(crewList)
                .build();
    }


    private List<CrewListDTO> getActorAndDorectorList(Long movieId) { //배우, 감독 가져오기
        List<CrewListDTO> actors = actorMovieRepo.findByMovieId(movieId)
                .stream()
                .map(am -> CrewListDTO.builder()
                        .name(am.getActor().getName())
                        .profile(am.getActor().getProfileImage())
                        .job("Actor")
                        .character(am.getCharacter())
                        .build())
                .collect(Collectors.toList());

        Movie movie = movieRepo.findById(movieId).orElse(null);
        if (movie != null && movie.getDirector() != null) {
            actors.add(CrewListDTO.builder()
                    .name(movie.getDirector().getName())
                    .profile(movie.getDirector().getProfileImage())
                    .job("Director")
                    .character("")
                    .build());
        }
        return actors;
    }


    //내가 준 영화평점 (영화 상세페이지 조회)
    @Transactional(readOnly = true)
    public Float getUserScoreForMovie(Long movieId, Long userId) {
        return scoreRepo.findByMovieIdAndUserId(movieId, userId)
                .map(Score::getScore)
                .orElse(null);


    }

    public String encodeImage(byte[] imageData) {
        return Base64.getEncoder().encodeToString(imageData);
    }

}


