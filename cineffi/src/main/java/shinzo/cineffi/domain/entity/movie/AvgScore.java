package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.domain.entity.BaseEntity;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
public class AvgScore extends BaseEntity {

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


    public Float getAllAvgScore() {
        return 0.0F;
    }

//    @Column(columnDefinition = "NUMERIC(2,1)")
//    @ColumnDefault("0")
    public Float getCinephileAvgScore() {
        return 0.0F;
    }

    public Float getLevelAvgScore () {
        return 0.0F;
    }


}
