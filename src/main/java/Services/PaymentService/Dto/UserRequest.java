package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;

import java.util.UUID;

public record UserRequest (String firstName, String lastName, String emailAddress, String phoneNumber,
                           String password, String status, String location, AccountType accountType) {
}
