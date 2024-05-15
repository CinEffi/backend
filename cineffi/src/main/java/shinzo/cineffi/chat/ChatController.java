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
import shinzo.cineffi.domain.dto.ResponseDTO;
import shinzo.cineffi.domain.entity.chat.ChatroomTag;
import shinzo.cineffi.exception.message.SuccessMsg;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ChatController {
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, String> sessionIds = new ConcurrentHashMap<>();
    private final Map<Long, ChatQuery> queryLookers = new ConcurrentHashMap<>();
    private final ChatService chatService;

    public void chatSessionInit(Long userId, WebSocketSession session) {
        chatService.chatUserInit(userId);
        sessions.put(session.getId(), ChatSession.builder().session(session).userId(userId).chatroomId(0L).build());
        sessionIds.put(userId, session.getId());
        queryLookers.put(userId, ChatQuery.builder().queryType(QUERY_TYPE.NONE).build());
    }

    public void chatSessionQuit(WebSocketSession session) {
        var sessionId = session.getId();
        Long userId = sessions.get(sessionId).getUserId();
        chatService.chatUserQuit(userId);
        sessions.remove(sessionId);
        sessionIds.remove(userId);
        queryLookers.remove(userId);
    }

    public void chatroomList(WebSocketSession session, Object queryData) throws Exception {

        var sessionId = session.getId();
        Long userId = sessions.get(sessionId).getUserId();

        ChatQuery chatQuery = (ChatQuery) queryData;

        //        chatService.

        ChatroomListDTO list;

        ChatroomListDTO closed;

        sendToSession(session, WebSocketMessage.builder().type("LIST").data("").build());
    }

    public boolean isSessionOK(WebSocketSession session) { return session != null && session.isOpen(); }

    public void sessionDelete(WebSocketSession session) throws Exception {
        chatroomLeave(session);
        session_chatroom.remove(session.getId());
        sessions.remove(session.getId());
    }

    public void sendToSession(WebSocketSession session, WebSocketMessage message) throws Exception {
        if (isSessionOK(session)) {session.sendMessage(new TextMessage(CinEffiUtils.getString(message)));}
        else {sessionDelete(session);}
    }

    private void chatroomList(WebSocketSession session) throws Exception {
        Set<Long> chatroomIdSet = chatroom_sessions.keySet();
        sendToSession(session, WebSocketMessage.builder()
                .type("CHATROOM_LIST")
                .sender("SERVER")
                .data(chatroomIdSet.isEmpty() ?
                        "no chatroom" :
                        chatroomIdSet.toString()
                ).build()
        );
    }

    private void chatroomCreate(WebSocketSession session // 사실 오히려 이쪽이 더 필요가 없긴 한데
//        , TextMessage textMessage //뭐 방 제목을 전달해줘야한다거나 할때 쓸수 있어.
    )
            throws Exception
    {
        System.out.println("WebSocketHandler.chatroomCreate");
        ////////////////////////////////////////////////////////////////////////

        Long chatroomId = chatroom_sessions.size() + 1L;
        Set<String> sessionsInChatroom = new HashSet<>();
        chatroom_sessions.put(chatroomId, sessionsInChatroom);
        chatroomList(session);
        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.chatroomCreate : done");
    }

    private void chatroomJoin(WebSocketSession session, String messageData) throws Exception {
        System.out.println("WebSocketHandler.chatroomJoin");
        System.out.println("session = " + session);
        System.out.println("messageData = " + messageData);
        ////////////////////////////////////////////////////////////////////////
        String sessionId = session.getId();
        String responseResult = "JOIN_FAILURE";
        try {
            Long chatroomId = Long.parseLong(messageData);// 실패시 exception인데, 어떻게 되나 한번 보자. 웹소켓에 잘 전달이 되는지

            if (session_chatroom.get(sessionId) == 0L && chatroom_sessions.containsKey(chatroomId)) {
                responseResult = "JOIN_SUCCESS";
                chatroomLeave(session);
                session_chatroom.put(sessionId, chatroomId);
                chatroom_sessions.get(chatroomId).add(session.getId());
            }
        }
        catch (NumberFormatException e) {
            responseResult = "JOIN_FAILURE";
            System.out.println(e);
            System.out.println("[Invalid chatroomId] : " + messageData);
        }
        try {
            sendToSession(session, WebSocketMessage.builder().sender("SERVER").type("JOIN_STATUS").data(responseResult).build());
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
        Long exChatroomId = session_chatroom.get(sessionId);
        if (exChatroomId != 0L) {
            sendMessageToChatroom(session, "내 간데이");
            chatroom_sessions.get(exChatroomId).remove(sessionId);
        }
        session_chatroom.put(session.getId(), 0L);
        System.out.println("WebSocketHandler.chatroomLeave - done");
    }

    private void sendMessageToChatroom(WebSocketSession session, String messageData) throws Exception  {

        System.out.println("WebSocketHandler.sendMessageToChatroom");
        System.out.println("session = " + session);
        System.out.println("messageData = " + messageData);
        ////////////////////////////////////////////////////////////////////////
        String senderId = session.getId();

        Long chatroomId = session_chatroom.get(senderId);
        if (chatroomId == 0L)
        {
            System.out.println("chatroomId(0), session : " + session);
            System.out.println("messageData = " + messageData);
            System.out.println("WebSocketHandler.sendMessageToChatroom : done");
            return;
        }


        for (String sessionId : chatroom_sessions.get(chatroomId)) {
            sendToSession(sessions.get(sessionId), WebSocketMessage.builder().sender(senderId).type("CHAT_MESSAGE").data(messageData).build());
        }

        ////////////////////////////////////////////////////////////////////////
        System.out.println("WebSocketHandler.sendMessageToChatroom : done");
    }

}