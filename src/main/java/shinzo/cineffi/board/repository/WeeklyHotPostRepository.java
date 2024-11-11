package shinzo.cineffi.board.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.board.WeeklyHotPost;
import shinzo.cineffi.domain.entity.board.WeeklyHotPost.HotPostStatusType;

import java.util.List;

public interface WeeklyHotPostRepository extends JpaRepository<WeeklyHotPost, Long> {
    List<WeeklyHotPost> findAllByHotPostStatus(HotPostStatusType status);
}
