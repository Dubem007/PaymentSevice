package Services.PaymentService.Repository;

import Services.PaymentService.Dto.AccountDto;
import Services.PaymentService.Dto.AccountRequest;
import Services.PaymentService.Dto.UserDto;
import Services.PaymentService.Dto.WalletDetails;
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
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public AccountRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public AccountRequest createNewAccount(AccountRequest transaction) {
        String sql = "INSERT INTO payment_db.ACCOUNT " +
                "(Id, accountNumber, accountName,balance,bank, accountType,userId) " +
                "VALUES (?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String new_Id = UUID.randomUUID().toString();
            pstmt.setString(1, new_Id);
            pstmt.setString(2, transaction.getAccountNumber());
            pstmt.setString(3, transaction.getAccountName());
            pstmt.setString(4, transaction.getBalance().toString());
            pstmt.setString(5, transaction.getBank());
            pstmt.setString(6, transaction.getAccountType().name());
            pstmt.setString(7, transaction.getUserId().toString());

            pstmt.executeUpdate();

            transaction.setId(UUID.fromString(new_Id));
            return transaction;
        } catch (SQLException e) {
            logger.error("Failed to create new Account with error: {}", e.getLocalizedMessage());
            throw new PaymentRepository.DatabaseException("Failed to create new Account", e);
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
    public AccountDto getAccountDetailsByUserId(UUID userId) {
        logger.info("About to getAccountDetailsByUserId with accountId: {}", userId);

        String sql = "SELECT * FROM payment_db.ACCOUNT WHERE userId = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs); // Map ResultSet to Account object
            }
        } catch (SQLException e) {
            logger.error("Failed to find account details by user Id with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to find account details by user Id", e);
        }
        return null; // Return null if account not found
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
