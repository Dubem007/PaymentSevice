package Services.PaymentService.Dto;

import java.util.UUID;

public record LoginRequest (String emailAddress, String password) {
}
