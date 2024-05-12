package shinzo.cineffi.chat;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByChatroomIdOrderByTimestampAsc(Long chatroomId);
}
