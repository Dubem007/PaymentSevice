package Services.PaymentService.Service;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.util.Base64;

@Service
public class EncryptionService {
    private SecretKey secretKey;
    private static final String ALGORITHM = "AES";

    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new SecurityException("Encryption failed", e);
        }
    }

    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(
                    Base64.getDecoder().decode(encryptedData)
            );
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new SecurityException("Decryption failed", e);
        }
    }
}
