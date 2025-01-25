package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;

public record BankPaymentRequest(String sourceAccountNumber, String destinationAccountNumber, String sourceBankName,String destinationBankName, AccountType accountType, String narration) {
}
