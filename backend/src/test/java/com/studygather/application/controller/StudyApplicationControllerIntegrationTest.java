package com.studygather.application.controller;

import com.jayway.jsonpath.JsonPath;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    void ownerGetsStudyApplications() throws Exception {
        SignUpResponse owner = createUser("owner-list-api@example.com", "owner-list-api");
        SignUpResponse applicant = createUser(
                "owner-list-applicant@example.com",
                "owner-list-applicant"
        );
        StudyResponse study = createStudy(owner, "개설자 신청 목록 테스트");
        createApplication(study.id(), applicant);
        String ownerToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        mockMvc.perform(get("/api/studies/{studyId}/applications", study.id())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].applicantId").value(applicant.id()))
                .andExpect(jsonPath("$.data[0].applicantNickname")
                        .value(applicant.nickname()));
    }

    @Test
    void applicantGetsAndCancelsOwnApplication() throws Exception {
        SignUpResponse owner = createUser("cancel-api-owner@example.com", "cancel-api-owner");
        SignUpResponse applicant = createUser(
                "cancel-api-applicant@example.com",
                "cancel-api-applicant"
        );
        StudyResponse study = createStudy(owner, "신청 취소 API 테스트");
        Long applicationId = createApplication(study.id(), applicant);
        String applicantToken = jwtTokenProvider.createAccessToken(applicant.id(), UserRole.USER);

        mockMvc.perform(get("/api/applications/me")
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(applicationId))
                .andExpect(jsonPath("$.data[0].studyTitle").value("신청 취소 API 테스트"));

        mockMvc.perform(post("/api/applications/{applicationId}/cancel", applicationId)
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("CANCELED"));

        mockMvc.perform(post("/api/applications/{applicationId}/cancel", applicationId)
                        .header("Authorization", "Bearer " + applicantToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("현재 상태에서는 참여 신청을 변경할 수 없습니다."));
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

    private Long createApplication(Long studyId, SignUpResponse applicant) throws Exception {
        String applicantToken = jwtTokenProvider.createAccessToken(applicant.id(), UserRole.USER);
        MvcResult result = mockMvc.perform(post("/api/studies/{studyId}/applications", studyId)
                        .header("Authorization", "Bearer " + applicantToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "목록 및 취소 테스트 신청입니다."
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        Number applicationId = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.id"
        );
        return applicationId.longValue();
    }
}
