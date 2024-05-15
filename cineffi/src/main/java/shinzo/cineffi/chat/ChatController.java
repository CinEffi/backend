package shinzo.cineffi.chat;


import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.domain.dto.ChatroomListDTO;
import shinzo.cineffi.domain.dto.CreateChatroomDTO;
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.entity.chat.ChatroomTag;
import shinzo.cineffi.exception.CustomException;
import shinzo.cineffi.exception.message.ErrorMsg;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatController {
    private final static Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final static Map<String, String> sessionIds = new ConcurrentHashMap<>();
    private final static Map<String, ChatQuery> queryLookers = new ConcurrentHashMap<>();
    private final ChatService chatService;

    public static String getNicknameFromSession(WebSocketSession session) { return sessionIds.get(session.getId()); }
    public static boolean isSessionOK(WebSocketSession session) { return session != null && session.isOpen(); }
    public static void sessionDelete(WebSocketSession session) throws Exception {
        String nickname  = getNicknameFromSession(session);
        queryLookers.remove(nickname);
        sessions.remove(nickname);
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
        chatService.chatUserQuit(nickname);
        queryLookers.remove(nickname);
        sessions.remove(nickname);
        sessionIds.remove(session.getId());
    }

    public WebSocketMessage sendChatroomList(WebSocketSession session, boolean isOpen) throws Exception { //, Object queryData
        ChatroomListDTO chatroomListDTO = isOpen ? chatService.listOpenChatroom() : chatService.listClosedChatroom();
        return WebSocketMessage.builder().type("LIST").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").result(chatroomListDTO).build()).build();
    }

    public WebSocketMessage createChatroom(WebSocketSession session, CreateChatroomDTO createChatroomDTO) throws Exception {
        String nickname = getNicknameFromSession(session);
        chatService.createChatroom(nickname, createChatroomDTO.getTitle(), createChatroomDTO.getTags());

        // chatService.joinChatroom(nickname, );
//        chatService.sendMessageToChatroom();

        return WebSocketMessage.builder().type("CREATE").sender("SERVER").data(ResponseDTO.builder()
                .isSuccess(true).message("처리완료").build()).build();
        // join은 여기서 하지 않고, create 통보를 받았을 시에 프론트에서 바로 join을 요청하게끔 되어있어야한다.
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void sendMessageToChatroom(WebSocketSession session, String messageData) throws Exception  {
        String senderId = session.getId();
        String nickname = getNicknameFromSession(session);

        ChatSession chatSession = sessions.get(nickname);
        Long chatroomId = chatSession.getChatroomId();
        if (chatroomId == 0L) throw new CustomException(ErrorMsg.CHATROOM_NON_FOUND);

//        for (String sessionId : chatroom_sessions.get(chatroomId)) {
//            sendToSession(sessions.get(sessionId), WebSocketMessage.builder().sender(senderId).type("CHAT_MESSAGE").data(messageData).build());
//        }

        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.sendMessageToChatroom : done");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////


    private void chatroomJoin(WebSocketSession session, String messageData) throws Exception {
        System.out.println("WebSocketHandler.chatroomJoin");
        System.out.println("session = " + session);
        System.out.println("messageData = " + messageData);
        ////////////////////////////////////////////////////////////////////////
        String sessionId = session.getId();
        String responseResult = "JOIN_FAILURE";
        try {
            Long chatroomId = Long.parseLong(messageData);// 실패시 exception인데, 어떻게 되나 한번 보자. 웹소켓에 잘 전달이 되는지
//            if (session_chatroom.get(sessionId) == 0L && chatroom_sessions.containsKey(chatroomId)) {
//                responseResult = "JOIN_SUCCESS";
//                chatroomLeave(session);
//                session_chatroom.put(sessionId, chatroomId);
//                chatroom_sessions.get(chatroomId).add(session.getId());
//            }
        }
        catch (NumberFormatException e) {
            responseResult = "JOIN_FAILURE";
            System.out.println(e);
            System.out.println("[Invalid chatroomId] : " + messageData);
        }
        try {
//            sendToSession(session, WebSocketMessage.builder().sender("SERVER").type("JOIN_STATUS").data(responseResult).build());
            if (responseResult.equals("JOIN_SUCCESS")) { sendMessageToChatroom(session, "내 왔다."); }
        }
        catch (Exception e) {
            System.out.println(e);
            System.out.println("Utils.getString Exception occur");
        }
        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.chatroomJoin : done");
    }

    private void chatroomLeave(WebSocketSession session) throws Exception {
        System.out.println("WebSocketHandler.chatroomLeave");
        System.out.println("session = " + session);

        var sessionId = session.getId();
//        Long exChatroomId = session_chatroom.get(sessionId);
//        if (exChatroomId != 0L) {
//            sendMessageToChatroom(session, "내 간데이");
////            chatroom_sessions.get(exChatroomId).remove(sessionId);
//        }
//        session_chatroom.put(session.getId(), 0L);
        System.out.println("WebSocketHandler.chatroomLeave - done");
    }


}