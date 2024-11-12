package shinzo.cineffi.board.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.board.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPostIdOrderByCreatedAtAsc(Long postId, Pageable pageable);
}
