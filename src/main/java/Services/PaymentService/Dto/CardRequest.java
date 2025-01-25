package Services.PaymentService.Dto;

import Services.PaymentService.Enums.CardType;

import java.math.BigDecimal;

public record CardRequest(String sourceCardNumber, String sourceCardHolderName, String cvv, String expiryDate,
                             CardType cardType, String destinationCardNumber, String destinationCardHolderName) {
}

