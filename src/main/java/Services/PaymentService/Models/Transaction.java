package Services.PaymentService.Models;

import Services.PaymentService.Enums.PaymentMethod;
import Services.PaymentService.Enums.TransactionStatus;
import Services.PaymentService.Enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@Table(name ="TRANSACTION")
public class Transaction {

    @Id
    private UUID Id;
    private TransactionType transactionType;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod paymentMethod;
    private String accountDetails;
    private TransactionStatus transferStatus;
    private Timestamp transactionTime;

    public UUID getId() {
        return Id;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountDetails() {
        return accountDetails;
    }

    public TransactionStatus getTransferStatus() {
        return transferStatus;
    }

    public Timestamp getTransactionTime() {
        return transactionTime;
    }

    // Setters
    public void setId(UUID id) {
        this.Id = id;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setAccountDetails(String accountDetails) {
        this.accountDetails = accountDetails;
    }

    public void setTransferStatus(TransactionStatus transferStatus) {
        this.transferStatus = transferStatus;
    }

    public void setTransactionTime(Timestamp transactionTime) {
        this.transactionTime = transactionTime;
    }
}
