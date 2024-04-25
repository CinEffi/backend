package shinzo.cineffi.domain.entity.movie;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyBoxOffice {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "daily_box_Office_id")
    private Long id; // 영화

    private String rank; //박스오피스 순위

    //DB 저장 일자
    private String targetDt;

    private String audiAcc;

    //나머지 상세 데이터들은 TMDB에서 받아오는 걸로!!!!(기억)



}
