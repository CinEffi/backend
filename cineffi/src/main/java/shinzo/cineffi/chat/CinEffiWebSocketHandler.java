package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import shinzo.cineffi.Utils.CinEffiUtils;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.auth.AuthService;
import shinzo.cineffi.domain.dto.CreateChatroomDTO;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class CinEffiWebSocketHandler extends TextWebSocketHandler {
    private final ChatController chatController;
    private final EncryptUtil encryptUtil;
    // 웹소켓 연결 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String userId = uri.getQuery().split("=")[1];
        System.out.println("userID!!!!!"+userId);
        Long loginUserId = encryptUtil.LongDecrypt(userId);
        System.out.println("find error!!!" + loginUserId);
        session.getAttributes().put("userId", loginUserId);

        chatController.chatSessionInit(loginUserId, session);
        // JWT로 유저정보 어떻게 가져올지 코드 적어야함. @제욱
//        Long userId = AuthService.getLoginUserId(SecurityContextHolder.getContext().getAuthentication().getPrincipal());;
    }
    // 웹소켓 연결 종료 시
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("CinEffiWebSocketHandler.afterConnectionClosed [status] : " + status);
        chatController.chatSessionQuit(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        WebSocketMessage webSocketMessage = CinEffiUtils.getObject(textMessage.getPayload());

        System.out.println("webSocketMessage = " + webSocketMessage);
        System.out.println("webSocketMessage.getType() = " + webSocketMessage.getType());
        System.out.println("webSocketMessage.getType() = " + webSocketMessage.getSender());
        System.out.println("webSocketMessage.getType() = " + webSocketMessage.getData());
        System.out.println("session.getUri() = "+ session.getUri());
        System.out.println("session.getId() = " + session.getId());
        System.out.println("session.getPrincipal() = " + session.getPrincipal());
        String type = webSocketMessage.getType();

//        switch(type) {
//            case "LIST" :
//                sendToSession(session, chatController.sendChatroomList(session, (Boolean)webSocketMessage.getData()));
//                break;
//            case "CREATE" :
//                sendToSession(session, chatController.createChatroom(session, (CreateChatroomDTO)webSocketMessage.getData()));//session, textMessage);
//                break;
//            case "SEND" :
////                sendToSession(session, chatController.sendMessageToChatroom(session, webSocketMessage.getData().toString()));
//                break;
//                ////////////////////////////////////////////////////////
//            case "JOIN" :
////                sendToSession(session, chatController.chatroomJoin(session, webSocketMessage.getData().toString()));
//                break;
//            case "EXIT" :
////                sendToSession(session, chatController.leaveChatroom(session));
//                ////////////////////////////////////////////////////////
//            default :
//                System.out.println("[FATAL ERROR] Unknown type from Client [type] : " + type);
//        }
    }

    public static void sendToSession(WebSocketSession session, WebSocketMessage message) throws Exception {
        if (ChatController.isSessionOK(session)) {session.sendMessage(new TextMessage(CinEffiUtils.getString(message)));}
        else {ChatController.sessionDelete(session);
            System.out.println("[FATAL_ERROR] = sendToSession Failed, session " + session.getId() + " deleted.");
            System.out.println("message.type = " + message.getType() + ", message.data = " + message.getData());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {} //TODO:
}
