package Services.PaymentService.Controller;

import Services.PaymentService.Dto.*;
import Services.PaymentService.Service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private AuthenticationService authService;
    // Define the logger
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @PostMapping(path = "auth/registerUser", produces = "application/json")
    public ResponseEntity<GenResult> registerUser(@RequestBody UserRequest request) {
        try {
            logger.info("About to register a new User....");
            GenResult result = authService.registerUser(request);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenResult(false, "Failed to register new user", null ));
            }
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseEntity.status(500)
                    .body(new GenResult(false,"An unexpected error occurred in registerUser: " + e.getMessage(),null));
        }
    }

    @PostMapping(path = "auth/loginUser", produces = "application/json")
    public ResponseEntity<GenResult> loginUser(@RequestBody LoginRequest request) {
        try {
            logger.info("About to login the user...");
            GenResult result = authService.login(request.emailAddress(),request.password());
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenResult(false, "Failed to login user", null ));
            }
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseEntity.status(500)
                    .body(new GenResult(false,"An unexpected error occurred in loginUser: " + e.getMessage(),null));
        }
    }
}
