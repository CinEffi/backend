package shinzo.cineffi.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.board.Post;
import shinzo.cineffi.domain.entity.board.PostLike;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    List<PostLike> findAllByPostId(Long postId);
}
