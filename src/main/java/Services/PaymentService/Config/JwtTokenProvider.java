package Services.PaymentService.Config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;

@Component
public class JwtTokenProvider {
    private final Key SECRET_KEY;  // Generates a secure key
    private static final long EXPIRATION_TIME = 86400000; // 24 hours in milliseconds
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Autowired
    public JwtTokenProvider() {
        String secret = "0p1xIH6b7Xm2HX+9oxFAJZa3mTqePbC/SdCzDOXe0Aw="; // Replace with your secret
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
        // this.SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    // Method to generate a JWT token
    public String generateToken(UUID userId, String accountNumber, UUID walletId,String username,List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("accountNumber", accountNumber);
        claims.put("walletId", walletId);
        claims.put("username", username);
        claims.put("userid", userId.toString());
        claims.put("subject", username);
        claims.put("roles", roles);

        logger.info("generateToken secret key : {}",SECRET_KEY);
        return Jwts.builder()
                .setSubject(username)
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // Method to validate the JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Method to extract the username from the JWT token
    public String getUsernameFromToken(String token) {
        try {
            logger.info("Validating token: {}", token);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.info("Claims: {}", claims);
            // Extract the username from custom claims
            return claims.get("username", String.class);
        } catch (JwtException e) {
            logger.error("Error parsing token: {}", e.getMessage());
            return null; // Invalid token
        }

    }

    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extract the roles claim
            Object rolesObject = claims.get("roles");

            if (rolesObject instanceof List) {
                // The roles are stored as a List in the token
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) rolesObject;
                return roles;
            } else {
                logger.warn("Roles claim is not a list: {}", rolesObject);
                return Collections.emptyList(); // Return an empty list if roles are not present
            }
        } catch (JwtException e) {
            logger.error("Error extracting roles from token: {}", e.getMessage());
            return Collections.emptyList(); // Return an empty list if the token is invalid
        }
    }

}
