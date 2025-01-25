package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Enums.CardType;

public record BankDetails(String accountNumber, String routingNumber, String bankName, AccountType accountType, String swiftCode,
                          String iban) {
}
