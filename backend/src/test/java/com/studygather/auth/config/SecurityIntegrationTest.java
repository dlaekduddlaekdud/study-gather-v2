package com.studygather.auth.config;

import com.studygather.auth.jwt.JwtTokenProvider;
import com.studygather.user.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityIntegrationTest.ProtectedTestController.class)
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void permitsHealthCheckWithoutToken() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsProtectedRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void permitsProtectedRequestWithValidToken() throws Exception {
        String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.authority").value("ROLE_USER"));
    }

    @Test
    void rejectsProtectedRequestWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void rejectsProtectedRequestWithBlankBearerToken() throws Exception {
        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void rejectsProtectedRequestWithTamperedToken() throws Exception {
        String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER);

        mockMvc.perform(get("/api/test/protected")
                        .header("Authorization", "Bearer " + tamperSignature(token)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void doesNotPersistAuthenticationBetweenRequests() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String token = jwtTokenProvider.createAccessToken(1L, UserRole.USER);

        mockMvc.perform(get("/api/test/protected")
                        .session(session)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/protected")
                        .session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    private String tamperSignature(String token) {
        int signatureStart = token.lastIndexOf('.') + 1;
        char current = token.charAt(signatureStart);
        char replacement = current == 'a' ? 'b' : 'a';

        return token.substring(0, signatureStart)
                + replacement
                + token.substring(signatureStart + 1);
    }

    @RestController
    static class ProtectedTestController {

        @GetMapping("/api/test/protected")
        public ProtectedResponse protectedEndpoint(
                Authentication authentication
        ) {
            String authority = authentication.getAuthorities().iterator().next().getAuthority();
            return new ProtectedResponse((Long) authentication.getPrincipal(), authority);
        }
    }

    record ProtectedResponse(Long userId, String authority) {
    }
}
