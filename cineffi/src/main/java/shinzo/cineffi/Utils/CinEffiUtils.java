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

    public static String[] extractSegments(String input, char delimChar) {
        int firstIndex = input.indexOf(delimChar);
        int lastIndex = input.lastIndexOf(delimChar);
        String[] segments = new String[3];
        segments[0] = firstIndex != -1 ? input.substring(0, firstIndex) : input;
        segments[2] = lastIndex != -1 ? input.substring(lastIndex + 1) : input;
        segments[1] = firstIndex != lastIndex ? input.substring(firstIndex + 1, lastIndex) : "";
        return segments;
    }

}
