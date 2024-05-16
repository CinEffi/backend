package shinzo.cineffi.user;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.domain.dto.GetMyPageRes;
import shinzo.cineffi.domain.dto.GetProfileRes;
import shinzo.cineffi.domain.entity.user.Follow;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.entity.user.UserAccount;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.user.repository.FollowRepository;
import shinzo.cineffi.user.repository.UserAccountRepository;
import shinzo.cineffi.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static shinzo.cineffi.exception.message.ErrorMsg.*;
import static shinzo.cineffi.user.ImageConverter.decodeImage;

@Controller
@RequiredArgsConstructor
public class UserService {
    @Autowired
    private UserAccountRepository userAccountRepository;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final EncryptUtil encryptUtil;

    public GetMyPageRes getMyPage(Long userId, Long loginUserId) {

        Optional<User> foundUser = userRepository.findById(userId);
        if (foundUser.isEmpty()) throw new CustomException(EMPTY_USER);
        User targetUser = foundUser.get();

        Boolean isFollowed = Boolean.FALSE;
        List<Follow> followingList = followRepository.findAllByFollowerId(loginUserId);

        // 특정 유저가 팔로우하고 있는지 확인
        for (Follow follow : followingList) {
            if (follow.getFollowing().getId().equals(userId)) {
                isFollowed = true;
            }
        }

        GetMyPageRes getMyPageRes = GetMyPageRes.builder()
                .userId(encryptUtil.LongEncrypt(targetUser.getId()))
                .nickname(targetUser.getNickname())
                .userProfileImage(decodeImage(targetUser.getProfileImage()))
                .exp(targetUser.getExp())
                .expMax((int) floor(5 * pow(1.1, (targetUser.getLevel() - 1))))
                .isBad(targetUser.getIsBad())
                .isCertified(targetUser.getIsCertified())
                .level(targetUser.getLevel())
                .collectionNum(targetUser.getUserActivityNum().getCollectionNum())
                .scrapNum(targetUser.getUserActivityNum().getScrapNum())
                .followerNum(targetUser.getUserActivityNum().getFollowersNum())
                .followingNum(targetUser.getUserActivityNum().getFollowingsNum())
                .isFollowed(isFollowed)
                .genreLabel(targetUser.getUserAnalysis().getGenreTendency())
                .scoreLabel(targetUser.getUserAnalysis().getScoreTendency())
                .build();
        return getMyPageRes;
    }

    public GetProfileRes getMyProfileInfo(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) throw new CustomException(EMPTY_USER);
        return new GetProfileRes(user.get().getNickname(), user.get().getUserAccount().getEmail(), decodeImage(user.get().getProfileImage()));

    }

    public void editUserProfile(Long userId, String nickname, String password, MultipartFile uploadedFile) throws IOException {
        // 중복 닉네임 검사
        if (userRepository.existsByNickname(nickname)) throw new CustomException(DUPLICATE_NICKNAME);
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) throw new CustomException(EMPTY_USER);

        User user = userOptional.get();
        UserAccount userAccount = user.getUserAccount();

        try {
            userRepository.save(user.toBuilder()
                    .nickname( nickname == null || nickname.isEmpty() ? user.getNickname() : nickname)
                    .profileImage( uploadedFile == null || uploadedFile.isEmpty() ? user.getProfileImage() : uploadedFile.getBytes())
                    .userAccount( password == null || password.isEmpty() ? user.getUserAccount() : userAccount.toBuilder().password(BCrypt.hashpw(password, BCrypt.gensalt())).build()) // 비밀번호 암호화하여 저장
                    .build());
        } catch (IOException e) {
            throw new CustomException(FAIDED_TO_CONVERT_IMAGE);
        }

    }
}