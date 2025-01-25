package Services.PaymentService.Dto;

import java.sql.Timestamp;
import java.util.UUID;

public record UserDetails(UUID Id, String firstName, String lastName, String emailAddress, String phoneNumber,
                          String password, String status, String location, Timestamp created_at, Timestamp updated_at){
}