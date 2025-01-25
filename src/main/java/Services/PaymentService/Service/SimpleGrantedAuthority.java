package Services.PaymentService.Service;

import Services.PaymentService.Service.Interface.GrantedAuthority;

    public class SimpleGrantedAuthority implements GrantedAuthority {
        private final String role;

        public SimpleGrantedAuthority(String role) {
            this.role = role;
        }

        @Override
        public String getAuthority() {
            return this.role;
        }
    }

