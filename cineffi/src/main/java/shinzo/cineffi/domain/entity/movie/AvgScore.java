package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.Utils.CinEffiUtils;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvgScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avg_score_id")
    private Long id;

    @Builder.Default
    private Float allScoreSum = 0f;
    @Builder.Default
    private Integer allScoreCount = 0;

    @Builder.Default
    private Float levelScoreSum = 0f;
    @Builder.Default
    private Integer levelScoreCount = 0;

    @Builder.Default
    private Float cinephileScoreSum = 0f;
    @Builder.Default
    private Integer cinephileScoreCount = 0;

    public void setAllAvgScore(Float deltaSum, Integer deltaCount) {
        this.allScoreSum += deltaSum;
        this.allScoreCount += deltaCount;
    }
    public void setLevelAvgScore(Float deltaSum, Integer deltaCount) {
        this.levelScoreSum += deltaSum;
        this.levelScoreCount += deltaCount;
    }
    public void setCinephileAvgScore(Float deltaSum, Integer deltaCount) {
        this.cinephileScoreSum += deltaSum;
        this.cinephileScoreCount += deltaCount;
    }

    public Float getAllAvgScore() { return CinEffiUtils.averageScore(allScoreSum, allScoreCount); }
    public Float getLevelAvgScore () { return CinEffiUtils.averageScore(levelScoreSum, levelScoreCount); }
    public Float getCinephileAvgScore() { return CinEffiUtils.averageScore(cinephileScoreSum, cinephileScoreCount); }
}
