package com.studygather.application.controller;

import com.studygather.auth.jwt.JwtTokenProvider;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.service.StudyService;
import com.studygather.user.dto.request.SignUpRequest;
import com.studygather.user.dto.response.SignUpResponse;
import com.studygather.user.entity.UserRole;
import com.studygather.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyApplicationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private StudyService studyService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Test
    void createsApplicationWithLoginToken() throws Exception {
        SignUpResponse owner = createUser("application-api-owner@example.com", "application-owner");
        SignUpResponse applicant = createUser(
                "application-api-applicant@example.com",
                "application-applicant"
        );
        StudyResponse study = createStudy(owner, "참여 신청 API 스터디");
        String accessToken = jwtTokenProvider.createAccessToken(applicant.id(), UserRole.USER);

        mockMvc.perform(post("/api/studies/{studyId}/applications", study.id())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "성실하게 참여하겠습니다."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("스터디 참여 신청이 완료되었습니다."))
                .andExpect(jsonPath("$.data.studyId").value(study.id()))
                .andExpect(jsonPath("$.data.applicantId").value(applicant.id()))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void rejectsApplicationWithoutToken() throws Exception {
        SignUpResponse owner = createUser("no-token-owner@example.com", "no-token-owner");
        StudyResponse study = createStudy(owner, "미인증 신청 테스트");

        mockMvc.perform(post("/api/studies/{studyId}/applications", study.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "토큰 없는 신청"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void rejectsApplicationFromStudyOwner() throws Exception {
        SignUpResponse owner = createUser("self-application-owner@example.com", "self-owner");
        StudyResponse study = createStudy(owner, "자기 스터디 신청 방지");
        String accessToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        mockMvc.perform(post("/api/studies/{studyId}/applications", study.id())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "자기 스터디 신청"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("개설자는 자신의 스터디에 참여 신청할 수 없습니다."));
    }

    @Test
    void rejectsDuplicateApplication() throws Exception {
        SignUpResponse owner = createUser("duplicate-api-owner@example.com", "duplicate-owner");
        SignUpResponse applicant = createUser(
                "duplicate-api-applicant@example.com",
                "duplicate-applicant"
        );
        StudyResponse study = createStudy(owner, "중복 신청 API 테스트");
        String accessToken = jwtTokenProvider.createAccessToken(applicant.id(), UserRole.USER);
        String requestBody = """
                {
                  "message": "중복 신청입니다."
                }
                """;
        mockMvc.perform(post("/api/studies/{studyId}/applications", study.id())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/studies/{studyId}/applications", study.id())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("이미 해당 스터디에 신청했거나 참여 중입니다."));
    }

    private SignUpResponse createUser(String email, String nickname) {
        return userService.signUp(new SignUpRequest(email, "password123", nickname));
    }

    private StudyResponse createStudy(SignUpResponse owner, String title) {
        return studyService.createStudy(owner.id(), new CreateStudyRequest(
                title,
                "Controller 테스트용 스터디입니다.",
                5,
                LocalDateTime.now().plusDays(7).withNano(0)
        ));
    }
}
