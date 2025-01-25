package Services.PaymentService.Config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider {
    private final Key SECRET_KEY;  // Generates a secure key
    private static final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds

    @Autowired
    public JwtTokenProvider() {
        String secret = "0p1xIH6b7Xm2HX+9oxFAJZa3mTqePbC/SdCzDOXe0Aw="; // Replace with your secret
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
        // this.SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // Method to generate a JWT token
    public String generateToken(UUID userId, String accountNumber, UUID walletId,String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountNumber", accountNumber); // Add account number to claims
        claims.put("walletId", walletId); // Add walletId to claims
        claims.put("username", username); // Add username to claims
        claims.put("userid", userId.toString()); // Add username to claims

        return Jwts.builder()
                .setSubject(username) // Set the user ID as the subject of the token
                .setClaims(claims) // Set the claims (map of additional data)
                .setIssuedAt(new Date()) // Set the current timestamp
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Set expiration time
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // Sign the token using the secret key and algorithm
                .compact(); // Build the token
    }

    // Method to validate the JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // Set the signing key to validate the token
                    .build()
                    .parseClaimsJws(token); // Parse the token
            return true; // If no exception is thrown, the token is valid
        } catch (Exception e) {
            return false; // Invalid token
        }
    }

    // Method to extract the username from the JWT token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY) // Set the signing key to parse the token
                .build()
                .parseClaimsJws(token) // Parse the token
                .getBody() // Get the body (claims)
                .getSubject(); // Extract the subject (username)
    }
}
