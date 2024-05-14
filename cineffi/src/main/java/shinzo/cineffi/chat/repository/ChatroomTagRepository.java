package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.ChatroomTag;

public interface ChatroomTagRepository extends JpaRepository<ChatroomTag, Long> { }