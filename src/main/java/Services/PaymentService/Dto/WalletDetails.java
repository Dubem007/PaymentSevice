package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Enums.WalletType;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletDetails(UUID Id, String customerName, BigDecimal balance, WalletType walletType,UUID AccountId) {
}