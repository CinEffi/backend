package shinzo.cineffi.domain.entity.board;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;
import shinzo.cineffi.domain.entity.BaseEntity;
import shinzo.cineffi.domain.entity.user.User;

import java.util.Objects;

@Where(clause = "is_delete = false")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "post_id")
    private Post post;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User writer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Integer likeNumber;

    @PrePersist
    public void setDefaultValues() {
        this.likeNumber = 0;
    }

    @Builder
    public Comment(Post post, User writer, String content) {
        this.post = post;
        this.writer = writer;
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // 같은 객체이면 true
        if (o == null) return false; // 비교하는 객체가 null 이면 false

        Post that = (Post) o;
        if (this.getId() == null || that.getId() == null) return false; // id가 null이면 false

        return Objects.equals(this.getId(), that.getId()); // 엔티티 Id로 비교
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, post, writer, content, likeNumber);
    }

    public void setIsDelete(boolean isDelete) {
        super.setIsDelete(isDelete);
    }

    public void increaseLikeNumber() {
        this.likeNumber++;
    }

    public void decreaseLikeNumber() {
        this.likeNumber--;
    }

}
