package shinzo.cineffi.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.UserChat;

public interface UserChatRepository extends JpaRepository<UserChat, Long> { }