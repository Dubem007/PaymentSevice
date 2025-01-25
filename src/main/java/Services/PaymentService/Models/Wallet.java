package Services.PaymentService.Models;

import Services.PaymentService.Enums.WalletType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Table(name = "WALLET")
public class Wallet {
    @Id
    private UUID Id;
    private String customerName;
    private BigDecimal balance;
    private WalletType walletType;


    public UUID getWalletId() {
        return Id;
    }
    public void setWalletId(UUID Id) {
        this.Id = Id;
    }

    public String getCustomerName() {
        return customerName;
    }
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public WalletType getWalletType() {
        return walletType;
    }
    public void setWalletType(WalletType walletType) {
        this.walletType = walletType;
    }
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}

