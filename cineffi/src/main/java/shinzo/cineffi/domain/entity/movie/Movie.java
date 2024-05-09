package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.domain.entity.BaseEntity;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@DynamicInsert
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

    @OneToMany(mappedBy = "movie")
    private List<MovieGenre> genreList;

    private Integer runtime;
    @Lob
    private String introduction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private Director director;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avg_score_id")
    private AvgScore avgScore;

    private int tmdbId;
    private String engTitle;

    private String kobisCode;

}
