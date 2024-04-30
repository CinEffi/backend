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

    @Column(columnDefinition = "NUMERIC(2,1)")
    @ColumnDefault("0")
    private Float allAvgScore;

    @Column(columnDefinition = "NUMERIC(2,1)")
    @ColumnDefault("0")
    private Float cinephileAvgScore;

    @Column(columnDefinition = "NUMERIC(2,1)")
    @ColumnDefault("0")
    private Float levelAvgScore;
}
