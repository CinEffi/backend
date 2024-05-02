package shinzo.cineffi.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import shinzo.cineffi.domain.entity.user.Follow;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {


    List<Follow> findAllByFollowerId(Long userId);

    List<Follow> findAllByFollowingId(Long userId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);
}
