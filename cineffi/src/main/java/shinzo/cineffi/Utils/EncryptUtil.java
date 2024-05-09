package shinzo.cineffi.Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Component
public class EncryptUtil { //URL 암호화 복호화

    @Value("${app.encryption.key}")
    private String key;
    private static final String INIT_VECTOR = "encryptionIntVec";


    //값을 암호화하는 메서드
    public String LongEncrypt(Long value) {
        try {
            String plainText = String.valueOf(value);
            System.out.println(plainText);
            IvParameterSpec iv = generateIV();
            SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, iv);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //암호화된 값을 복호화하는 메서드


    //암호화된 값을 복호화하는 메서드
    public Long LongDecrypt(String encrypted) {
        try {
            IvParameterSpec iv = generateIV();
            SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, iv);

            byte[] original = cipher.doFinal(Base64.getDecoder().decode(String.valueOf(encrypted)));
            return Long.parseLong(new String(original));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static IvParameterSpec generateIV() {
        byte[] iv = new byte[16]; // 16바이트 IV
        new SecureRandom().nextBytes(iv); // 랜덤하게 바이트 배열 채우기
        return new IvParameterSpec(iv);
    }
}
