package com.seccertificate.certificateservice.security;

import com.seccertificate.certificateservice.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    
    private Long id;
    private String email;
    private String password;
    private String role;
    private Customer.CustomerStatus status;
    
    public static CustomUserDetails create(Customer customer) {
        return new CustomUserDetails(
                customer.getId(),
                customer.getEmail(),
                customer.getPassword(),
                customer.getRole(),
                customer.getStatus()
        );
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return email;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return status == Customer.CustomerStatus.ACTIVE;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return status == Customer.CustomerStatus.ACTIVE;
    }
}
