package shinzo.cineffi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import shinzo.cineffi.Utils.EncryptUtil;
import shinzo.cineffi.chat.CinEffiWebSocketHandler;

import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import shinzo.cineffi.chat.ChatController;

import java.util.Map;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final ChatController chatController;

@Override
public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry
            .addHandler(signalingSocketHandler(chatController), "/api/chat")
            .addInterceptors(new HttpSessionHandshakeInterceptor())
            .setHandshakeHandler(new DefaultHandshakeHandler())
            .setAllowedOrigins("*");

}

    @Bean
    public org.springframework.web.socket.WebSocketHandler signalingSocketHandler(ChatController chatController) {
        return new CinEffiWebSocketHandler(chatController);
    }

}
