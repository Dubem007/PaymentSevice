package Services.PaymentService.Controller;

import Services.PaymentService.Dto.PaymentRequest;
import Services.PaymentService.Dto.PaymentResult;
import Services.PaymentService.Service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    // Define the logger
    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);


    @PostMapping(path = "pay/make_payment", produces = "application/json")
    public ResponseEntity<PaymentResult> processPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            logger.info("About Processing payment status request");
            PaymentResult result = paymentService.processPayment(request);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PaymentResult(false, "Payment failed to process."));
            }
            if (result.isSuccess()) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return ResponseEntity.status(500)
                    .body(PaymentResult.failure("An unexpected error occurred: " + e.getMessage()));
        }
    }
}
