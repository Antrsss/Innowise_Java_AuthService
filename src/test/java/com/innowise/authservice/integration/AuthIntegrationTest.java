package com.innowise.authservice.integration;

import com.innowise.authservice.dto.AuthRequest;
import com.innowise.authservice.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

  private MockMvc mockMvc;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
  }

  @Test
  void fullAuthCycle_IntegrationTest() throws Exception {
    AuthRequest authRequest = new AuthRequest("integration_user@example.com", "password123", Role.ROLE_USER);

    mockMvc.perform(post("/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isCreated());

    MvcResult result = mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(authRequest)))
        .andExpect(status().isOk())
        .andReturn();

    String responseBody = result.getResponse().getContentAsString();

    String accessToken = objectMapper.readTree(responseBody).get("accessToken").toString();

    accessToken = accessToken.replace("\"", "");

    mockMvc.perform(get("/auth/validate")
            .param("token", accessToken))
        .andExpect(status().isOk());
  }
}