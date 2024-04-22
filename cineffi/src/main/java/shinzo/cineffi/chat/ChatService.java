package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shinzo.cineffi.domain.dto.ChatroomCreateDTO;
import shinzo.cineffi.domain.dto.ChatroomSearchDTO;
import shinzo.cineffi.domain.entity.chat.Chatroom;
import shinzo.cineffi.domain.entity.chat.UserChat;
import shinzo.cineffi.domain.entity.user.User;
import shinzo.cineffi.domain.enums.UserChatStatus;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.user.repository.UserRepository;
import shinzo.cineffi.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final UserChatRepository userChatRepository;

    public List<ChatroomSearchDTO> searchChatroom(String q, int page, int size) {
        List<ChatroomSearchDTO> searchResult = new ArrayList<>(); // 이거 고쳐야합니다.. 구현이 안된 상태에요
        return searchResult;
    }


    public Long createChatroom(ChatroomCreateDTO chatroomCreateDTO, Long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new CustomException(ErrorMsg.NOT_LOGGED_ID));

        Chatroom createChatroom = Chatroom.builder()
                .title(chatroomCreateDTO.getChatroomName())
                .tags(Utils.convertListToUBString(chatroomCreateDTO.getChatroomTag()))
                .debate(chatroomCreateDTO.getChatroomType())
                .owner(owner)
                .build();

        Chatroom chatroom = chatRepository.save(createChatroom);
        return chatroom.getId();
    }

    public Long deleteChatroom(Long chatroomId, Long ownerId) {
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new CustomException(ErrorMsg.NOT_LOGGED_ID));
        Chatroom chatroom = chatRepository.findById(chatroomId).orElseThrow(
                () -> new CustomException(ErrorMsg.CHATROOM_NOT_FOUND));
        if (!chatroom.getOwner().equals(owner))
            throw new CustomException(ErrorMsg.NOT_CHATROOM_OWNER);
        chatRepository.deleteById(chatroomId);
        return chatroomId;
    }


    public Long joinChatroom(Long chatroomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorMsg.NOT_LOGGED_ID));

        Chatroom chatroom = chatRepository.findById(chatroomId).orElseThrow(() -> new CustomException(ErrorMsg.CHATROOM_NOT_FOUND));
        UserChat createUserChat = UserChat.builder()
                .user(user)
                .chatroom(chatroom)
                .userChatStatus(UserChatStatus.JOINED)
                .build();
        UserChat userChat = userChatRepository.save(createUserChat);
        return userChat.getId();
    }

    public Long exitChatroom(Long chatroomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new CustomException(ErrorMsg.NOT_LOGGED_ID));
        Chatroom chatroom = chatRepository.findById(chatroomId).orElseThrow(() -> new CustomException(ErrorMsg.CHATROOM_NOT_FOUND));

        Optional<UserChat> opUserChat = chatroom.getUserChats().stream().filter(userChatElem -> userChatElem.getUser().equals(user)).findFirst();
        if (!opUserChat.isPresent())
            throw new CustomException(ErrorMsg.USERCHAT_NOT_FOUND);
        Long id = opUserChat.get().getId();
        userChatRepository.deleteById(id);
        return id;
    }
}