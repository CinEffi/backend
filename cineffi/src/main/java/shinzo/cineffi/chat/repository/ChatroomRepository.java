package shinzo.cineffi.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.chat.Chatroom;

import java.util.List;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    List<Chatroom> findAllByIsDeletedTrueOrderByIdDesc();

    @Modifying
    @Transactional
    @Query("UPDATE Chatroom c SET c.isDelete = :isDelete WHERE c.id = :chatroomId")
    void updateIsDeleteById(@Param("chatroomId") Long chatroomId, @Param("isDelete") boolean isDelete);
}