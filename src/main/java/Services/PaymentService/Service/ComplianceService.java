package Services.PaymentService.Service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ComplianceService {
    public boolean validateTransfer(String accountNumber, BigDecimal amount) {
        // Compliance and transfer limit checks
        return isWithinDailyLimit(accountNumber, amount) &&
                meetsAMLRequirements(amount);
    }

    private boolean isWithinDailyLimit(String accountNumber, BigDecimal amount) {
        // Simulate daily transfer limit check
        return amount.compareTo(BigDecimal.valueOf(5000)) <= 0;
    }

    private boolean meetsAMLRequirements(BigDecimal amount) {
        // Anti-Money Laundering check
        return amount.compareTo(BigDecimal.valueOf(10000)) < 0;
    }
}
