package Services.PaymentService.Dto;

import Services.PaymentService.Enums.WalletType;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletPaymentRequest(UUID sourceWalletId, UUID destinationWalletId, String narration) {
}
