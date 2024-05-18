package shinzo.cineffi.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.FollowDto;
import shinzo.cineffi.domain.dto.GetFollowRes;
import shinzo.cineffi.domain.entity.user.Follow;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.entity.user.UserActivityNum;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.FollowRepository;
import shinzo.cineffi.user.repository.UserActivityNumRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static shinzo.cineffi.exception.message.ErrorMsg.*;
import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final UserActivityNumRepository uanRepository;
//    private final EncryptUtil encryptUtil;

    @Transactional
    // 유저 팔로우
    public void followUser(Long followingUserId, Long followerUserId) {
        Optional<User> followerUser = userRepository.findById(followerUserId);
        if (followerUser.isEmpty()) throw new CustomException(EMPTY_USER);
        Optional<User> followingUser = userRepository.findById(followingUserId);
        if (followingUser.isEmpty()) throw new CustomException(EMPTY_FOLLOWING_USER);

        // 중복 검사
        followRepository.findByFollowerIdAndFollowingId(followerUserId, followingUserId).ifPresent(
                f -> { throw new CustomException(DUPLICATE_FOLLOW);}
        );
            // 팔로우 정보 저장
        followRepository.save(Follow.builder()
                .follower(followerUser.get())
                .following(followingUser.get())
                .build());

        // UserActivityNum 업데이트
        increaseUserFollowNum(followingUserId, followerUserId);

    }


    // 유저 언팔로우
    @Transactional
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

        // UserActivityNum 업데이트
        decreaseUserFollowNum(followingUserId, followerUserId);

    }

    // targetUser 를 팔로우하고 있는 유저 목록 조회
    @Transactional
    public GetFollowRes getFollowerList(Long targetUserId, Long loginUserId, Pageable pageable) {
        List<FollowDto> FollowerResList = new ArrayList<>();

        Page<Follow> followList = followRepository.findAllByFollowingId(targetUserId, pageable);
        if (!userRepository.existsById(targetUserId)) throw new CustomException(EMPTY_USER); // 존재하지 않으면 에러 메시지 던지기

        for (Follow f : followList) {
            User follower = f.getFollower();
            Boolean isFollowed = Boolean.FALSE;
            Optional<Follow> foundFollow = followRepository.findByFollowerIdAndFollowingId(loginUserId, follower.getId()); // 내가 팔로우하는 사람 목록 조회
            if (!foundFollow.isEmpty())
                isFollowed = Boolean.TRUE;

            FollowerResList.add(FollowDto.builder()
                    .followId(EncryptUtil.LongEncrypt(f.getId()))
                    .userId(EncryptUtil.LongEncrypt(follower.getId()))
                    .nickname(follower.getNickname())
                    .profileImage(decodeImage(follower.getProfileImage()))
                    .level(follower.getLevel())
                    .isCertified(follower.getIsCertified())
                    .isBad(follower.getIsBad())
                    .isFollowed(isFollowed) // 해당 follower를 내가 팔로워하고 있는지?
                    .build());
        }

        return GetFollowRes.builder()
                .followList(FollowerResList)
                .totalPageNum(followList.getTotalPages())
                .build();
    }

    // targetUser 가 팔로우하고 있는 유저 목록 조회
    @Transactional
    public GetFollowRes getFollowingList(Long targetUserId, Long loginUserId, Pageable pageable) {
        List<FollowDto> FollowingResList = new ArrayList<>();

        Page<Follow> followList = followRepository.findAllByFollowerId(targetUserId, pageable);
        if (!userRepository.existsById(targetUserId)) throw new CustomException(EMPTY_USER); // 존재하지 않으면 에러 메시지 던지기

        for (Follow f : followList) {
            User following = f.getFollowing();
            Boolean isFollowed = Boolean.FALSE;
            Optional<Follow> foundFollow = followRepository.findByFollowerIdAndFollowingId(loginUserId, following.getId()); // 내가 팔로우하는 사람 목록 조회
            if (!foundFollow.isEmpty())
                isFollowed = Boolean.TRUE;

            FollowingResList.add(FollowDto.builder()
                    .followId(EncryptUtil.LongEncrypt(f.getId()))
                    .userId(EncryptUtil.LongEncrypt(following.getId()))
                    .nickname(following.getNickname())
                    .profileImage(decodeImage(following.getProfileImage()))
                    .level(following.getLevel())
                    .isCertified(following.getIsCertified())
                    .isBad(following.getIsBad())
                    .isFollowed(isFollowed) // 해당 follower를 내가 팔로워하고 있는지?
                    .build());
        }

        return GetFollowRes.builder()
                .followList(FollowingResList)
                .totalPageNum(followList.getTotalPages())
                .build();
    }


    private void increaseUserFollowNum(Long followingUserId, Long followerUserId) {
        // 팔로우 하는 사람의 팔로잉 숫자를 +1
        uanRepository.findByUserId(followerUserId).ifPresent( uan -> uan.addFollowingsNum() );

        // 팔로우 당하는 사람의 팔로워 숫자를 +1
        uanRepository.findByUserId(followingUserId).ifPresent(
                uan -> {
                    uan.addFollowersNum();
                    if (uan.getFollowersNum() >= 300 && uan.getUser().getIsCertified() == Boolean.FALSE) // 팔로워가 300명 이상이고, 인증마크가 없다면
                        uan.getUser().changeUserCertifiedStatus(Boolean.TRUE); // isCertified를 true로
                } );

    }

    private void decreaseUserFollowNum(Long followingUserId, Long followerUserId) {
        // 팔로우 하는 사람의 팔로잉 숫자를 -1
        uanRepository.findByUserId(followerUserId).ifPresent(uan -> uan.subFollowingsNum());

        // 팔로우 당하는 사람의 팔로워 숫자를 -1
        uanRepository.findByUserId(followingUserId).ifPresent(
                uan -> {
                    uan.subFollowersNum();
                    if (uan.getFollowersNum() < 300 && uan.getUser().getIsCertified() == Boolean.TRUE) // 팔로워가 300명 미만이고, 인증마크가 있다면
                        uan.getUser().changeUserCertifiedStatus(Boolean.FALSE); // isCertified를 false로
                } );

    }

}
