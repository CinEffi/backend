package shinzo.cineffi.domain.entity.movie;

import jakarta.persistence.*;
import shinzo.cineffi.domain.enums.Genre;

@Entity
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_genre_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", referencedColumnName = "movie_id")
    private Movie movie;
}
