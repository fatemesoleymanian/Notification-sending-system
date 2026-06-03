package com.neon.Notification.sending.system.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.Notification.sending.system.notification.repository.NotificationRepository;
import com.neon.Notification.sending.system.support.TestUsers;
import com.neon.Notification.sending.system.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void registerShouldReturnJwtAndPersistUserAndWelcomeNotification() throws Exception {
        String username = "alice-register";
        String email = "alice-register@example.com";
        String password = TestUsers.PASSWORD;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"%s"}
                                """.formatted(username, email, password)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.username").value(username));

        var user = appUserRepository.findByUsername(username).orElseThrow();
        var history = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(history).hasSize(1);
        assertThat(history.get(0).getTitle()).isEqualTo("Welcome");
        assertThat(history.get(0).getMessage()).isEqualTo("Your account has been created successfully.");
    }

    @Test
    void loginShouldReturnJwtAndPersistLoginNotification() throws Exception {
        String username = "alice-login";
        String email = "alice-login@example.com";
        String password = TestUsers.PASSWORD;

        registerUser(username, email, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body.get("tokenType").asText()).isEqualTo("Bearer");

        var user = appUserRepository.findByUsername(username).orElseThrow();
        var history = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getTitle()).isEqualTo("Login successful");
        assertThat(history.get(1).getTitle()).isEqualTo("Welcome");
    }

    private void registerUser(String username, String email, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"username":"%s","email":"%s","password":"%s"}
                        """.formatted(username, email, password)))
                .andExpect(status().isCreated());
    }
}
