package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.UserChat;

import java.util.List;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    UserChat findByUserIdAndChatroomId(Long userId, Long chatroomId);
}