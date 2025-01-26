package Services.PaymentService.Repository;

import Services.PaymentService.Dto.CardDetailsDto;
import Services.PaymentService.Dto.TransactionDto;
import Services.PaymentService.Enums.CardType;
import Services.PaymentService.Enums.TransactionStatus;
import Services.PaymentService.Enums.TransactionType;
import Services.PaymentService.Models.Transaction;
import Services.PaymentService.Service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class CardDetailsRepository {
    @Autowired
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(CardDetailsRepository.class);

    public CardDetailsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void updateCardBalance(String cardNumber, BigDecimal newBalance) {
        logger.info("About to updateCardBalance for cardNumber: {}", cardNumber);
        logger.info("About to updateCardBalance with new balance: {}", newBalance);
        String sql = "UPDATE payment_db.CARD_DETAILS SET balance = ? WHERE cardNumber = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBigDecimal(1, newBalance);
            // pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setString(2, cardNumber);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update card balance: {}", e.getLocalizedMessage());
            throw new PaymentRepository.DatabaseException("Failed to update card balance", e);
        }
    }
    public CardDetailsDto getCardDetails(String cardNumber) {
        logger.info("About to getCardDetails for cardNumber: {}", cardNumber);

        String sql = "SELECT * FROM payment_db.CARD_DETAILS WHERE cardNumber = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cardNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to find cardDetails: {}", e.getLocalizedMessage());
            throw new PaymentRepository.DatabaseException("Failed to find cardDetails", e);
        }
    }

    public CardDetailsDto getCardDetailsByAccountId(UUID accountId) {
        logger.info("About to getCardDetails for cardNumber by accountId: {}", accountId);

        String sql = "SELECT * FROM payment_db.CARD_DETAILS WHERE AccountId = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, accountId.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTransaction(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to find cardDetails by accountId: {}", e.getLocalizedMessage());
            throw new PaymentRepository.DatabaseException("Failed to find cardDetails by by accountId", e);
        }
    }

    private CardDetailsDto mapResultSetToTransaction(ResultSet rs){

       try{
           // Extracting values from the ResultSet
           UUID id = UUID.fromString(rs.getString("Id"));
           String cardNumber = rs.getString("cardNumber");
           String cardHolderName = rs.getString("cardHolderName");
           BigDecimal balance = rs.getBigDecimal("balance");
           String cvv = rs.getString("cvv");
           String expiryDate = rs.getString("expiryDate");
           CardType cardType = CardType.valueOf(rs.getString("cardType"));
           UUID accountid = UUID.fromString(rs.getString("AccountId"));

           // Creating and returning a TransactionDto object
           return new CardDetailsDto(
                   id,
                   cardNumber,
                   cardHolderName,
                   balance,
                   cvv,
                   expiryDate,
                   cardType,
                   accountid
           );
       } catch (SQLException e) {
        logger.error("Failed to map cardDetails: {}", e.getLocalizedMessage());
        throw new PaymentRepository.DatabaseException("Failed to map cardDetails", e);
      }
    }
}
