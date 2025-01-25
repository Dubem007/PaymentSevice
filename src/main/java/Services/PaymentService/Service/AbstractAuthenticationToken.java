package Services.PaymentService.Service;

import Services.PaymentService.Service.Interface.Authentication;
import Services.PaymentService.Service.Interface.CredentialsContainer;
import Services.PaymentService.Service.Interface.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractAuthenticationToken implements Authentication, CredentialsContainer {
    private final Collection<? extends GrantedAuthority> authorities;
    private Object details;
    private boolean authenticated = false;

    // Constructor
    public AbstractAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities != null ? authorities : Collections.emptyList();
    }

    // Authentication Methods
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public boolean isAuthenticated() {
        return this.authenticated;
    }

    // Overridable Authentication Method
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    // Additional Methods
    public void setDetails(Object details) {
        this.details = details;
    }

    public Object getDetails() {
        return this.details;
    }

    // Implements CredentialsContainer
    @Override
    public void eraseCredentials() {
        // Default implementation to clear sensitive authentication information
    }

    public abstract Object getPrincipal();
}
