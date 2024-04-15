package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import shinzo.cineffi.domain.entity.BaseEntity;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvgScore extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "avg_score_id")
    private Long id;

    @Builder.Default
    @Column(columnDefinition = "NUMERIC(2,1)")
    private float allAvgScore = 0f;

    @Builder.Default
    @Column(columnDefinition = "NUMERIC(2,1)")
    private float cinephileAvgScore = 0f;

    @Builder.Default
    @Column(columnDefinition = "NUMERIC(2,1)")
    private float levelAvgScore = 0f;
}
