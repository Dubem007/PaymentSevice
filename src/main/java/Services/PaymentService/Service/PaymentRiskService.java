package Services.PaymentService.Service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class PaymentRiskService {
    public boolean assessTransactionRisk(String cardNumber, BigDecimal amount) {
        // Risk assessment logic
        return !(amount.compareTo(BigDecimal.valueOf(10000)) > 0 ||
                isBlacklistedCard(cardNumber));
    }

    private boolean isBlacklistedCard(String cardNumber) {
        // Simulated blacklist check
        List<String> blacklist = Arrays.asList("1111", "2222");
        return blacklist.stream().anyMatch(cardNumber::startsWith);
    }
}
