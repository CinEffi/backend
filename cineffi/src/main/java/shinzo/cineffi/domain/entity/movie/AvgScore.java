package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class AvgScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avg_score_id")
    private Long id;

    @ColumnDefault("0")
    private Float allScoreSum;
    @ColumnDefault("0")
    private Integer allScoreCount;

    @ColumnDefault("0")
    private Float levelScoreSum;
    @ColumnDefault("0")
    private Integer levelScoreCount;

    @ColumnDefault("0")
    private Float cinephileScoreSum;
    @ColumnDefault("0")
    private Integer cinephileScoreCount;

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
    private static Float averageScore(Float sum, Integer count) {
        return 0 < count ? Math.round((sum / count) * 10.0f) / 10.0f : null;
    }

    public Float getAllAvgScore() { return averageScore(allScoreSum, allScoreCount); }
    public Float getLevelAvgScore () { return averageScore(levelScoreSum, levelScoreCount); }
    public Float getCinephileAvgScore() { return averageScore(cinephileScoreSum, cinephileScoreCount); }
}
