package shinzo.cineffi.chat.repository;

import com.google.common.base.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.entity.chat.Chatroom;

import java.util.List;

public interface ChatroomRepository extends JpaRepository<Chatroom, Long> {
    List<Chatroom> findAllByIsDeleteTrueOrderByIdDesc();

    Optional<Chatroom> findByIdAndIsDeleteTrue(Long chatroomId);

    @Transactional
    @Modifying
    @Query("UPDATE Chatroom c SET c.isDelete = :isDelete WHERE c.id = :chatroomId")
    void updateIsDeleteById(@Param("chatroomId") Long chatroomId, @Param("isDelete") boolean isDelete);
}