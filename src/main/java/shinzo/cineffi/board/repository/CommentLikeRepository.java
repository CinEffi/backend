package shinzo.cineffi.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.board.Comment;
import shinzo.cineffi.domain.entity.board.CommentLike;
import shinzo.cineffi.domain.entity.board.Post;
import shinzo.cineffi.domain.entity.board.PostLike;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByUserAndComment(User user, Comment comment);

    List<CommentLike> findAllByCommentId(Long commentId);
}
