package Services.PaymentService.Service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class TransactionLimitService {
    public boolean isTransactionAllowed(UUID walletId, BigDecimal amount) {
        // Check transaction limits
        return isWithinWalletLimit(walletId, amount) &&
                isWithinDailyLimit(amount);
    }

    private boolean isWithinWalletLimit(UUID walletId, BigDecimal amount) {
        // Simulate wallet-specific limit
        return amount.compareTo(BigDecimal.valueOf(1000)) <= 0;
    }

    private boolean isWithinDailyLimit(BigDecimal amount) {
        // Simulate daily transaction limit
        return amount.compareTo(BigDecimal.valueOf(5000)) <= 0;
    }
}
