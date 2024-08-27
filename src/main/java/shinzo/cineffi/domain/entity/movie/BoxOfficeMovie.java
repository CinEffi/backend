package shinzo.cineffi.domain.entity.movie;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BoxOfficeMovie {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_box_Office_id")
    private Long id; // 영화

    private Long movieId;

    private String rank; //박스오피스 순위

    //DB 저장 일자
    private String targetDt;

    private String title;

    //나머지 상세 데이터들은 TMDB에서 받아오는 걸로!!!!(기억)
    private LocalDate releaseDate;

    @Lob
    private String poster;

    private Float cinephileAvgScore;
    private Float levelAvgScore;



}
