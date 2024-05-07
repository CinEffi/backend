package shinzo.cineffi.domain.entity.user;


import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.domain.enums.Genre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserAnalysis {

    private static final String[] scoreTendency = new String[] {
            "영화의 무덤", "무자비한 비평의 칼날", "평작 도살자", "섬세한 비평가",
            "균형을 수호하는 중재자", "실용주의자", "걱정 마세요 감독님!",
            "나는 관대하다", "영화라면 뭐든 좋아!", "만점 앵무새"
    };

    private static final String[] genreTendency = new String[] {
            "액션", "모험", "애니메이션", "코미디",
            "범죄", "다큐멘터리", "드라마", "가족", "판타지",
            "역사", "공포", "음악", "미스터리", "로맨스",
            "SF", "TV 영화", "스릴러", "전쟁", "서부"
    };

    public final static int reviewPoint = 15;
    public final static float scorePoint = 2.0f;
    public final static int scrapPoint = 4;


    @Id
    @Column(name = "user_id")
    private Long id;

    //
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @ColumnDefault("0.0")
    private Float scoreSum = 0.0f;

    @Builder.Default
    @ColumnDefault("0")
    private Integer scoreNum = 0;


    @Builder.Default
    @ColumnDefault("0")
    private Integer scoreLabelIndex = 0 ;

    @Builder.Default
    @ColumnDefault("0")
    private Integer genreLabelIndex = 0;

    @ElementCollection
    @Column(name = "genre_record", columnDefinition = "INTEGER[]")
    private List<Integer> genreRecord;

    @PrePersist
    private void initializeGenreRecord() {
        if (this.genreRecord == null) this.genreRecord = new ArrayList<>(Arrays.asList(new Integer[19]));
    }
    // 평점 5개 이상부터 생깁니다.
    public void updateScoreTendency(Float deltaScore, Integer deltaCount) {
        this.scoreSum += deltaScore;
        this.scoreNum += deltaCount;
        this.scoreLabelIndex = Math.round(CinEffiUtils.averageScore(scoreSum, scoreNum) * 2);
    }

    public void updateGenreTendency(Genre genre, int score) {
        int challenger = genre.ordinal();
        int newRecord = this.genreRecord.get(challenger) + score;
        this.genreRecord.set(challenger, newRecord);

        int champion = this.genreLabelIndex;
        if (champion != challenger) {
            int oldRecord = this.genreRecord.get(champion);
            if (oldRecord < newRecord)
                this.genreLabelIndex = challenger;
        }
    }

    public String getScoreTendency() {
        return this.scoreNum < 5 ? null : scoreTendency[this.scoreLabelIndex];
    }

    public String getGenreTendency() {
        return 10 < genreRecord.get(this.genreLabelIndex) ? null : genreTendency[this.genreLabelIndex];
    }
}