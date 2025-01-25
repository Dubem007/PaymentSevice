package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Enums.WalletType;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateWallet {
    private UUID id;
    private String customerName;
    private BigDecimal balance;
    private WalletType walletType;
    private UUID accountId;

    // Constructor
    public CreateWallet(UUID id, String customerName, BigDecimal balance, WalletType walletType, UUID accountId) {
        this.id = id;
        this.customerName = customerName;
        this.balance = balance;
        this.walletType = walletType;
        this.accountId = accountId;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public void setBank(UUID accountId) {
        this.accountId = accountId;
    }
}
