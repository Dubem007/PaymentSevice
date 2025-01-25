package Services.PaymentService.Dto;

public class GenResult {
    private final boolean success;
    private final String message;
    private final Object data;

    public GenResult(boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getter for success
    public boolean isSuccess() {
        return success;
    }

    // Getter for message
    public String getMessage() {
        return message;
    }

    // Getter for message
    public Object getData() {
        return data;
    }
    public static GenResult success(String message, Object data) {
        return new GenResult(true, message, data);
    }

    public static GenResult failure(String message, Object data) {
        return new GenResult(false, message, data);
    }
}
