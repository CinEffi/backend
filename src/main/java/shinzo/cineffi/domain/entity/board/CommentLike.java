package shinzo.cineffi.domain.entity.board;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.Where;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

@Where(clause = "is_delete = false")
@Entity
@Builder
@AllArgsConstructor
public class CommentLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Long id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
}
