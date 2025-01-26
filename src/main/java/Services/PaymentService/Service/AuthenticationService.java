package Services.PaymentService.Service;

import Services.PaymentService.Config.JwtTokenProvider;
import Services.PaymentService.Dto.*;
import Services.PaymentService.Enums.AccountType;
import Services.PaymentService.Enums.TransactionStatus;
import Services.PaymentService.Enums.WalletType;
import Services.PaymentService.Repository.AccountRepository;
import Services.PaymentService.Repository.PaymentRepository;
import Services.PaymentService.Repository.UserRepository;
import Services.PaymentService.Repository.WalletRepository;
import Services.PaymentService.Utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class AuthenticationService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final AccountRepository accountRepository;
    @Autowired
    private final WalletRepository walletRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtTokenProvider tokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(PaymentGateway.class);
    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenProvider tokenProvider,AccountRepository accountRepository,WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.accountRepository = accountRepository;
        this.walletRepository = walletRepository;
    }
    // User Registration
    public GenResult registerUser(UserRequest model) {
        try{
            logger.info("About to register a new user ....");
            // Check if user exists
            UserDetails user_exists = userRepository.findByEmail(model.emailAddress());

            if (user_exists != null) {
                logger.info("Username already exists ....");
                return new GenResult(false, "Username already exists", null);
            }
            // Encode password
            String encodedPassword = passwordEncoder.encode(model.password());

            UserDto newUser = new UserDto(
                    null,
                    model.firstName(),
                    model.lastName(),
                    model.emailAddress(),
                    model.phoneNumber(),
                    encodedPassword,
                    "APPROVED",
                    model.location(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            logger.info("About to create new user....");
            UserDto new_user = userRepository.createUser(newUser);
            logger.info("Successfully created the new user with Id: {}",new_user.getId());
            if(new_user.getId() == null)
            {
                return new GenResult(false, "Failed to create new user", null);
            }

            // Create account and wallet
            String new_Account = StringUtils.generateAccountNumber();
            String new_AccountName = model.firstName() + " " + model.lastName();
            AccountRequest account = new AccountRequest(
                    null,
                    new_Account,
                    BigDecimal.TEN,
                    new_AccountName,
                    "InterSwitch",
                    model.accountType(),
                    new_user.getId()
            );
            logger.info("About to create account for new user....");
            AccountRequest resp_account = accountRepository.createNewAccount(account);
            logger.info("Successfully created account for the new user with Id: {}",resp_account.getId());
            if(resp_account.getId() == null)
            {
                return new GenResult(false, "Failed to create new account for user", null);
            }

            WalletType wallettype = model.accountType().equals(AccountType.SAVINGS) ? WalletType.PERSONAL : WalletType.BUSINESS;
            CreateWallet wallet = new CreateWallet(
                    null,
                    new_AccountName,
                    BigDecimal.TEN,
                    wallettype,
                    resp_account.getId()
            );
            logger.info("About to create wallet for new user....");
            CreateWallet resp_wallet =walletRepository.createNewWallet(wallet);
            logger.info("Successfully created wallet for the new user with Id: {}",resp_wallet.getId());
            if(resp_wallet.getId() == null)
            {
                return new GenResult(false, "Failed to create new account for user", null);
            }
            return new GenResult(true, "User successfully created", newUser);
        }catch (Exception e) {
            // Handle unexpected errors
            logger.error("Failed to create new user with error: {}", e.getMessage());
            return new GenResult(false,"Failed to create new user with error: " + e.getMessage(), null);
        }  // Retrieve user from database

    }

    // User Login
    public GenResult login(String username, String password) {

      try{
          logger.info("About to proceed with login user....");
          UserDetails user_exists = userRepository.findByEmail(username);

          if (user_exists == null) {

              return new GenResult(false, "Username does not exists", null);
          }
          String existing_password = user_exists.password();
          // Verify password
          logger.info("Thc login password, {}",password);
          logger.info("Thc existing password, {}",existing_password);
          if (!passwordEncoder.matches(password, existing_password)) {
              logger.info("Invalid password provided....");
              return new GenResult(false, "Invalid password provided", null);
          }
          logger.info("About to get user account details....");
          AccountDto accountUser = accountRepository.getAccountDetailsByUserId(user_exists.Id());
          if(accountUser.Id() == null)
          {
              logger.info("User does not have any account created....");
              return new GenResult(false, "User does not have any account created", null);
          }
          logger.info("About to get user wallet details....");
          WalletDetails walletUser = walletRepository.getWalletDetailsByAccountId(accountUser.Id());
          if(walletUser.Id() == null)
          {
              logger.info("User does not have any wallet created....");
              return new GenResult(false, "User does not have any wallet created", null);
          }
          List<String> roles = Collections.singletonList("ADMIN_USER");
          // Generate JWT
          String token = tokenProvider.generateToken(user_exists.Id(), accountUser.accountNumber(),walletUser.Id(), user_exists.emailAddress(),roles);
          if(token == null)
          {
              logger.info("Failed to login User and generate token ....");
              return new GenResult(false, "Failed to login User and generate token", null);
          }
          LoginResponse resp = new LoginResponse(
                  user_exists.emailAddress(),token, LocalDateTime.now()
          );
          return new GenResult(true, "User successful login", resp);

      } catch (Exception e) {
          // Handle unexpected errors
          logger.error("login error: {}", e.getMessage());
          return new GenResult(false,"login error error: " + e.getMessage(), null);
      }  // Retrieve user from database


    }
}
