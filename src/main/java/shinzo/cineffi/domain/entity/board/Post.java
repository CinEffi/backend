package shinzo.cineffi.domain.entity.board;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User writer;

    @Column(nullable = false)
    private Integer view;

    @Column(nullable = false)
    private Integer commentNumber;

    @Column(nullable = false)
    private Integer likeNumber;

    @PrePersist
    public void setDefaultValues() {
        this.view = 0;
        this.commentNumber = 0;
        this.likeNumber = 0;
    }

}
