package Services.PaymentService.Dto;

import Services.PaymentService.Enums.PaymentMethod;
import Services.PaymentService.Enums.TransactionStatus;
import Services.PaymentService.Enums.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

public class TransactionDto {
    private UUID transactionId;
    private final TransactionType transactionType;
    private final BigDecimal amount;
    private final String currency;
    private final PaymentMethod paymentMethod;
    private final String accountDetails;
    private TransactionStatus transferStatus;
    private final Timestamp transactionTime;

    @Autowired
    public TransactionDto(UUID transactionId,TransactionType transactionType, BigDecimal amount, String currency,PaymentMethod paymentMethod, String accountDetails,Timestamp transactionTime,TransactionStatus transferStatus) {
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
        this.accountDetails = accountDetails;
        this.transactionTime = transactionTime;
        this.transferStatus = transferStatus;
    }

    public UUID getId() {
        return transactionId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountDetails() {
        return accountDetails;
    }

    public Timestamp getTimestamp() {
        return transactionTime;
    }

    public TransactionStatus getTransactionStatus() {
        return transferStatus;
    }

    public UUID setId(UUID id) {
        return transactionId = id;
    }

    public TransactionStatus setTransactionStatus(TransactionStatus transactionStatus) {
        return transferStatus = transactionStatus;
    }
}

