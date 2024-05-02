package shinzo.cineffi.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.user.Follow;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {


    List<Follow> findAllByFollowerId(Long userId);

    List<Follow> findAllByFollowingId(Long userId);

    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    Page<Follow> findAllByFollowerId(Long userId, Pageable pageable);

    Page<Follow> findAllByFollowingId(Long userId, Pageable pageable);

}
