package com.seccertificate.certificateservice.service;

import com.seccertificate.certificateservice.dto.AuthResponse;
import com.seccertificate.certificateservice.dto.CustomerDTO;
import com.seccertificate.certificateservice.dto.LoginRequest;
import com.seccertificate.certificateservice.security.CustomUserDetails;
import com.seccertificate.certificateservice.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final CustomerService customerService;
    
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        CustomerDTO customer = customerService.getCustomerById(userDetails.getId());
        
        return AuthResponse.builder()
                .token(jwt)
                .type("Bearer")
                .apiKey(customer.getApiKey())
                .customer(customer)
                .build();
    }
}