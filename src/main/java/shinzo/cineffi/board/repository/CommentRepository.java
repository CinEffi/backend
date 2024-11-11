package shinzo.cineffi.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.board.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
