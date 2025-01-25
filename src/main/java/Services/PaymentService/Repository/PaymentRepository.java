package Services.PaymentService.Repository;

import Services.PaymentService.Dto.TransactionDto;
import Services.PaymentService.Enums.PaymentMethod;
import Services.PaymentService.Enums.TransactionStatus;
import Services.PaymentService.Enums.TransactionType;
import Services.PaymentService.Models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Repository
public class PaymentRepository {

    @Autowired
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(CardDetailsRepository.class);


    public PaymentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public TransactionDto save(TransactionDto transaction) {
        String sql = "INSERT INTO payment_db.TRANSACTION " +
                "(Id, transactionType, amount,currency,paymentMethod, accountDetails,transferStatus, transactionTime) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String new_Id = UUID.randomUUID().toString();
            pstmt.setString(1, new_Id);
            pstmt.setString(2, transaction.getTransactionType().name());
            pstmt.setBigDecimal(3, transaction.getAmount());
            pstmt.setString(4, transaction.getCurrency());
            pstmt.setString(5, transaction.getPaymentMethod().name());
            pstmt.setString(6, transaction.getAccountDetails());
            pstmt.setString(7, transaction.getTransactionStatus().name());
            pstmt.setTimestamp(8, transaction.getTimestamp());

            pstmt.executeUpdate();

            transaction.setId(UUID.fromString(new_Id));
            return transaction;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to save transaction", e);
        }
    }

    public TransactionDto findById(String transactionId) {
        String sql = "SELECT * FROM payment_db.TRANSACTION WHERE Id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transactionId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find transaction", e);
        }
    }

    public List<TransactionDto> findByStatus(TransactionStatus status) {
        String sql = "SELECT * FROM payment_db.TRANSACTION WHERE transferStatus = ?";
        List<TransactionDto> transactions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find transactions", e);
        }
    }

    public void updateTransactionStatus(UUID transactionId, TransactionStatus newStatus) {
        logger.info("About to update transaction status for Id: {}", transactionId);
        logger.info("About to update transaction status with new status: {}", newStatus);

        String sql = "UPDATE payment_db.TRANSACTION SET transferStatus = ? WHERE Id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus.name());
            // pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, transactionId.toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.info("Failed to update transaction status: {}", e.getLocalizedMessage());
            throw new DatabaseException("Failed to update transaction status", e);
        }
    }

    public List<TransactionDto> findTransactionsInDateRange(LocalDateTime start, LocalDateTime end) {
        String sql = "SELECT * FROM payment_db.TRANSACTION " +
                "WHERE transactionTime BETWEEN ? AND ?";
        List<TransactionDto> transactions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(start));
            pstmt.setTimestamp(2, Timestamp.valueOf(end));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapResultSetToTransaction(rs));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find transactions in date range", e);
        }
    }

    private TransactionDto mapResultSetToTransaction(ResultSet rs) throws SQLException {
        // Extracting values from the ResultSet
        String Id = rs.getString("Id");
        TransactionType transactionType = TransactionType.valueOf(rs.getString("transactionType"));
        BigDecimal amount = rs.getBigDecimal("amount");
        String currency = rs.getString("currency");
        Timestamp transactionTime = rs.getTimestamp("transactionTime");
        TransactionStatus transferStatus = TransactionStatus.valueOf(rs.getString("transferStatus"));
        String accountDetails = rs.getString("accountDetails");
        PaymentMethod paymentMethod = PaymentMethod.valueOf(rs.getString("paymentMethod"));

        UUID id = UUID.fromString(Id);
        // Creating and returning a TransactionDto object
        return new TransactionDto(
                id,
                transactionType,
                amount,
                currency,
                paymentMethod,
                accountDetails,
                transactionTime,
                transferStatus
        );
    }


    // Custom exception for database-related errors
    public static class DatabaseException extends RuntimeException {
        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
