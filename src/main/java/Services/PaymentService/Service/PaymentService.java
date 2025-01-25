package Services.PaymentService.Service;

import Services.PaymentService.Controller.PaymentController;
import Services.PaymentService.Dto.PaymentRequest;
import Services.PaymentService.Dto.PaymentResult;
import Services.PaymentService.Dto.TransactionDto;
import Services.PaymentService.Enums.PaymentMethod;
import Services.PaymentService.Enums.TransactionStatus;
import Services.PaymentService.Enums.TransactionType;
import Services.PaymentService.Models.Transaction;
import Services.PaymentService.Repository.PaymentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
    private final PaymentGateway paymentGateway;
    @Autowired
    private final PaymentRepository payRepository;
    @Autowired
    private final SecurityManager securityManager;
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(PaymentGateway paymentGateway,
                          PaymentRepository payRepository,
                          SecurityManager securityManager) {
        this.paymentGateway = paymentGateway;
        this.payRepository = payRepository;
        this.securityManager = securityManager;
    }

    public PaymentResult processPayment(PaymentRequest request) throws JsonProcessingException {
        // Validate payment request
        ObjectMapper objectMapper = new ObjectMapper();
        String paymentRequestJson = objectMapper.writeValueAsString(request);
        logger.info("About to process payment: {}", paymentRequestJson);
        if (!securityManager.validatePaymentRequest(request)) {
            logger.info("Invalid payment request due to failed validation");
            return PaymentResult.failure("Invalid payment request");
        }
        TransactionDto transaction = switch (request.getPaymentMethod()) {
            case CREDIT_CARD -> createCardTransaction(request);
            case BANK_TRANSFER -> createBankTransaction(request);
            case DIGITAL_WALLET -> createWalletTransaction(request);
        };
        logger.info("Successfully created transaction for payment method: {}", transaction.getPaymentMethod());
        logger.info("Successfully created transaction for payment Id: {}", transaction.getId());
        try {
            // Process payment based on payment method
            PaymentResult result = switch (request.getPaymentMethod()) {
                case CREDIT_CARD -> processCreditCardPayment(request);
                case BANK_TRANSFER -> processBankTransfer(request);
                case DIGITAL_WALLET -> processDigitalWalletPayment(request);
            };
            logger.info("The payment result after processing: {}", result.isSuccess());
            // Update transaction status
            updateTransactionStatus(transaction, result.isSuccess());

            return result;
        } catch (Exception e) {
            // Handle unexpected errors
            logger.error("Payment processing error: {}", e.getMessage());
            transaction.setTransactionStatus(TransactionStatus.FAILED);
            payRepository.save(transaction);
            return new PaymentResult(false,"Payment processing error: " + e.getMessage());
        }
    }

    private TransactionDto createCardTransaction(PaymentRequest request) {
        PaymentMethod resp = request.getPaymentMethod();

        TransactionType req = request.getTransactionType() == TransactionType.CREDIT
                ? TransactionType.CREDIT
                : TransactionType.DEBIT;

        TransactionDto transaction = new TransactionDto(
                null,
                req,
                request.getAmount(),
                request.getCurrency(),
                resp,
                request.getCardDetails().toString(),
                Timestamp.valueOf(LocalDateTime.now()),
                TransactionStatus.PENDING
        );
        return payRepository.save(transaction);
    }

    private TransactionDto createBankTransaction(PaymentRequest request) {
        PaymentMethod resp = request.getPaymentMethod();

        TransactionType req = request.getTransactionType() == TransactionType.CREDIT
                ? TransactionType.CREDIT
                : TransactionType.DEBIT;

        TransactionDto transaction = new TransactionDto(
                null,
                req,
                request.getAmount(),
                request.getCurrency(),
                resp,
                request.getBankDetails().toString(),
                Timestamp.valueOf(LocalDateTime.now()),
                TransactionStatus.PENDING
        );
        return payRepository.save(transaction);
    }

    private TransactionDto createWalletTransaction(PaymentRequest request) {
        PaymentMethod resp = request.getPaymentMethod();

        TransactionType req = request.getTransactionType() == TransactionType.CREDIT
                ? TransactionType.CREDIT
                : TransactionType.DEBIT;

        TransactionDto transaction = new TransactionDto(
                null,
                req,
                request.getAmount(),
                request.getCurrency(),
                resp,
                request.getWalletDetails().toString(),
                Timestamp.valueOf(LocalDateTime.now()),
                TransactionStatus.PENDING
        );
        return payRepository.save(transaction);
    }

    private PaymentResult processCreditCardPayment(PaymentRequest request) {
        // Implement credit card payment logic
        return paymentGateway.processCreditCardPayment(request);
    }

    private PaymentResult processBankTransfer(PaymentRequest request) {
        // Implement bank transfer logic
        return paymentGateway.processBankTransfer(request);
    }

    private PaymentResult processDigitalWalletPayment(PaymentRequest request) {
        // Implement digital wallet payment logic
        return paymentGateway.processDigitalWalletPayment(request);
    }

    private void updateTransactionStatus(TransactionDto transaction, boolean result) {

        TransactionStatus newStatus = result ?
                TransactionStatus.COMPLETED :
                TransactionStatus.FAILED;

        payRepository.updateTransactionStatus(transaction.getId(),newStatus);
    }
}
