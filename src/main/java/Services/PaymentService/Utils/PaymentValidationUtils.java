package Services.PaymentService.Utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentValidationUtils {
    public static boolean isValidCardNumber(String cardNumber) {
        // Remove non-digit characters
        String cleanedNumber = cardNumber.replaceAll("\\D", "");

        // Check length and contains only digits
        if (cleanedNumber.length() < 12 || cleanedNumber.length() > 19) {
            return false;
        }

        // Luhn Algorithm
        int sum = 0;
        boolean isEvenIndex = false;

        for (int i = cleanedNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cleanedNumber.charAt(i));

            if (isEvenIndex) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isEvenIndex = !isEvenIndex;
        }

        return sum % 10 == 0;
    }

    // CVV Validation
    public static boolean isValidCVV(String cvv) {
        // Check CVV format
        return cvv != null &&
                cvv.matches("^[0-9]{3,4}$");
    }

    // Bank Account Number Validation
    public static boolean isValidAccountNumber(String accountNumber) {
        // Check basic account number criteria
        return accountNumber != null &&
                accountNumber.matches("^\\d{8,17}$") &&
                !accountNumber.matches("^0+$");
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0; // Amount must be positive
    }

    public static boolean isValidCard(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}"); // Simple validation for a 16-digit card number
    }
}
