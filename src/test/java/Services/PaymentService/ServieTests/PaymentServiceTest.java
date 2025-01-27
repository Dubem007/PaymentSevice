package Services.PaymentService.ServieTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import Services.PaymentService.Dto.*;
import Services.PaymentService.Enums.*;
import Services.PaymentService.Repository.AccountRepository;
import Services.PaymentService.Repository.CardDetailsRepository;
import Services.PaymentService.Repository.WalletRepository;
import Services.PaymentService.Service.PaymentGateway;
import Services.PaymentService.Service.PaymentRiskService;
import Services.PaymentService.Service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private CardDetailsRepository cardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentRiskService paymentRiskService;

    @Mock
    private PaymentGateway paymentGateway;
    @Mock
    private PaymentResult expectedPaymentResult;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessCreditCardPayment_SuccessfulPayment() {
        // Arrange
        PaymentRequest request = mockPaymentRequest();

        CardDetailsDto sourceCard = new CardDetailsDto(
                UUID.fromString("1b8b5b80-dc3f-409a-9b9d-9a2dd72b01c2"),
                "5551111111111111",
                "mike iko-kosi",// Use a dynamically generated UUID// Valid CVV
                BigDecimal.valueOf(10L),
                "223",
                "12/2025",
                 CardType.DEBIT,
                UUID.fromString("155ca0f9-1265-4a7c-bdea-b8fcc12dddce")
        );

        CardDetailsDto destinationCard = new CardDetailsDto(
                UUID.fromString("77292b21-21a3-4d5a-bd3c-d9304ed5e63b"),
                "9991111111111111",
                "dubem paul",// Use a dynamically generated UUID// Valid CVV
                BigDecimal.valueOf(10L),
                "233",
                "12/2025",
                CardType.CREDIT,
                UUID.fromString("2288ec1e-9cbd-4571-b222-f191e08039f4")               // Valid account ID
        );

        AccountDto sourceAccount = new AccountDto(UUID.fromString("155ca0f9-1265-4a7c-bdea-b8fcc12dddce"), "9501809365",  BigDecimal.valueOf(510L),"mike iko-kosi","InterSwitch", AccountType.SAVINGS);
        AccountDto destinationAccount = new AccountDto(UUID.fromString("2288ec1e-9cbd-4571-b222-f191e08039f4"), "8903418903",  BigDecimal.valueOf(4500L),"dubem paul","InterSwitch", AccountType.CURRENT);

        WalletDetails sourceWallet = new WalletDetails(UUID.fromString("1b046763-b726-4ce0-9f37-638bd1e55e66"), "mike iko-kosi",  BigDecimal.valueOf(10L), WalletType.PERSONAL,UUID.fromString("155ca0f9-1265-4a7c-bdea-b8fcc12dddce"));
        WalletDetails destinationWallet = new WalletDetails(UUID.fromString("02199661-3bf4-4f1a-ae15-5866de72a64d"), "dubem paul",  BigDecimal.valueOf(4500L), WalletType.BUSINESS,UUID.fromString("2288ec1e-9cbd-4571-b222-f191e08039f4"));

        when(cardRepository.getCardDetails("5551111111111111")).thenReturn(sourceCard);
        when(cardRepository.getCardDetails("9991111111111111")).thenReturn(destinationCard);
        when(accountRepository.getAccountDetailsById(UUID.fromString("155ca0f9-1265-4a7c-bdea-b8fcc12dddce"))).thenReturn(sourceAccount);
        when(accountRepository.getAccountDetailsById(UUID.fromString("2288ec1e-9cbd-4571-b222-f191e08039f4"))).thenReturn(destinationAccount);
        when(walletRepository.getWalletDetailsByAccountId(UUID.fromString("155ca0f9-1265-4a7c-bdea-b8fcc12dddce"))).thenReturn(sourceWallet);
        when(walletRepository.getWalletDetailsByAccountId(UUID.fromString("2288ec1e-9cbd-4571-b222-f191e08039f4"))).thenReturn(destinationWallet);
        when(paymentRiskService.assessTransactionRisk(anyString(), any())).thenReturn(true);
        when(expectedPaymentResult = new PaymentResult(true, "Payment processed successfully"));
        when(expectedPaymentResult.isSuccess()).thenReturn(true);
        when(expectedPaymentResult.getMessage()).thenReturn("Payment processed successfully");
        when(paymentGateway.processCreditCardPayment(request)).thenReturn(expectedPaymentResult);
        // Act
        expectedPaymentResult = paymentService.processCreditCardPayment(request);

        // Assert
        // assertTrue(expectedPaymentResult.isSuccess());
        assertEquals("Payment processed successfully", expectedPaymentResult.getMessage());

        verify(walletRepository).update(UUID.fromString("1b046763-b726-4ce0-9f37-638bd1e55e66"), new BigDecimal("500.00")); // Source wallet updated
        verify(walletRepository).update(UUID.fromString("02199661-3bf4-4f1a-ae15-5866de72a64d"), new BigDecimal("4510.00")); // Destination wallet updated
    }

    @Test
    void testProcessCreditCardPayment_InvalidSourceCard() {
        // Arrange
        PaymentRequest request = mockPaymentRequest();
        when(cardRepository.getCardDetails("1234567890123456")).thenReturn(null);

        // Act
        PaymentResult result = paymentService.processCreditCardPayment(request);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Card not found", result.getMessage());
    }

    @Test
    void testProcessCreditCardPayment_InsufficientFunds() {
        // Arrange
        PaymentRequest request = mockPaymentRequest();

        CardDetailsDto sourceCard = new CardDetailsDto(
                UUID.fromString("1b8b5b80-dc3f-409a-9b9d-9a2dd72b01c2"),
                "5551111111111111",
                "mike iko-kosi",// Use a dynamically generated UUID// Valid CVV
                BigDecimal.valueOf(550L),
                "223",
                "12/2025",
                CardType.DEBIT,
                UUID.fromString("155ca0f9-1265-4a7c-bdea-b8fcc12dddce"));
        when(cardRepository.getCardDetails("5551111111111111")).thenReturn(sourceCard);

        // Act
        PaymentResult result = paymentService.processCreditCardPayment(request);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("Insufficient funds", result.getMessage());
    }


    // Mock PaymentRequest
    private PaymentRequest mockPaymentRequest() {
        CardRequest cardDetails = new CardRequest("5551111111111111",
                "mike iko-kosi",
                "223",// Use a dynamically generated UUID// Valid CVV
                "12/2025",
                CardType.DEBIT,
                "5551111111111111",
                "5551111111111111");
        WalletPaymentRequest walletDetails = new WalletPaymentRequest(UUID.fromString("1b046763-b726-4ce0-9f37-638bd1e55e66"),UUID.fromString("1b046763-b726-4ce0-9f37-638bd1e55e66"),"New apyment");
        BankPaymentRequest bankDetails = new BankPaymentRequest("9501809365","8903418903","Soruce bank","Destination Bank",AccountType.SAVINGS,"New apyment");
        return new PaymentRequest(
                new BigDecimal("10.00"),
                "NGN",
                TransactionType.DEBIT,
                cardDetails,
                bankDetails,
                walletDetails,
                PaymentMethod.CREDIT_CARD);
    }
}

