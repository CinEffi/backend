package shinzo.cineffi.Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import shinzo.cineffi.chat.WebSocketMessage;

public class CinEffiUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Float averageScore(Float sum, Integer count) {
        return 0 < count ? Math.round((sum / count) * 10.0f) / 10.0f : null;
    }


    public static WebSocketMessage getObject(final String message) throws Exception {
        return objectMapper.readValue(message, WebSocketMessage.class);
    }

    public static String getString(final WebSocketMessage message) throws Exception {
        return objectMapper.writeValueAsString(message);
    }



}
