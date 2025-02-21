package shinzo.cineffi.user;

import java.util.Base64;

public class ImageConverter {
    // 바이트 배열 -> String 형식 으로 디코딩
    public static String decodeImage(byte[] imageByteArray) {
        if (imageByteArray == null) return null;
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageByteArray);
    }

    //  String -> 바이트 배열 형식 으로 인코딩
    public static byte[] encodeImage(String image) {
        return Base64.getDecoder().decode(image);
    }
}
