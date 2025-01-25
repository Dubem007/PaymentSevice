package Services.PaymentService.Service;

import Services.PaymentService.Dto.PaymentRequest;
import Services.PaymentService.Enums.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class SecurityManager {
    private final EncryptionService encryptionService;

    public SecurityManager(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }
    //private final AuthenticationService authService;

    public boolean validatePaymentRequest(PaymentRequest request) {
        // Validate request integrity
        if (request == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Check user authentication
//        if (!authService.isUserAuthenticated(request.getUserId())) {
//            return false;
//        }

        // Validate payment method
        return isValidPaymentMethod(request.getTransactionType());
    }

    public String encryptSensitiveData(String data) {
        return encryptionService.encrypt(data);
    }

    public String decryptSensitiveData(String encryptedData) {
        return encryptionService.decrypt(encryptedData);
    }

    private boolean isValidPaymentMethod(TransactionType method) {
        // Additional method-specific validation
        return method != null;
    }
}
