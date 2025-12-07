package com.seccertificate.certificateservice.security;

import com.seccertificate.certificateservice.entity.Customer;
import com.seccertificate.certificateservice.repository.CustomerRepository;
import jakarta.servlet.FilterChain;
import org.springframework.lang.NonNull;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {
    
    private final CustomerRepository customerRepository;
    
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String apiKey = getApiKeyFromRequest(request);
            
            if (StringUtils.hasText(apiKey)) {
                Customer customer = customerRepository.findActiveCustomerByApiKey(apiKey).orElse(null);
                
                if (customer != null) {
                    CustomUserDetails userDetails = CustomUserDetails.create(customer);
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            logger.error("Could not set API key authentication", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getApiKeyFromRequest(HttpServletRequest request) {
        return request.getHeader("X-API-Key");
    }
}
