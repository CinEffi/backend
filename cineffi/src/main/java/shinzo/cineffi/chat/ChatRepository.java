package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.user.User;

import java.util.List;


public interface ChatRepository extends JpaRepository<Chatroom, Long> {

}
