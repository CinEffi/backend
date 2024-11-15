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

    @Lob
    @Column(nullable = true)
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

    // 기본값 설정
    @PrePersist
    public void setDefaultValues() {
        this.view = 0;
        this.commentNumber = 0;
        this.likeNumber = 0;
    }

    public void increaseView() {
        this.view++;
    }

    public void increaseCommentNumber() {
        this.commentNumber++;
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
        return Objects.hash(id, title, content, writer, view, commentNumber, likeNumber);
    }

    public void setIsDelete(boolean isDelete) {
        super.setIsDelete(isDelete);
    }
}
