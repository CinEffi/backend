package shinzo.cineffi.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.auth.AuthService;
import shinzo.cineffi.domain.dto.AuthRequestDTO;
import shinzo.cineffi.domain.entity.movie.AvgScore;
import shinzo.cineffi.domain.entity.movie.Director;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.review.Review;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.movie.repository.AvgScoreRepository;
import shinzo.cineffi.movie.repository.DirectorRepository;
import shinzo.cineffi.movie.repository.MovieRepository;
import shinzo.cineffi.review.repository.ReviewRepository;
import shinzo.cineffi.user.FollowService;
import shinzo.cineffi.user.repository.UserRepository;

import java.time.LocalDate;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class InitService {
    private final AuthService authService;
    private final AvgScoreRepository avgScoreRepository;
    private final DirectorRepository directorRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final FollowService followService;


    // Initialize 시 더미데이터 삽입 (테스트 유저, 영화, 평론)
    @PostConstruct
    @Transactional
    public void initialize() {
        // 테스트 유저 생성
        authService.authUser(AuthRequestDTO.builder()
                .nickname("첫째희석")
                .email("test@test.com")
                .password("test123!")
                .isauthentication(true)
                .build());

        authService.authUser(AuthRequestDTO.builder()
                .nickname("둘째민희")
                .email("minn@test.com")
                .password("test123!")
                .isauthentication(true)
                .build());

        authService.authUser(AuthRequestDTO.builder()
                .nickname("셋째제욱")
                .email("jjj@test.com")
                .password("test123!")
                .isauthentication(true)
                .build());

        Optional<User> 첫째희석 = userRepository.findById(1L);
        Optional<User> 둘째민희 = userRepository.findById(2L);
        Optional<User> 셋째제욱 = userRepository.findById(3L);

        // 영화 1
        AvgScore avgScore1 = avgScoreRepository.save(AvgScore.builder().allAvgScore(4.2f).cinephileAvgScore(3.8f).levelAvgScore(2.0f).build());
        Director director1 = directorRepository.save(Director.builder().name("허명행").build());
        Movie 범죄도시4 = movieRepository.save(Movie.builder()
                .avgScore(avgScore1)
                .title("범죄도시4")
                .director(director1)
                .introduction("신종 마약 사건 3년 뒤, 괴물형사 ‘마석도’(마동석)와 서울 광수대는 배달앱을 이용한 마약 판매 사건을 수사하던 중 수배 중인 앱 개발자가 필리핀에서 사망한 사건이 대규모 온라인 불법 도박 조직과 연관되어 있음을 알아낸다. 필리핀에 거점을 두고 납치, 감금, 폭행, 살인 등으로 대한민국 온라인 불법 도박 시장을 장악한 특수부대 용병 출신의 빌런 ‘백창기’(김무열)와 한국에서 더 큰 판을 짜고 있는 IT업계 천재 CEO ‘장동철’(이동휘). ‘마석도’는 더 커진 판을 잡기 위해 ‘장이수’(박지환)에게 뜻밖의 협력을 제안하고 광역수사대는 물론, 사이버수사대까지 합류해 범죄를 소탕하기 시작하는데… 나쁜 놈 잡는데 국경도 영역도 제한 없다! 업그레이드 소탕 작전! 거침없이 싹 쓸어버린다!")
                .originCountry("KO")
                .releaseDate(LocalDate.now())
                .runtime(100)
                .tmdbId(1)
                .build());
        // 영화 2
        AvgScore avgScore2 = avgScoreRepository.save(AvgScore.builder().allAvgScore(2.0f).cinephileAvgScore(3.7f).levelAvgScore(5.0f).build());
        Director director2 = directorRepository.save(Director.builder().name("마이클 미첼").build());
        Movie 쿵푸팬더4 = movieRepository.save(Movie.builder()
                .avgScore(avgScore2)
                .title("쿵푸팬더4")
                .director(director2)
                .introduction("오랜만이지! 드림웍스 레전드 시리즈 마침내 컴백! 마침내 내면의 평화… 냉면의 평화…가 찾아왔다고 믿는 용의 전사 ‘포’ 이젠 평화의 계곡의 영적 지도자가 되고, 자신을 대신할 후계자를 찾아야만 한다. “이제 용의 전사는 그만둬야 해요?” 용의 전사로의 모습이 익숙해지고 새로운 성장을 하기보다 지금 이대로가 좋은 ‘포’ 하지만 모든 쿵푸 마스터들의 능력을 그대로 복제하는 강력한 빌런 ‘카멜레온’이 나타나고 그녀를 막기 위해 정체를 알 수 없는 쿵푸 고수 ‘젠’과 함께 모험을 떠나게 되는데… 포는 가장 강력한 빌런과 자기 자신마저 뛰어넘고 진정한 변화를 할 수 있을까?")
                .originCountry("KO")
                .releaseDate(LocalDate.now())
                .runtime(200)
                .tmdbId(2)
                .build());
        // 영화 3
        AvgScore avgScore3 = avgScoreRepository.save(AvgScore.builder().allAvgScore(1.0f).cinephileAvgScore(3.0f).levelAvgScore(2.0f).build());
        Director director3 = directorRepository.save(Director.builder().name("장재현").build());
        Movie 파묘 = movieRepository.save(Movie.builder()
                .avgScore(avgScore3)
                .title("파묘")
                .director(director3)
                .introduction("미국 LA, 거액의 의뢰를 받은 무당 ‘화림’(김고은)과 ‘봉길’(이도현)은 기이한 병이 대물림되는 집안의 장손을 만난다. 조상의 묫자리가 화근임을 알아챈 ‘화림’은 이장을 권하고, 돈 냄새를 맡은 최고의 풍수사 ‘상덕’(최민식)과 장의사 ‘영근’(유해진)이 합류한다. “전부 잘 알 거야… 묘 하나 잘못 건들면 어떻게 되는지” 절대 사람이 묻힐 수 없는 악지에 자리한 기이한 묘. ‘상덕’은 불길한 기운을 느끼고 제안을 거절하지만, ‘화림’의 설득으로 결국 파묘가 시작되고… 나와서는 안될 것이 나왔다.")
                .originCountry("KO")
                .releaseDate(LocalDate.now())
                .runtime(120)
                .tmdbId(3)
                .build());

        // 첫째희석의 범죄도시4 평론
        reviewRepository.save(Review.builder()
                .user(첫째희석.get())
                .movie(범죄도시4)
                .content("인생 최고의 영화! 추천합니다")
                .score(2.0F)
                .build());

        // 둘째민희의 쿵푸팬더4 평론
        reviewRepository.save(Review.builder()
                .user(둘째민희.get())
                .movie(쿵푸팬더4)
                .content("우웨에에에에에에에에ㅔ게")
                .score(0.5F)
                .build());

        // 셋째제욱의 쿵푸팬더4 평론
        reviewRepository.save(Review.builder()
                .user(셋째제욱.get())
                .movie(쿵푸팬더4)
                .content("나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. 나쁘진 않은듯. ")
                .score(1.5F)
                .build());

        // 팔로우
        followService.followUser(1L, 2L);
        followService.followUser(1L, 3L);
        followService.followUser(2L, 1L);

    }
}
