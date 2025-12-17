package com.seccertificate.certificateservice.service;

import com.seccertificate.certificateservice.dto.AuthResponse;
import com.seccertificate.certificateservice.dto.CustomerDTO;
import com.seccertificate.certificateservice.dto.LoginRequest;
import com.seccertificate.certificateservice.security.CustomUserDetails;
import com.seccertificate.certificateservice.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_shouldReturnAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("password");

        CustomUserDetails principal = new CustomUserDetails(1L, request.getEmail(), "pwd", "USER", com.seccertificate.certificateservice.entity.Customer.CustomerStatus.ACTIVE);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-token");

        CustomerDTO dto = new CustomerDTO();
        dto.setId(1L);
        dto.setEmail(request.getEmail());
        dto.setApiKey("api-key-1");
        when(customerService.getCustomerById(1L)).thenReturn(dto);

        AuthResponse res = authService.login(request);

        assertEquals("jwt-token", res.getToken());
        assertEquals("Bearer", res.getType());
        assertEquals("api-key-1", res.getApiKey());
        assertEquals(1L, res.getCustomer().getId());
        assertEquals(request.getEmail(), res.getCustomer().getEmail());
    }
}
