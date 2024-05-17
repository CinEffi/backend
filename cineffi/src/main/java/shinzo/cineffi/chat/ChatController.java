package shinzo.cineffi.chat;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import shinzo.cineffi.domain.dto.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatController {
    private final static Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final static Map<String, String> sessionIds = new ConcurrentHashMap<>();
    private final static Map<String, ChatQuery> queryLookers = new ConcurrentHashMap<>();
    private final ChatService chatService;

    public static Map<String, ChatSession> getSessions() { return sessions;}
    public static Map<String, String> getSessionIds() { return sessionIds;}
    public static Map<String, ChatQuery> getQueryLookers() { return queryLookers;}

    public static String getNicknameFromSession(WebSocketSession session) { return sessionIds.get(session.getId()); }
    public static boolean isSessionOK(WebSocketSession session) { return session != null && session.isOpen(); }
    public static void sessionDelete(WebSocketSession session) throws Exception {
        String nickname  = getNicknameFromSession(session);
        if (nickname != null) {
            queryLookers.remove(nickname);
            sessions.remove(nickname);
        }
        sessionIds.remove(session.getId());
    }

    public void chatSessionInit(Long userId, WebSocketSession session) {
        String nickname = chatService.chatUserInit(userId);
        sessionIds.put(session.getId(), nickname);
        sessions.put(nickname, ChatSession.builder().session(session).userId(userId).chatroomId(0L).build());
        queryLookers.put(nickname, ChatQuery.builder().queryType(QUERY_TYPE.NONE).build());
    }

    public void chatSessionQuit(WebSocketSession session) {

        String nickname = getNicknameFromSession(session);
        if (nickname != null) {
            ChatSession chatSession = sessions.get(nickname);
            if (chatSession != null) {
                Long chatroomId = chatSession.getChatroomId();
                if (chatroomId != 0L) {
                    try { if (chatroomId != 0L) chatroomLeave(chatroomId, nickname);
                        } catch (Exception e) { System.out.println("chatroomLeave in chatSessionQuit got exception"); }
                }
            }//   chatService.chatUserQuit(nickname); 안 씁니다.
            queryLookers.remove(nickname);
            sessions.remove(nickname);
        }
        sessionIds.remove(session.getId());
    }

    public WebSocketMessage<ResponseDTO<ChatroomListDTO>> chatroomListSend(boolean isOpen) throws Exception { //, Object queryData
        ChatroomListDTO chatroomListDTO = isOpen ? chatService.listOpenChatroom() : chatService.listClosedChatroom();
        return WebSocketMessage.<ResponseDTO<ChatroomListDTO>>builder().type("LIST").sender("SERVER").data(ResponseDTO.<ChatroomListDTO>builder()
                .isSuccess(true).message("처리완료").result(chatroomListDTO).build()).build();
    }

    public WebSocketMessage<?> chatroomCreate(String nickname, CreateChatroomDTO createChatroomDTO) throws Exception {
        Long chatroomId = chatService.createChatroom(nickname, createChatroomDTO.getTitle(), createChatroomDTO.getTags());
        return WebSocketMessage.builder().type("CREATE").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").result(chatroomId).build()).build();
        // join은 여기서 하지 않고, create 통보를 받았을 시에 프론트에서 바로 join을 요청하게끔 되어있어야한다.
    }

    public WebSocketMessage<?> chatroomJoin(String nickname, Long chatroomId) throws Exception {
        InChatroomInfoDTO inChatroomInfoDTO = chatService.joinChatroom(nickname, chatroomId);
        sessions.put(nickname, sessions.get(nickname).toBuilder().chatroomId(chatroomId).build());
        return WebSocketMessage.builder().type("JOIN").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").result(inChatroomInfoDTO).build()).build();
    }

    public WebSocketMessage<?> chatroomLeave(Long chatroomId, String nickname) throws Exception {
        Long leavedChatroomId = chatService.leaveChatroom(chatroomId, nickname);
        chatService.sendMessageToChatroom(chatroomId, "SERVER", "[notice] : " + nickname + "님이 퇴장하셨습니다.");
        sessions.put(nickname, sessions.get(nickname).toBuilder().chatroomId(0L).build());
        return WebSocketMessage.builder().type("EXIT").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").result(leavedChatroomId).build()).build();
    }

    public void messageToChatroom(Long chatroomId, String nickname, String message) throws Exception {
        chatService.sendMessageToChatroom(chatroomId, nickname, message);
    }

    public void tmpForBackupTest() {// [TMP] // 컨트롤러 호출 안할 가능성이 농후하니, 지우거나 이름을 바꾸시길
        chatService.backupToDatabase();// [TMP]
    }// [TMP]
    public void tmpForChatroomClose() {// [TMP] // 컨트롤러 호출 안할 가능성이 농후하니, 지우거나 이름을 바꾸시길
        chatService.closeChatroom(1L);// [TMP]
    }// [TMP]

}