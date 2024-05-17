package shinzo.cineffi.Utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import shinzo.cineffi.chat.WebSocketMessage;

public class CinEffiUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static Float averageScore(Float sum, Integer count) {
        if(sum == null || count == null) return null;
        return 0 < count ? Math.round((sum / count) * 10.0f) / 10.0f : null;
    }

    public static <T> T getObject(final String message, String key, Class<T> classtype) throws Exception {
        // JSON 문자열을 JsonNode로 읽기
        JsonNode jsonNode = objectMapper.readTree(message);
        // 키(key)로 매핑된 데이터 추출
        JsonNode dataNode = jsonNode.get(key);
        // 추출된 데이터를 지정된 클래스 타입으로 변환하여 반환
        return objectMapper.treeToValue(dataNode, classtype);
    }

    public static <T> String getString(final WebSocketMessage<T> message) throws Exception {
        return objectMapper.writeValueAsString(message);
    }

    public static String[] extractSegments(String input, char delimChar) {
        input = input.replaceAll("^\"|\"$", ""); // 문자열의 시작과 끝에 있는 큰따옴표를 제거
        int firstIndex = input.indexOf(delimChar);
        int lastIndex = input.lastIndexOf(delimChar);
        String[] segments = new String[3];
        segments[0] = firstIndex != -1 ? input.substring(0, firstIndex) : input;
        segments[2] = lastIndex != -1 ? input.substring(lastIndex + 1) : input;
        segments[1] = firstIndex != lastIndex ? input.substring(firstIndex + 1, lastIndex) : "";
        return segments;
    }

}
