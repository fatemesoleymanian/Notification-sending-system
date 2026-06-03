package com.neon.Notification.sending.system.notification.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neon.Notification.sending.system.notification.repository.NotificationRepository;
import com.neon.Notification.sending.system.support.TestNotifications;
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
class NotificationControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void userShouldSendNotificationAndStoreHistory() throws Exception {
        String username = "alice-notify";
        String email = "alice-notify@example.com";
        String token = registerAndLogin(username, email, TestUsers.PASSWORD);

        mockMvc.perform(post("/notifications")
                        .header(AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"channel":"EMAIL","title":"Order created","message":"Your order has been created successfully."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.channel").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.username").value(username));

        var user = appUserRepository.findByUsername(username).orElseThrow();
        var history = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(history).hasSize(3);
        assertThat(history.get(0).getTitle()).isEqualTo("Order created");
        assertThat(history.get(1).getTitle()).isEqualTo("Login successful");
        assertThat(history.get(2).getTitle()).isEqualTo("Welcome");

        mockMvc.perform(get("/notifications/me")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void userShouldGetForbiddenJsonWhenCallingAdminEndpoint() throws Exception {
        String username = "alice-forbidden";
        String email = "alice-forbidden@example.com";
        String token = registerAndLogin(username, email, TestUsers.PASSWORD);

        mockMvc.perform(get("/notifications/admin/all")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("You do not have access to this resource."));
    }

    @Test
    void adminShouldAccessAdminEndpoints() throws Exception {
        String token = loginAsAdmin();

        mockMvc.perform(get("/notifications/admin/all")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void protectedEndpointShouldReturnJsonUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/notifications/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication is required to access this resource."));
    }

    private String registerAndLogin(String username, String email, String password) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","email":"%s","password":"%s"}
                                """.formatted(username, email, password)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn();

        return tokenFrom(result);
    }

    private String loginAsAdmin() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"%s"}
                                """.formatted(TestUsers.ADMIN_USERNAME, TestUsers.ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn();

        return tokenFrom(result);
    }

    private String tokenFrom(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }
}
