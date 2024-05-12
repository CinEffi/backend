package shinzo.cineffi.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.UserChat;

import java.util.Optional;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {
    Optional<UserChat> findByUserIdAndChatroomId(Long userId, Long chatroomId);
}