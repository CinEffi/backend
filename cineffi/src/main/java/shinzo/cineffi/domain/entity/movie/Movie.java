package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Type;
import shinzo.cineffi.domain.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    private String title;

    @Column(columnDefinition = "DATE")
    private LocalDate releaseDate;

    @Lob
    private byte[] poster;
    private String originCountry;
    private String[] genre;

    @Column(columnDefinition = "TIME")
    private LocalTime runtime;
    private String introduction;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avgScore_id")
    private AvgScore avgScore;

}
