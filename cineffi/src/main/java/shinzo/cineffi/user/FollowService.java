package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.user.Follow;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.user.repository.FollowRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static shinzo.cineffi.exception.message.ErrorMsg.*;
import static shinzo.cineffi.user.ImageConverter.decodeImage;
import static shinzo.cineffi.user.ImageConverter.encodeImage;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;


    // 유저 팔로우
    public void followUser(Long followingUserId, Long followerUserId) {
        Optional<User> followerUser = userRepository.findById(followerUserId);
        if (followerUser.isEmpty()) throw new CustomException(EMPTY_USER);
        Optional<User> followingUser = userRepository.findById(followingUserId);
        if (followingUser.isEmpty()) throw new CustomException(EMPTY_FOLLOWING_USER);

        followRepository.findByFollowerIdAndFollowingId(followerUserId, followingUserId).ifPresent(
                f -> {
                    throw new CustomException(DUPLICATE_FOLLOW);
                }
        );

        followRepository.save(Follow.builder()
                .follower(followerUser.get())
                .following(followingUser.get())
                .build());
    }

    // 유저 언팔로우
    public void unfollowUser(Long followingUserId, Long followerUserId) {
        Optional<User> followerUser = userRepository.findById(followerUserId);
        if (followerUser.isEmpty()) throw new CustomException(EMPTY_USER);
        Optional<User> followingUser = userRepository.findById(followingUserId);
        if (followingUser.isEmpty()) throw new CustomException(EMPTY_FOLLOWING_USER);

        followRepository.findByFollowerIdAndFollowingId(followerUserId, followingUserId).ifPresentOrElse(
                f -> {
                    followRepository.delete(f);
                },
                () -> {
                    throw new CustomException(EMPTY_FOLLOW);
                }
        );

    }

    // targetUser 를 팔로우하고 있는 유저 목록 조회
    @Transactional
    public List<GetFollowRes> getFollowerList(Long targetUserId, Long loginUserId) {
        List<GetFollowRes> GetFollowerResList = new ArrayList<>();

        List<Follow> followList = followRepository.findAllByFollowingId(targetUserId);
        for (Follow f : followList) {
            User follower = f.getFollower();
            Boolean isFollowed = Boolean.FALSE;
            if (loginUserId != null) {
                List<Follow> followingList = followRepository.findAllByFollowerId(loginUserId); // 내가 팔로우하는 사람 목록 조회

                for (Follow follow : followingList) {
                    if (follow.getFollowing().getId().equals(follower.getId())) { // 내가 팔로우하는 사람 목록에 있으면
                        isFollowed = true;
                    }
                }
            }
            GetFollowerResList.add(GetFollowRes.builder()
                    .followId(f.getId())
                    .userId(follower.getId())
                    .nickname(follower.getNickname())
                    .profileImage(decodeImage(follower.getProfileImage()))
                    .level(follower.getLevel())
                    .isCertified(follower.getIsCertified())
                    .isBad(follower.getIsBad())
                    .isFollowed(isFollowed) // 해당 follower를 내가 팔로워하고 있는지?
                    .build());
        }

        return GetFollowerResList;
    }

    // targetUser 가 팔로우하고 있는 유저 목록 조회
    @Transactional
    public List<GetFollowRes> getFollowingList(Long targetUserId, Long loginUserId) {


        List<GetFollowRes> GetFollowingResList = new ArrayList<>();

        List<Follow> followList = followRepository.findAllByFollowerId(targetUserId);
        for (Follow f : followList) {
            User following = f.getFollowing();
            Boolean isFollowed = Boolean.FALSE;
            List<Follow> followingList = followRepository.findAllByFollowerId(loginUserId); // 내가 팔로우하는 사람 목록 조회

            for (Follow follow : followingList) {
                if (follow.getFollowing().getId().equals(following.getId())) { // 내가 팔로우하는 사람 목록에 있으면
                    isFollowed = true;
                }
            }
            GetFollowingResList.add(GetFollowRes.builder()
                    .followId(f.getId())
                    .userId(following.getId())
                    .nickname(following.getNickname())
                    .profileImage(decodeImage(following.getProfileImage()))
                    .level(following.getLevel())
                    .isCertified(following.getIsCertified())
                    .isBad(following.getIsBad())
                    .isFollowed(isFollowed) // 해당 follower를 내가 팔로워하고 있는지?
                    .build());
        }

        return GetFollowingResList;
    }



}
