package Services.PaymentService.Dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoginResponse {
    private String username;
    private String access_token;
    private Timestamp login_time;

    // Constructor
    public LoginResponse(String username, String access_token, Timestamp login_time) {

        this.username = username;
        this.access_token = access_token;
        this.login_time = login_time;
    }

    public String getUsername() {
        return username;
    }

    public String getAccessToken() {
        return access_token;
    }

    public Timestamp getLoginTime() {
        return login_time;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAccessToken(String access_token) {
        this.access_token = access_token;
    }

    public void setLoginTime(Timestamp login_time) {
        this.login_time = login_time;
    }

}
