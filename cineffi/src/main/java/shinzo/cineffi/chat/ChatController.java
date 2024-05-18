package shinzo.cineffi.chat;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.domain.dto.*;
import shinzo.cineffi.domain.entity.chat.Chatroom;

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
//        queryLookers.put(nickname, ChatQuery.builder().queryType(QUERY_TYPE.NONE).build());
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
        ChatroomDTO chatroomDTO = chatService.createChatroom(nickname, createChatroomDTO.getTitle(), createChatroomDTO.getTags());
        updateToChatList("CREATE", chatroomDTO);
        return WebSocketMessage.builder().type("CREATE").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").result(chatroomDTO.getChatroomId()).build()).build();
        // join은 여기서 하지 않고, create 통보를 받았을 시에 프론트에서 바로 join을 요청하게끔 되어있어야한다.
    }

    public WebSocketMessage<InChatroomInfoDTO> chatroomJoin(String nickname, Long chatroomId) throws Exception {
        InChatroomInfoDTO inChatroomInfoDTO = chatService.joinChatroom(nickname, chatroomId);
        sessions.put(nickname, sessions.get(nickname).toBuilder().chatroomId(chatroomId).build());
        updateToChatList("JOIN", ChatroomDTO.builder().chatroomId(chatroomId).build());
        return WebSocketMessage.<InChatroomInfoDTO>builder().type("JOIN").sender("SERVER").data(inChatroomInfoDTO).build();
    }

    public WebSocketMessage<?> chatroomLeave(Long chatroomId, String nickname) throws Exception {
        Long leavedChatroomId = chatService.leaveChatroom(chatroomId, nickname);
        sessions.put(nickname, sessions.get(nickname).toBuilder().chatroomId(0L).build());
        updateToChatList("EXIT", ChatroomDTO.builder().chatroomId(chatroomId).build());
        return WebSocketMessage.builder().type("EXIT").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").result(leavedChatroomId).build()).build();
    }

    public void chatroomClose(Long chatroomId) throws Exception {
        chatService.closeChatroom(chatroomId);
        updateToChatList("CLOSE", ChatroomDTO.builder().chatroomId(chatroomId).build());
    }

    public void messageToChatroom(Long chatroomId, String nickname, String message) throws Exception {
        chatService.sendMessageToChatroom(chatroomId, nickname, message);
    }

    private void updateToChatList(String type, ChatroomDTO chatroomDTO) {
        try {
            if (type.equals("CREATE")) {
                WebSocketMessage<ChatroomDTO> newChatroom = WebSocketMessage.<ChatroomDTO>builder()
                        .sender("SERVER").type("NEWCHAT").data(chatroomDTO).build();
                for (Map.Entry<String, ChatQuery> entry : queryLookers.entrySet()) {
                    String nickname = entry.getKey();// ChatQuery value = entry.getValue(); 지금은 채팅방에 대한 쿼리를 지원하지 않으므로 필요하지 않습니다.
                    ChatSession chatSession = ChatController.sessions.get(nickname);
                    chatSession.getSession().sendMessage(new TextMessage(CinEffiUtils.getString(
                            newChatroom)));
                }
            }
            else {
                String sendType = type.equals("CLOSE") ? "CLOSECHAT" : (type.equals("JOIN") ? "JOINCHAT" : "EXITCHAT");
                WebSocketMessage<Long> closedChatroomID = WebSocketMessage.<Long>builder()
                        .sender("SERVER").type(sendType).data(chatroomDTO.getChatroomId()).build();
                for (Map.Entry<String, ChatQuery> entry : queryLookers.entrySet()) {
                    String nickname = entry.getKey();// ChatQuery value = entry.getValue(); 지금은 채팅방에 대한 쿼리를 지원하지 않으므로 필요하지 않습니다.
                    ChatSession chatSession = ChatController.sessions.get(nickname);
                    chatSession.getSession().sendMessage(new TextMessage(CinEffiUtils.getString(
                            closedChatroomID)));
                }
            }
        }
            catch (Exception e) {
            System.out.println("[Exception Occur on ChatController.updateToChatList]");
        }
    }

    public void tmpForBackupTest() {// [TMP] // 컨트롤러 호출 안할 가능성이 농후하니, 지우거나 이름을 바꾸시길
        chatService.backupToDatabase();// [TMP]
    }// [TMP]
    public void tmpForChatroomClose() {// [TMP] // 컨트롤러 호출 안할 가능성이 농후하니, 지우거나 이름을 바꾸시길
        chatService.closeChatroom(1L);// [TMP]
    }// [TMP]

}