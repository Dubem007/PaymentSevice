package Services.PaymentService.Dto;

import Services.PaymentService.Enums.CardType;

import java.math.BigDecimal;
import java.util.UUID;

public record CardDetailsDto(UUID Id, String cardNumber, String cardHolderName, BigDecimal balance, String cvv,String expiryDate,
                             CardType cardType) {
}
