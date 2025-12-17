package com.seccertificate.certificateservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seccertificate.certificateservice.dto.ApiResponse;
import com.seccertificate.certificateservice.dto.AuthResponse;
import com.seccertificate.certificateservice.dto.CustomerDTO;
import com.seccertificate.certificateservice.dto.LoginRequest;
import com.seccertificate.certificateservice.security.ApiKeyAuthenticationFilter;
import com.seccertificate.certificateservice.security.JwtAuthenticationFilter;
import com.seccertificate.certificateservice.service.AuthService;
import com.seccertificate.certificateservice.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void login_shouldReturnTokenAndApiKey() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("alice@example.com");
        req.setPassword("secret");

        CustomerDTO customer = new CustomerDTO();
        customer.setApiKey("api-key-1");
        AuthResponse resp = AuthResponse.builder()
                .token("jwt-token")
                .type("Bearer")
                .apiKey(customer.getApiKey())
                .customer(customer)
                .build();

        Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(resp);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Login successful")))
                .andExpect(jsonPath("$.data.token", is("jwt-token")))
                .andExpect(jsonPath("$.data.apiKey", is("api-key-1")));
    }

    @Test
    void register_shouldReturnCreatedCustomer() throws Exception {
        CustomerDTO payload = new CustomerDTO();
        payload.setEmail("alice@example.com");
        payload.setCompanyName("Alice Corp");
        payload.setPassword("Secret123!");

        CustomerDTO created = new CustomerDTO();
        created.setId(10L);
        created.setEmail(payload.getEmail());
        created.setCompanyName(payload.getCompanyName());
        created.setApiKey("api-key-10");

        Mockito.when(customerService.createCustomer(any(CustomerDTO.class))).thenReturn(created);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Customer registered successfully")))
                .andExpect(jsonPath("$.data.id", is(10)))
                .andExpect(jsonPath("$.data.email", is("alice@example.com")))
                .andExpect(jsonPath("$.data.apiKey", is("api-key-10")));
    }
}
