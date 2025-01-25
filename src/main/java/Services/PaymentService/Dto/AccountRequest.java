package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountRequest {
    private UUID id;
    private String accountNumber;
    private BigDecimal balance;
    private String accountName;
    private String bank;
    private AccountType accountType;
    private UUID userId;

    // Constructor
    public AccountRequest(UUID id, String accountNumber, BigDecimal balance, String accountName, String bank, AccountType accountType,UUID userId) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountName = accountName;
        this.bank = bank;
        this.accountType = accountType;
        this.userId = userId;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }
}
