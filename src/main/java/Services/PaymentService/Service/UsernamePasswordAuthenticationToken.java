package Services.PaymentService.Service;

import Services.PaymentService.Service.Interface.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class UsernamePasswordAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private final Object credentials;

    // Constructor for unauthenticated token
    public UsernamePasswordAuthenticationToken(Object principal, Object credentials) {
        super(Collections.emptyList());
        this.principal = principal;
        this.credentials = credentials;
    }

    // Constructor for authenticated token
    public UsernamePasswordAuthenticationToken(
            Object principal,
            Object credentials,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public String getName() {
        return "";
    }
}
