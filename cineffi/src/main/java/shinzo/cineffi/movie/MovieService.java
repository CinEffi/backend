package shinzo.cineffi.movie;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.movie.*;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.domain.entity.score.Score;
import shinzo.cineffi.domain.enums.Genre;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.movie.repository.*;
import shinzo.cineffi.review.repository.ReviewRepository;
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
    private final ReviewRepository reviewRepository;

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

        return new GenreMovieListDTO(genre.getKor(), dtoList);
    }

    public void insertDailyBoxOffice() {
        boxOfficeDataHandler.dailyBoxOffice();

    }

    public List<BoxOfficeMovie> getEnhancedDailyMovies() {
        boxOfficeDataHandler.processDailyBoxOfficeData();
        return boxOfficeMovieRepository.findAll();
    }

    public List<MovieDTO> findSearchList(String q){
        final int totalResultNum = 20;
        List<MovieDTO> result = new ArrayList<>();
        Set<String> titleSet = new HashSet<>();
        Pageable pageable = PageRequest.of(0, totalResultNum);
        q=q.trim();

        List<Movie> searchList = movieRepo.findSearchList(q, pageable);
        List<MovieDTO> dtoList = searchList.stream()
                .map(movie -> MovieDTO.builder()
                        .movieId(movie.getId())
                        .title(movie.getTitle())
                        .releaseDate(movie.getReleaseDate())
                        .poster(encodeImage(movie.getPoster()))
                        .levelAvgScore(movie.getAvgScore().getLevelAvgScore())
                        .cinephileAvgScore(movie.getAvgScore().getCinephileAvgScore())
                        .build())
                .collect(Collectors.toList());
        titleSet.addAll(dtoList.stream().map(MovieDTO::getTitle).toList());

        Genre genre = Genre.getEnum(q);
        if(genre != null) {
            List<Movie> genreList = movieRepo.findGenreList(genre, pageable);
            List<MovieDTO> dtoList2 = genreList.stream()
                    .map(movie -> MovieDTO.builder()
                            .movieId(movie.getId())
                            .title(movie.getTitle())
                            .releaseDate(movie.getReleaseDate())
                            .poster(encodeImage(movie.getPoster()))
                            .levelAvgScore(movie.getAvgScore().getLevelAvgScore())
                            .cinephileAvgScore(movie.getAvgScore().getCinephileAvgScore())
                            .build())
                    .collect(Collectors.toList());

                if(!dtoList2.isEmpty()) {
                    for (MovieDTO dto : dtoList2) {
                        if (!titleSet.contains(dto.getTitle())) dtoList.add(dto);
                    }
                }
        }

        result.addAll(dtoList.subList(0, Math.min(20, dtoList.size() - 1)));

        return result;
    }


    public MovieDetailDTO findMovieDetails(Long movieId, Long userId) {
        Movie movie = movieRepo.findById(movieId)
                .orElseThrow(() -> new CustomException(ErrorMsg.MOVIE_NOT_FOUND));
        boolean isScrap = (userId != null) && scrapRepo.existsByMovieIdAndUserId(movieId, userId);


        List<CrewListDTO> crewList = getActorAndDirectorList(movieId);
        Float myScore = (userId != null) ? getUserScoreForMovie(movieId, userId) : null;

        Review existingReview = userId != null ? reviewRepository.findByMovieAndUserIdAndIsDeleteFalse(movie, userId) : null;
        InMovieDetailDTO inMovieDetail = InMovieDetailDTO.builder()
                .movieId(movie.getId())
                .movieTitle(movie.getTitle())
                .releaseDate(movie.getReleaseDate())
                .poster(encodeImage(movie.getPoster()))
                .originCountry(movie.getOriginCountry())
                .genre(movie.getGenreList().stream().map(MovieGenre::getGenre).map(Genre::getKor).collect(Collectors.toList()))
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
                .existingReviewId(existingReview != null ? existingReview.getId() : null)
                .existingReviewContent(existingReview != null ? existingReview.getContent() : null)
                .crewList(crewList)
                .build();
    }

    private List<CrewListDTO> getActorAndDirectorList(Long movieId) { //배우, 감독 가져오기
        List<CrewListDTO> crewList = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 8);

        Movie movie = movieRepo.findById(movieId).orElse(null);
        if (movie != null && movie.getDirector() != null) {
            crewList.add(CrewListDTO.builder()
                    .name(movie.getDirector().getName())
                    .profile(encodeImage(movie.getDirector().getProfileImage()))
                    .job("Director")
                    .character("")
                    .build());
        }

        List<CrewListDTO> actors = actorMovieRepo.findByMovieId(movieId, pageable)
                .stream()
                .map(am -> CrewListDTO.builder()
                        .name(am.getActor().getName())
                        .profile(encodeImage(am.getActor().getProfileImage()))
                        .job("Actor")
                        .character(am.getCharacter())
                        .build())
                .collect(Collectors.toList());

        crewList.addAll(actors);
        return crewList;
    }


    //내가 준 영화평점 (영화 상세페이지 조회)
    @Transactional(readOnly = true)
    public Float getUserScoreForMovie(Long movieId, Long userId) {
        Score score = scoreRepo.findByMovieIdAndUserId(movieId, userId);
        return (score != null) ? score.getScore() : null;
    }

    public String encodeImage(byte[] imageData) {
        String baseImgStr = "data:image/png;base64,";
        String result = Base64.getEncoder().encodeToString(imageData);

        return baseImgStr + result;
    }

}


