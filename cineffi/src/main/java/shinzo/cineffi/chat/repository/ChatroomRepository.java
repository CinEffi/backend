package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shinzo.cineffi.domain.entity.chat.Chatroom;

import java.util.List;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    List<Chatroom> findAllByIsDeletedTrueOrderByIdDesc();

    void updateIsDeleteById(Long chatroomId, boolean b);
}