package Services.PaymentService.Dto;

import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Enums.CardType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDto (UUID Id, String accountNumber, BigDecimal balance,String accountName, String bank,
                         AccountType accountType){

}
