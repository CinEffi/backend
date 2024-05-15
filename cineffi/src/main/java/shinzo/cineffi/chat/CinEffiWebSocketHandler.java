package shinzo.cineffi.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import shinzo.cineffi.Utils.CinEffiUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class CinEffiWebSocketHandler extends TextWebSocketHandler {
    private final ChatController chatController;
    // 웹소켓 연결 시
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // JWT로 유저정보 어떻게 가져올지 코드 적어야함. @제욱
        Long userId = 1L;
        chatController.chatSessionInit(userId, session);
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

        String type = webSocketMessage.getType();
        switch(type) {
            case "LIST" :
                chatController.chatroomList(session, webSocketMessage.getData());
                break;
            case "CREATE" :
                chatController.chatroomCreate(session, webSocketMessage.getData());//session, textMessage);
                break;
            case "JOIN" :
                chatController.chatroomJoin(session, webSocketMessage.getData().toString());
                break;
            case "LEAVE" :
                chatController.leaveChatroom(session);
            case "SEND" :
                chatController.sendMessageToChatroom(session, webSocketMessage.getData().toString());
                break;
            default :
                System.out.println("[FATAL ERROR] Unknown type from Client [type] : " + type);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {} //TODO:
}
