package Services.PaymentService.Service;

import Services.PaymentService.Dto.*;
import Services.PaymentService.Enums.PaymentMethod;
import Services.PaymentService.Enums.TransactionType;
import Services.PaymentService.Models.Account;
import Services.PaymentService.Repository.AccountRepository;
import Services.PaymentService.Repository.CardDetailsRepository;
import Services.PaymentService.Repository.WalletRepository;
import Services.PaymentService.Utils.PaymentValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PaymentGateway {
    @Autowired
    private final CardDetailsRepository cardRepository;
    @Autowired
    private final AccountRepository accountRepository;
    @Autowired
    private final WalletRepository walletRepository;
    @Autowired
    private final PaymentRiskService paymentRiskService;
    @Autowired
    private final ComplianceService complianceService;
    @Autowired
    private final TransactionLimitService transactionLimitService;
    private static final Logger logger = LoggerFactory.getLogger(PaymentGateway.class);

    public PaymentGateway(CardDetailsRepository cardRepository,AccountRepository accountRepository,WalletRepository walletRepository,PaymentRiskService paymentRiskService,ComplianceService complianceService,TransactionLimitService transactionLimitService) {
        this.cardRepository = cardRepository;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
        this.paymentRiskService = paymentRiskService;
        this.complianceService = complianceService;
        this.transactionLimitService = transactionLimitService;
    }

    public PaymentResult processCreditCardPayment(PaymentRequest request) {
        try {
            // Validate input parameters
            logger.info("About to start processCreditCardPayment...");

            if (!PaymentValidationUtils.isValidCard(request.getCardDetails().sourceCardNumber())) {
                logger.info("Invalid source card number...");
                return new PaymentResult(false, "Invalid source card number");
            }

            if (!PaymentValidationUtils.isValidCard(request.getCardDetails().destinationCardNumber())) {
                logger.info("Invalid destination card number...");
                return new PaymentResult(false, "Invalid destination card number");
            }

            if (!paymentRiskService.assessTransactionRisk(request.getCardDetails().sourceCardNumber(), request.getAmount())) {
                logger.info("Card is not fit to process payment...");
                return new PaymentResult(false, "Card is not fit to process payment");
            }

            if (!PaymentValidationUtils.isValidCVV(request.getCardDetails().cvv())) {
                logger.info("Invalid CVV...");
                return new PaymentResult(false, "Invalid CVV provided");
            }

            if (!PaymentValidationUtils.isValidAmount(request.getAmount())) {
                logger.info("Invalid payment amount provided...");
                return new PaymentResult(false, "Invalid payment amount provided");
            }

            // Fetch card details from the database
            logger.info("About to retrieve source card details.....");
            CardDetailsDto cardDetails = cardRepository.getCardDetails(request.getCardDetails().sourceCardNumber());
            logger.info("Successfully retrieved source card details with card number : {}", cardDetails.cardNumber());
            if (cardDetails.cardNumber() == null) {
                return new PaymentResult(false, "Card not found");
            }

            // Validate CVV
            if (!cardDetails.cvv().equals(request.getCardDetails().cvv())) {
                return new PaymentResult(false, "Invalid CVV");
            }

            // Check if sufficient balance is available
            if (cardDetails.balance().compareTo(request.getAmount()) < 0) {
                return new PaymentResult(false, "Insufficient funds");
            }

            logger.info("About to destination card details.....");
            CardDetailsDto desti_cardDetails = cardRepository.getCardDetails(request.getCardDetails().destinationCardNumber());
            logger.info("Successfully retrieved destination card details with card number : {}", desti_cardDetails.cardNumber());
            if (desti_cardDetails.cardNumber() == null) {
                return new PaymentResult(false, "Card not found");
            }

            // Debit the amount and update balance
            logger.info("About to updateCardBalances for both cards....");
            boolean updateSuccess = updateCardBalance(cardDetails, desti_cardDetails, request.getAmount(),request.getTransactionType());
            logger.info("updateCardBalance with response as : {}", updateSuccess);

            //Accounts
            logger.info("About to get source account details.....");
            AccountDto account_cardDetails = accountRepository.getAccountDetailsById(cardDetails.accountId());
            logger.info("Successfully retrieved source account details with card number : {}", cardDetails.cardNumber());

            if (account_cardDetails.accountNumber() == null) {
                return new PaymentResult(false, "Card not found");
            }

            logger.info("About to get destination account details.....");
            AccountDto account_destination_cardDetails = accountRepository.getAccountDetailsById(desti_cardDetails.accountId());
            logger.info("Successfully retrieved destination account details with card number : {}", desti_cardDetails.cardNumber());

            if (account_destination_cardDetails.accountNumber() == null) {
                return new PaymentResult(false, "Card not found");
            }
            logger.info("About to performTransfer on both accounts for both cards...");
            boolean transferSuccess = performTransfer(account_cardDetails, account_destination_cardDetails, request.getAmount());
            logger.info("Concluded processBankTransfer for both cards...");

            // Wallets

            logger.info("About to get source wallet details for account reqs.....");
            WalletDetails walletDetails = walletRepository.getWalletDetailsByAccountId(account_cardDetails.Id());
            logger.info("Successfully retrieved source card details with account Id reqs : {}", account_cardDetails.Id());

            if (walletDetails.Id() == null) {
                return new PaymentResult(false, "Card not found");
            }

            logger.info("About to get destination card details for account Id reqs.....");
            WalletDetails destination_walletDetails = walletRepository.getWalletDetailsByAccountId(account_destination_cardDetails.Id());
            logger.info("Successfully retrieved destination card details with account Id reqs : {}", account_destination_cardDetails.Id());

            if (destination_walletDetails.Id() == null) {
                return new PaymentResult(false, "Card not found");
            }

            logger.info("About to update source wallet balance for card...");
            BigDecimal newSourceBalance = walletDetails.balance().subtract(request.getAmount());
            walletRepository.update(request.getWalletDetails().sourceWalletId(), newSourceBalance );
            logger.info("Successfully updated source wallet balance for card...");

            logger.info("About to update destination wallet balance for card...");
            BigDecimal newDestinationBalance = destination_walletDetails.balance().add(request.getAmount());
            walletRepository.update(request.getWalletDetails().destinationWalletId(), newDestinationBalance);
            logger.info("Successfully updated destination wallet balance for card...");


            if (!transferSuccess) {
                return new PaymentResult(false, "Failed to process card account payments");
            }
            if (updateSuccess) {
                return new PaymentResult(true, "Successfully processed card payment Transaction "); // Payment successful
            } else {
                return new PaymentResult(false, "Failed to process card payments");
            }
        } catch (Exception e) {
            // Log the exception and return an error response
            logger.error("Error processing credit card payment with message : {}", e.getMessage());
            logError("Error processing credit card payment", e);
            return new PaymentResult(false, "An unexpected error occurred");
        }
    }


    public PaymentResult processBankTransfer(PaymentRequest request) {
        // Validate input parameters
        logger.info("About to start processBankTransfer...");

        if (!isValidAmount(request.getAmount())) {
            logger.info("Invalid transfer amount provided...");
            return new PaymentResult(false, "Invalid transfer amount provided");
        }

        try {
            // Fetch source and destination account details
            logger.info("About to retrieve source account details : {}", request.getBankDetails().sourceAccountNumber());
            AccountDto sourceAccount = accountRepository.findByAccountNumber(request.getBankDetails().sourceAccountNumber());
            logger.info("Successfully retrieved source account details with accountNumber...{}", sourceAccount.accountNumber());
            logger.info("About to retrieve destination account details : {}", request.getBankDetails().destinationAccountNumber());
            AccountDto destinationAccount = accountRepository.findByAccountNumber(request.getBankDetails().destinationAccountNumber());
            logger.info("Successfully retrieved destination account details with accountNumber...{}", destinationAccount.accountNumber());

            if (sourceAccount.accountNumber() == null) {
                return new PaymentResult(false, "Source accounts not found");
            }

            if (destinationAccount.accountNumber() == null) {
                return new PaymentResult(false, "Destination accounts not found");
            }


            if (!complianceService.validateTransfer(request.getBankDetails().sourceAccountNumber(), request.getAmount())) {
                logger.info("Source account is not fit to process payment...");
                return new PaymentResult(false, "Source account is not fit to process payment");
            }

            // Validate sufficient funds in the source account
            if (sourceAccount.balance().compareTo(request.getAmount()) < 0) {
                logger.info("Insufficient funds in source account...");
                return new PaymentResult(false, "Insufficient funds in source account");
            }

            // Perform the transfer as a single transaction
            logger.info("About to performTransfer on both accounts...");
            boolean transferSuccess = performTransfer(sourceAccount, destinationAccount, request.getAmount());
            logger.info("Concluded processBankTransfer...");


            logger.info("About to retrieve source wallet details for account: {}", sourceAccount.Id());
            WalletDetails sourceWallet = walletRepository.getWalletDetailsByAccountId(sourceAccount.Id());
            logger.info("Successfully retrieved wallet details with account Id: {}", sourceAccount.Id());

            // Debit the source wallet
            logger.info("About to update source wallet balance for account...");
            BigDecimal newSourceBalance = sourceWallet.balance().subtract(request.getAmount());
            walletRepository.update(request.getWalletDetails().sourceWalletId(), newSourceBalance );
            logger.info("Successfully updated source wallet balance for account...");
            // Simulate getting destination wallet details
            logger.info("About to retrieve destination wallet details for account: {}", destinationAccount.Id());
            WalletDetails destinationWallet = walletRepository.getWalletDetailsByAccountId(destinationAccount.Id());
            logger.info("Successfully retrieved destination wallet details with account Id: {}", destinationAccount.Id());
            // Credit the destination wallet

            logger.info("About to update destination wallet balance for account...");
            BigDecimal newDestinationBalance = destinationWallet.balance().add(request.getAmount());
            walletRepository.update(request.getWalletDetails().destinationWalletId(), newDestinationBalance);
            logger.info("Successfully updated destination wallet balance for account...");

            // Cards

            logger.info("About to get source card details for account.....");
            CardDetailsDto card_walletDetails = cardRepository.getCardDetailsByAccountId(sourceAccount.Id());
            logger.info("Successfully retrieved source card details with account Id : {}", sourceWallet.Id());

            if (card_walletDetails.cardNumber() == null) {
                return new PaymentResult(false, "Card not found");
            }

            logger.info("About to get destination card details for account Id.....");
            CardDetailsDto card_destination_walletDetails = cardRepository.getCardDetailsByAccountId(destinationAccount.Id());
            logger.info("Successfully retrieved destination card details with account Id : {}", destinationWallet.Id());

            if (card_destination_walletDetails.cardNumber() == null) {
                return new PaymentResult(false, "Card not found");
            }
            logger.info("About to updateCardBalances for both cards for both accounts....");
            boolean updateSuccess = updateCardBalance(card_walletDetails, card_destination_walletDetails, request.getAmount(),request.getTransactionType());
            logger.info("updateCardBalance for both wallets with response as : {}", updateSuccess);

            if (transferSuccess) {
                return new PaymentResult(true, "Successfully processed bank transfer Transaction ");
            } else {
                return new PaymentResult(false, "Failed to complete the transfer");
            }
        } catch (Exception e) {
            // Log the error and return a failure result
            logger.error("Error processing bank transfer payment with message : {}", e.getMessage());
            logError("Error during transfer", e);
            return new PaymentResult(false, "An unexpected error occurred");
        }
    }

    public PaymentResult processDigitalWalletPayment(PaymentRequest request) {
        // Simulate wallet payment processing
        try {
            logger.info("About to start processDigitalWalletPayment...");
            // Check if source and destination wallets are valid
            if (isValidWallet(request.getWalletDetails().sourceWalletId()) && isValidWallet(request.getWalletDetails().destinationWalletId())) {

                // Simulate getting source wallet details (e.g., balance)
                logger.info("About to retrieve source wallet details for wallet: {}", request.getWalletDetails().sourceWalletId());
                WalletDetails sourceWallet = walletRepository.getWalletDetails(request.getWalletDetails().sourceWalletId());
                logger.info("Successfully retrieved source wallet details with customer name: {}", sourceWallet.customerName());
                // Check if source wallet has enough balance
                if (sourceWallet.balance().compareTo(request.getAmount()) < 0) {
                    logger.info("Insufficient balance in source wallet...");
                    return new PaymentResult(false, "Insufficient balance in source wallet");
                }
                if (!transactionLimitService.isTransactionAllowed(request.getWalletDetails().sourceWalletId(), request.getAmount())) {
                    logger.info("Wallet is not fit to process payment...");
                    return new PaymentResult(false, "Wallet is not fit to process payment");
                }

                // Debit the source wallet
                logger.info("About to update source wallet balance...");
                BigDecimal newSourceBalance = sourceWallet.balance().subtract(request.getAmount());
                walletRepository.update(request.getWalletDetails().sourceWalletId(), newSourceBalance );
                logger.info("Successfully updated source wallet balance...");
                // Simulate getting destination wallet details
                logger.info("About to retrieve destination wallet details for wallet: {}", request.getWalletDetails().destinationWalletId());
                WalletDetails destinationWallet = walletRepository.getWalletDetails(request.getWalletDetails().destinationWalletId());
                logger.info("Successfully retrieved destination wallet details with customer name: {}", destinationWallet.customerName());
                // Credit the destination wallet

                logger.info("About to update destination wallet balance...");
                BigDecimal newDestinationBalance = destinationWallet.balance().add(request.getAmount());
                walletRepository.update(request.getWalletDetails().destinationWalletId(), newDestinationBalance);
                logger.info("Successfully updated destination wallet balance...");

                logger.info("About to get source account details for wallet.....");
                AccountDto account_walletDetails = accountRepository.getAccountDetailsById(sourceWallet.AccountId());
                logger.info("Successfully retrieved source account details with wallet Id : {}", sourceWallet.Id());

                if (account_walletDetails.accountNumber() == null) {
                    return new PaymentResult(false, "Card not found");
                }

                logger.info("About to get destination account details for wallet.....");
                AccountDto account_destination_walletDetails = accountRepository.getAccountDetailsById(destinationWallet.AccountId());
                logger.info("Successfully retrieved destination account details with wallet Id : {}", destinationWallet.Id());

                if (account_destination_walletDetails.accountNumber() == null) {
                    return new PaymentResult(false, "Card not found");
                }
                logger.info("About to performTransfer on both accounts for both wallets...");
                boolean transferSuccess = performTransfer(account_walletDetails, account_destination_walletDetails, request.getAmount());
                logger.info("Concluded processBankTransfer for both wallets...");

                //Cards balance Update

                logger.info("About to get source card details for wallet.....");
                CardDetailsDto card_walletDetails = cardRepository.getCardDetailsByAccountId(account_walletDetails.Id());
                logger.info("Successfully retrieved source card details with wallet Id : {}", sourceWallet.Id());

                if (card_walletDetails.cardNumber() == null) {
                    return new PaymentResult(false, "Card not found");
                }

                logger.info("About to get destination card details for wallet.....");
                CardDetailsDto card_destination_walletDetails = cardRepository.getCardDetailsByAccountId(account_destination_walletDetails.Id());
                logger.info("Successfully retrieved destination card details with wallet Id : {}", destinationWallet.Id());

                if (card_destination_walletDetails.cardNumber() == null) {
                    return new PaymentResult(false, "Card not found");
                }
                logger.info("About to updateCardBalances for both cards requests....");
                boolean updateSuccess = updateCardBalance(card_walletDetails, card_destination_walletDetails, request.getAmount(),request.getTransactionType());
                logger.info("updateCardBalance for both cards with response as : {}", updateSuccess);

                if (!transferSuccess) {
                    return new PaymentResult(false, "Failed to process card account payments");
                }
                return new PaymentResult(true, "Successfully processed wallet Transaction ");  // Success
            }
            return new PaymentResult(false, "Invalid wallet details");
        } catch (Exception e) {
            return new PaymentResult(false, e.getMessage());
        }
    }

    // Method to validate wallet ID
    private boolean isValidWallet(UUID walletId) {
        return walletId != null;
    }

    private boolean performTransfer(AccountDto sourceAccount, AccountDto destinationAccount, BigDecimal amount) {
        try {
            // Start transaction
            //accountRepository.beginTransaction();

            // Debit the source account
            BigDecimal newSourceBalance = sourceAccount.balance().subtract(amount);
            accountRepository.update(sourceAccount,newSourceBalance);

            // Credit the destination account
            BigDecimal newDestinationBalance = destinationAccount.balance().add(amount);
            accountRepository.update(destinationAccount,newDestinationBalance);

            // Commit transaction
            //accountRepository.commitTransaction();
            return true;
        } catch (Exception e) {
            logger.error("Failed to performTransfer with error : {}", e.getMessage());
            // Rollback transaction in case of an error
            // accountRepository.rollbackTransaction();
            logError("Transaction failed", e);
            return false;
        }
    }

    private boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private void logError(String message, Exception e) {
        // Implement proper logging (e.g., using a logging framework like SLF4J or Log4j)
        System.err.println(message + ": " + e.getMessage());
    }

    private boolean updateCardBalance(CardDetailsDto source_cardDetails,CardDetailsDto destination_cardDetails, BigDecimal amount, TransactionType paymentMethod) {
        try {
            BigDecimal newBalance = BigDecimal.ZERO;
            BigDecimal desti_newBalance = BigDecimal.ZERO;
            logger.info("Processing to updateCardBalance...");
            if(paymentMethod.equals(TransactionType.DEBIT)){
                // Subtract the amount from the current balance
                newBalance = source_cardDetails.balance().subtract(amount);
                desti_newBalance = destination_cardDetails.balance().add(amount);
            }else
            {
                // Subtract the amount from the current balance
                newBalance = source_cardDetails.balance().add(amount);
                desti_newBalance = destination_cardDetails.balance().subtract(amount);
            }
            logger.info("Processing to source card balance...");
            cardRepository.updateCardBalance(source_cardDetails.cardNumber(), newBalance);
            logger.info("Successfully Processed source card balance...");
            logger.info("Processing to destination card balance...");
            cardRepository.updateCardBalance(destination_cardDetails.cardNumber(), desti_newBalance);
            logger.info("Successfully Processed source card balance......");
            return true;
        } catch (Exception e) {
            logger.error("Failed to update both cards balances: {}", e.getMessage());
            logError("Failed to update both cards balances", e);
            return false;
        }
    }


}
