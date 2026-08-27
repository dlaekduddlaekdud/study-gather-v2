package com.studygather.study.controller;

import com.jayway.jsonpath.JsonPath;
import com.studygather.auth.jwt.JwtTokenProvider;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.entity.StudyMemberRole;
import com.studygather.study.repository.StudyMemberRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class StudyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private StudyMemberRepository studyMemberRepository;

    @Autowired
    private StudyService studyService;

    @Test
    void createsStudyWithLoginToken() throws Exception {
        SignUpResponse owner = createUser("study-api-owner@example.com", "study-api-owner");
        String accessToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        MvcResult result = mockMvc.perform(post("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("스터디가 생성되었습니다."))
                .andExpect(jsonPath("$.data.ownerId").value(owner.id()))
                .andExpect(jsonPath("$.data.capacity").value(5))
                .andExpect(jsonPath("$.data.approvedCount").value(1))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andReturn();

        Number studyId = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.id"
        );
        StudyMemberRole memberRole = studyMemberRepository
                .findByStudyIdAndUserId(studyId.longValue(), owner.id())
                .orElseThrow()
                .getMemberRole();

        assertEquals(StudyMemberRole.OWNER, memberRole);
    }

    @Test
    void rejectsStudyCreationWithoutToken() throws Exception {
        mockMvc.perform(post("/api/studies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestBody()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    @Test
    void rejectsStudyCreationBelowMinimumCapacity() throws Exception {
        SignUpResponse owner = createUser("invalid-study-owner@example.com", "invalid-owner");
        String accessToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        mockMvc.perform(post("/api/studies")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "잘못된 정원 스터디",
                                  "description": "정원이 한 명뿐인 요청입니다.",
                                  "capacity": 1,
                                  "recruitmentDeadline": "2099-12-31T23:59:59"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("정원은 2명 이상이어야 합니다."));
    }

    @Test
    void getsOpenStudiesWithoutToken() throws Exception {
        SignUpResponse owner = createUser("public-list-owner@example.com", "public-list-owner");
        studyService.createStudy(owner.id(), new CreateStudyRequest(
                "공개 목록 스터디",
                "토큰 없이 조회할 수 있습니다.",
                5,
                java.time.LocalDateTime.now().plusDays(7).withNano(0)
        ));

        mockMvc.perform(get("/api/studies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath(
                        "$.data[?(@.title == '공개 목록 스터디')]"
                ).isNotEmpty());
    }

    @Test
    void getsStudyDetailWithoutToken() throws Exception {
        SignUpResponse owner = createUser("public-detail-owner@example.com", "public-detail-owner");
        Long studyId = studyService.createStudy(owner.id(), new CreateStudyRequest(
                "공개 상세 스터디",
                "상세 조회 설명입니다.",
                4,
                java.time.LocalDateTime.now().plusDays(7).withNano(0)
        )).id();

        mockMvc.perform(get("/api/studies/{studyId}", studyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(studyId))
                .andExpect(jsonPath("$.data.description").value("상세 조회 설명입니다."));
    }

    @Test
    void returnsNotFoundForUnknownStudy() throws Exception {
        mockMvc.perform(get("/api/studies/{studyId}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("스터디를 찾을 수 없습니다."));
    }

    @Test
    void updatesStudyAsOwner() throws Exception {
        SignUpResponse owner = createUser("update-api-owner@example.com", "update-api-owner");
        Long studyId = createStudy(owner, "수정 전 스터디");
        String accessToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        mockMvc.perform(patch("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "수정된 스터디",
                                  "capacity": 7
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("수정된 스터디"))
                .andExpect(jsonPath("$.data.capacity").value(7));
    }

    @Test
    void rejectsStudyUpdateByNonOwner() throws Exception {
        SignUpResponse owner = createUser("update-owner@example.com", "update-owner");
        SignUpResponse otherUser = createUser("update-other@example.com", "update-other");
        Long studyId = createStudy(owner, "수정 권한 테스트");
        String accessToken = jwtTokenProvider.createAccessToken(otherUser.id(), UserRole.USER);

        mockMvc.perform(patch("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "타인의 수정"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("스터디 개설자만 수행할 수 있습니다."));
    }

    @Test
    void rejectsEmptyStudyUpdate() throws Exception {
        SignUpResponse owner = createUser("empty-update-owner@example.com", "empty-update-owner");
        Long studyId = createStudy(owner, "빈 수정 테스트");
        String accessToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        mockMvc.perform(patch("/api/studies/{studyId}", studyId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("수정할 내용을 하나 이상 입력해야 합니다."));
    }

    @Test
    void closesStudyAsOwner() throws Exception {
        SignUpResponse owner = createUser("close-api-owner@example.com", "close-api-owner");
        Long studyId = createStudy(owner, "마감 API 테스트");
        String accessToken = jwtTokenProvider.createAccessToken(owner.id(), UserRole.USER);

        mockMvc.perform(post("/api/studies/{studyId}/close", studyId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스터디 모집을 마감했습니다."))
                .andExpect(jsonPath("$.data.status").value("CLOSED"));
    }

    @Test
    void rejectsStudyCloseWithoutToken() throws Exception {
        SignUpResponse owner = createUser("close-no-token-owner@example.com", "close-no-token-owner");
        Long studyId = createStudy(owner, "미인증 마감 테스트");

        mockMvc.perform(post("/api/studies/{studyId}/close", studyId))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."));
    }

    private SignUpResponse createUser(String email, String nickname) {
        return userService.signUp(new SignUpRequest(
                email,
                "password123",
                nickname
        ));
    }

    private Long createStudy(SignUpResponse owner, String title) {
        return studyService.createStudy(owner.id(), new CreateStudyRequest(
                title,
                "스터디 설명입니다.",
                5,
                java.time.LocalDateTime.now().plusDays(7).withNano(0)
        )).id();
    }

    private String validRequestBody() {
        return """
                {
                  "title": "Spring Boot 스터디",
                  "description": "함께 Spring Boot를 공부합니다.",
                  "capacity": 5,
                  "recruitmentDeadline": "2099-12-31T23:59:59"
                }
                """;
    }
}
