package Services.PaymentService.Dto;

public class PaymentResult {
    private final boolean success;
    private final String message;

    public PaymentResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getter for success
    public boolean isSuccess() {
        return success;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }
    // Static factory methods, getters, and constructors
    public PaymentResult success() {
        return new PaymentResult(true, "Payment successful");
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message);
    }

}
