package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.ChatMessage;
import shinzo.cineffi.domain.entity.chat.Chatroom;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {}