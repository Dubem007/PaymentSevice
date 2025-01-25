package Services.PaymentService.Utils;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;

@Component
public class StringUtils {
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
    public static boolean isNotBlankUUID(UUID str) {
        return str != null && !str.toString().trim().isEmpty();
    }

    // Additional utility methods
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String defaultIfBlank(String str, String defaultStr) {
        return isBlank(str) ? defaultStr : str;
    }

    public static String generateAccountNumber() {
        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            accountNumber.append(random.nextInt(10));
        }
        return accountNumber.toString();
    }

}
