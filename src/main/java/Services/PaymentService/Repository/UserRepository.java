package Services.PaymentService.Repository;

import Services.PaymentService.Dto.UserDto;
import Services.PaymentService.Dto.UserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class UserRepository {
    @Autowired
    private final DataSource dataSource;
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public UserDto createUser(UserDto transaction) {
        String sql = "INSERT INTO payment_db.USER " +
                "(Id, firstName, lastName,emailAddress, phoneNumber,passwordHash, status,location, created_at,updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String new_Id = UUID.randomUUID().toString();
            pstmt.setString(1, new_Id);
            pstmt.setString(2, transaction.getFirstName());
            pstmt.setString(3, transaction.getLastName());
            pstmt.setString(4, transaction.getEmailAddress());
            pstmt.setString(5, transaction.getPhoneNumber());
            pstmt.setString(6, transaction.getPasswordHash());
            pstmt.setString(7, transaction.getStatus());
            pstmt.setString(8, transaction.getLocation());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now())); // created_at
            pstmt.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now())); // updated_at

            pstmt.executeUpdate();

            transaction.setId(UUID.fromString(new_Id));
            return transaction;
        } catch (SQLException e) {
            logger.error("Failed to create new User with error: {}", e.getLocalizedMessage());
            throw new PaymentRepository.DatabaseException("Failed to create new User with error", e);
        }
    }

    // Method to update an account in the database
    public void updateUser(UserDto account, String newStatus) {
        logger.info("About to updateUser for user Id: {}", account.getId());
        logger.info("About to updateUser with new status: {}", newStatus);

        String sql = "UPDATE payment_db.ACCOUNT SET status = ? WHERE Id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setString(2, account.getId().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to update account status: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to update account status", e);
        }
    }

    // Method to find an account by account number
    public UserDetails findById(UUID userId) {
        String sql = "SELECT * FROM payment_db.USER WHERE Id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, userId.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs); // Map ResultSet to Account object
            }
        } catch (SQLException e) {
            logger.error("Failed to find account details by Id with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to find account details by Id", e);
        }
        return null; // Return null if account not found
    }

    // Method to find an account by account number
    public UserDetails findByEmail(String user_email) {
        String sql = "SELECT * FROM payment_db.USER WHERE emailAddress = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user_email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs); // Map ResultSet to Account object
            }
        } catch (SQLException e) {
            logger.error("Failed to find account details by email with error: {}", e.getLocalizedMessage());
            throw new RuntimeException("Failed to find account details by email", e);
        }
        return null; // Return null if account not found
    }

    // Method to map ResultSet to Account object
    private UserDetails mapResultSetToAccount(ResultSet rs) throws SQLException {
        UUID Id = UUID.fromString(rs.getString("Id"));
        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String emailAddress = rs.getString("emailAddress");
        String phoneNumber = rs.getString("phoneNumber");
        String status = rs.getString("status");
        String password = rs.getString("passwordHash");
        String location = rs.getString("location");
        return new UserDetails(Id,firstName,lastName,emailAddress,phoneNumber,password, status,location,null, null); // Assuming Account constructor takes accountNumber and balance
    }
}
