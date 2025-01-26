package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record UserRequest (
        @NotEmpty(message = "First name cannot be empty")
        @Size(max = 50, message = "First name must be at most 50 characters")
        String firstName,

        @NotEmpty(message = "Last name cannot be empty")
        @Size(max = 50, message = "Last name must be at most 50 characters")
        String lastName,

        @NotEmpty(message = "Email address cannot be empty")
        @Email(message = "Invalid email address format")
        String emailAddress,

        @NotEmpty(message = "Phone number cannot be empty")
        @Pattern(regexp = "\\+?[0-9]+", message = "Phone number must contain only digits and an optional leading '+'")
        String phoneNumber,

        @NotEmpty(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password,

        @NotEmpty(message = "Status cannot be empty")
        String status,

        @NotEmpty(message = "Location cannot be empty")
        String location,

        @NotNull(message = "Account type cannot be null")
        AccountType accountType) {
}
