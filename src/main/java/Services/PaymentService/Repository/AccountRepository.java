package Services.PaymentService.Repository;

import Services.PaymentService.Dto.AccountDto;
import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Models.Account;
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
public class AccountRepository {
    @Autowired
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(CardDetailsRepository.class);

    public AccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Method to begin a transaction
    public void beginTransaction() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false); // Start transaction
        } catch (SQLException e) {
            throw new RuntimeException("Failed to begin transaction", e);
        }
    }

    // Method to commit a transaction
    public void commitTransaction() {
        try (Connection conn = dataSource.getConnection()) {
            conn.commit(); // Commit the transaction
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit transaction", e);
        }
    }

    // Method to roll back a transaction
    public void rollbackTransaction() {
        try (Connection conn = dataSource.getConnection()) {
            conn.rollback(); // Rollback the transaction
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }

    // Method to update an account in the database
    public void update(AccountDto account, BigDecimal newBalance) {
        logger.info("About to updateAccountBalance for accountNumber: {}", account.accountNumber());
        logger.info("About to updateAccountBalance with new balance: {}", newBalance);

        String sql = "UPDATE payment_db.ACCOUNT SET balance = ? WHERE accountNumber = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, account.accountNumber());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update account balance: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to update account balance", e);
        }
    }

    // Method to find an account by account number
    public AccountDto findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM payment_db.ACCOUNT WHERE accountNumber = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs); // Map ResultSet to Account object
            }
        } catch (SQLException e) {
            logger.error("Failed to find account details with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to find account details", e);
        }
        return null; // Return null if account not found
    }

    // Method to map ResultSet to Account object
    private AccountDto mapResultSetToAccount(ResultSet rs) throws SQLException {
        UUID Id = UUID.fromString(rs.getString("Id"));
        String accountNumber = rs.getString("accountNumber");
        BigDecimal balance = rs.getBigDecimal("balance");
        String accountName = rs.getString("accountName");
        String bank = rs.getString("bank");
        AccountType accountType = AccountType.valueOf(rs.getString("accountType"));
        return new AccountDto(Id,accountNumber, balance,accountName,bank,accountType); // Assuming Account constructor takes accountNumber and balance
    }
}
