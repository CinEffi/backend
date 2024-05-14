package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.UserChat;

public interface UserChatRepository extends JpaRepository<UserChat, Long> {}