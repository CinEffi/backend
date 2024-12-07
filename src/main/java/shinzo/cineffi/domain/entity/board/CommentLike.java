package shinzo.cineffi.domain.entity.board;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

@Where(clause = "is_delete = false")
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_like_id")
    private Long id;

    @JoinColumn(nullable = false, name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(nullable = false, name = "comment_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Comment comment;

    public void setIsDelete(boolean isDelete) {
        super.setIsDelete(isDelete);
    }
}
