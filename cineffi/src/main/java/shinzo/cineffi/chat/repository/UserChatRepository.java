package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.chat.UserChat;
import shinzo.cineffi.domain.enums.UserChatStatus;

import java.util.List;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    UserChat findByUserIdAndChatroomId(Long userId, Long chatroomId);
    UserChat findByUserNicknameAndChatroomId(String userNickname, Long chatroomId);

    @Modifying
    @Transactional
    @Query("UPDATE UserChat uc SET uc.userChatStatus = :status WHERE uc.chatroom = :chatroomId")
    void updateUserChatStatusByChatroomId(Long chatroomId, UserChatStatus status);


}