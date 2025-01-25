package Services.PaymentService.Dto;

import Services.PaymentService.Enums.PaymentMethod;
import Services.PaymentService.Enums.TransactionType;
import Services.PaymentService.Utils.PaymentValidationUtils;
import Services.PaymentService.Utils.StringUtils;

import java.math.BigDecimal;

public class PaymentRequest {
    private final BigDecimal amount;
    private final String currency;
    private final TransactionType transactionType;
    private final PaymentMethod paymentMethod;
    private final CardRequest cardDetails;
    private final BankPaymentRequest bankDetails;
    private final WalletPaymentRequest walletDetails;

    public PaymentRequest(BigDecimal amount, String currency, TransactionType transactionType, CardRequest cardDetails, BankPaymentRequest bankDetails, WalletPaymentRequest walletDetails,PaymentMethod paymentMethod) {
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.cardDetails = cardDetails;
        this.bankDetails = bankDetails;
        this.walletDetails = walletDetails;
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
    public TransactionType getTransactionType() {
        return transactionType;
    }

    public CardRequest getCardDetails() {
        return cardDetails;
    }

    public BankPaymentRequest getBankDetails() {
        return bankDetails;
    }

    public WalletPaymentRequest getWalletDetails() {
        return walletDetails;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    // Validation methods
    public boolean validatePaymentDetails() {
        return switch (paymentMethod) {
            case CREDIT_CARD -> validateCardDetails();
            case BANK_TRANSFER -> validateBankDetails();
            case DIGITAL_WALLET -> validateWalletDetails();
        };
    }

    private boolean validateCardDetails() {
        return cardDetails != null
                && PaymentValidationUtils.isValidCardNumber(cardDetails.sourceCardNumber())
                && PaymentValidationUtils.isValidCVV(cardDetails.cvv());
    }
    private boolean validateDestinationCardDetails() {
        return cardDetails != null
                && PaymentValidationUtils.isValidCardNumber(cardDetails.destinationCardNumber());
    }

    private boolean validateBankDetails() {
        return bankDetails != null
                && PaymentValidationUtils.isValidAccountNumber(bankDetails.sourceAccountNumber());
    }

    private boolean validateDestinationBankDetails() {
        return bankDetails != null
                && PaymentValidationUtils.isValidAccountNumber(bankDetails.destinationAccountNumber());
    }


    private boolean validateWalletDetails() {
        return walletDetails != null
                && StringUtils.isNotBlankUUID(walletDetails.sourceWalletId());
    }

    private boolean validateDestinationWalletDetails() {
        return walletDetails != null
                && StringUtils.isNotBlankUUID(walletDetails.destinationWalletId());
    }



}
