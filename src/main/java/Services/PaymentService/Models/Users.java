package Services.PaymentService.Models;

import Services.PaymentService.Enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;


@Entity
@Data
@Table(name = "USER")
public class Users {
    @Id
    private UUID Id;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String passwordHash;
    private String location;
    private Timestamp created_at;
    private Timestamp updated_at;


    // Getters and setters
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getLocation() { return location; }
    public Timestamp getCreatedTime() { return created_at; }
    public UUID getId() { return Id; }
}

