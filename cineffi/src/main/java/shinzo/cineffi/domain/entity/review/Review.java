package shinzo.cineffi.domain.entity.review;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.movie.Movie;
import shinzo.cineffi.domain.entity.user.User;

@Entity
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert

public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @Column(length = 1000)
    @ColumnDefault("''")
    private String content;

    @ColumnDefault("0")
    private Integer likeNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    public Review addLikeNum() {likeNum++; return this;}
    public Review subLikeNum() {likeNum--; return this;}
}
