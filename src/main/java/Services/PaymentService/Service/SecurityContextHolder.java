package Services.PaymentService.Service;

import Services.PaymentService.Service.Interface.Authentication;

import java.io.Serializable;

public class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> contextHolder = new ThreadLocal<>();

    public static SecurityContext getContext() {
        SecurityContext ctx = contextHolder.get();
        if (ctx == null) {
            ctx = createEmptyContext();
            contextHolder.set(ctx);
        }
        return ctx;
    }

    public static void setContext(SecurityContext context) {
        contextHolder.set(context);
    }

    public static SecurityContext createEmptyContext() {
        return new SecurityContextImpl();
    }

    // Thread-local storage ensures thread safety
    public interface SecurityContext extends Serializable {
        Authentication getAuthentication();
        void setAuthentication(Authentication authentication);
    }

    public static class SecurityContextImpl implements SecurityContext {
        private Authentication authentication;

        @Override
        public Authentication getAuthentication() {
            return this.authentication;
        }

        @Override
        public void setAuthentication(Authentication authentication) {
            this.authentication = authentication;
        }
    }
}
