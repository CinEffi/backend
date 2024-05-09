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
    private static final byte[] FIXED_IV = {
            0x00, 0x01, 0x02, 0x03,
            0x04, 0x05, 0x06, 0x07,
            0x08, 0x09, 0x0A, 0x0B,
            0x0C, 0x0D, 0x0E, 0x0F
    };

    //값을 암호화하는 메서드
    public String LongEncrypt(Long value) {
        try {
            String plainText = String.valueOf(value);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(FIXED_IV);
            SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, ivParameterSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getUrlEncoder().encodeToString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //암호화된 값을 복호화하는 메서드


    //암호화된 값을 복호화하는 메서드
    public Long LongDecrypt(String encrypted) {
        try {
            IvParameterSpec ivParameterSpec = new IvParameterSpec(FIXED_IV);
            SecretKeySpec sKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sKeySpec, ivParameterSpec);

            byte[] original = cipher.doFinal(Base64.getUrlDecoder().decode(encrypted));
            return Long.parseLong(new String(original));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
