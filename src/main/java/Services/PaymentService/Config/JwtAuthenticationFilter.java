package Services.PaymentService.Config;

import Services.PaymentService.Service.Interface.GrantedAuthority;
import Services.PaymentService.Service.SecurityContextHolder;
import Services.PaymentService.Service.SimpleGrantedAuthority;
import Services.PaymentService.Service.UsernamePasswordAuthenticationToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider tokenProvider;
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal( HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromHeader(request);
        logger.info("The extracted token is: {}", token);

        // Check if token is present and valid
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            logger.info("The extracted username is: {}", username);

            if (username != null && !username.isEmpty()) {
                // Extract roles from the token and convert them to GrantedAuthorities
                List<String> roles = tokenProvider.getRolesFromToken(token);
                logger.info("Extracted roles: {}", roles);

                // Create a list of authorities based on the roles
                List<GrantedAuthority> authorities = roles.stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role)) // Add ROLE_ prefix to role
                        .collect(Collectors.toList());

                String authoritiesString = authorities.stream()
                        .map(authority -> authority.getAuthority()) // Get the actual authority string
                        .collect(Collectors.joining(", "));

                logger.info("The authorities for user: {}", authoritiesString);
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                logger.info("set Authentication for user: {}", authentication);
                // Set authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.info("Authentication set successfully for user: {}", username);

            } else {
                logger.warn("Failed to extract username from token.");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No username retrieved from token");
            }
        } else {
            logger.warn("No token provided.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token provided");
        }
        // Always continue the filter chain processing to allow the request to proceed
        filterChain.doFilter(request, response);
    }


    private String extractTokenFromHeader(HttpServletRequest request) {
        logger.info("About to extractTokenFromHeader...");
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
