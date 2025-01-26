package Services.PaymentService.Repository;

import Services.PaymentService.Dto.AccountDto;
import Services.PaymentService.Dto.AccountRequest;
import Services.PaymentService.Dto.CreateWallet;
import Services.PaymentService.Dto.WalletDetails;
import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Enums.WalletType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Repository
public class WalletRepository {
    @Autowired
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(WalletRepository.class);
    public WalletRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public CreateWallet createNewWallet(CreateWallet transaction) {
        String sql = "INSERT INTO payment_db.WALLET " +
                "(Id, customerName,balance,walletType, AccountId) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String new_Id = UUID.randomUUID().toString();
            pstmt.setString(1, new_Id);
            pstmt.setString(2, transaction.getCustomerName());
            pstmt.setString(3, transaction.getBalance().toString());
            pstmt.setString(4, transaction.getWalletType().name());
            pstmt.setString(5, transaction.getAccountId().toString());

            pstmt.executeUpdate();

            transaction.setId(UUID.fromString(new_Id));
            return transaction;
        } catch (SQLException e) {
            throw new PaymentRepository.DatabaseException("Failed to create new Account", e);
        }
    }

    // Method to update an account in the database
    public void update(UUID walletId, BigDecimal newBalance) {
        logger.info("About to updateWalletBalance for cardNumber: {}", walletId);
        logger.info("About to updateWalletBalance with new balance: {}", newBalance);

        String sql = "UPDATE payment_db.WALLET SET balance = ? WHERE Id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, walletId.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update wallet balance with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to update wallet balance", e);
        }
    }

    // Method to find an account by account number
    public WalletDetails getWalletDetails(UUID walletId) {
        logger.info("About to getWalletDetails with new balance: {}", walletId);

        String sql = "SELECT * FROM payment_db.WALLET WHERE Id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, walletId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs); // Map ResultSet to Account object
            }
        } catch (SQLException e) {
            logger.error("Failed to find wallet details with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to find wallet details", e);
        }
        return null; // Return null if account not found
    }

    // Method to find an account by account number
    public WalletDetails getWalletDetailsByAccountId(UUID accountId) {
        logger.info("About to getWalletDetails with accountId: {}", accountId);

        String sql = "SELECT * FROM payment_db.WALLET WHERE AccountId = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs); // Map ResultSet to Account object
            }
        } catch (SQLException e) {
            logger.error("Failed to find wallet details by account Id with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to find wallet details by account Id", e);
        }
        return null; // Return null if account not found
    }

    // Method to map ResultSet to Account object
    private WalletDetails mapResultSetToAccount(ResultSet rs) throws SQLException {
        UUID Id = UUID.fromString(rs.getString("Id"));
        String customerName = rs.getString("customerName");
        BigDecimal balance = rs.getBigDecimal("balance");
        WalletType walletType = WalletType.valueOf(rs.getString("walletType"));
        UUID accountId = UUID.fromString(rs.getString("AccountId"));
        return new WalletDetails(Id,customerName, balance,walletType,accountId); // Assuming Account constructor takes accountNumber and balance
    }
}
