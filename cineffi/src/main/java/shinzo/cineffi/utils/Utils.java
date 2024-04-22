package shinzo.cineffi.utils;

import java.util.Arrays;
import java.util.List;

public class Utils {
    public static String convertListToUBString(List<String> tags) {
        return String.join("_", tags);
    }
    // 태그 목록을 문자열로 변환하는 메서드
    public static List<String> convertUBStringToList(String tagsString) {
        return Arrays.asList(tagsString.split("_"));
    }
    // 언더바 문자열을 태그 목록으로 분할하는 메서드
}