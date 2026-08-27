package com.studygather.application.controller;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.dto.response.ApplicationListResponse;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.service.StudyApplicationService;
import com.studygather.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/studies/{studyId}/applications")
public class StudyApplicationController {

    private final StudyApplicationService applicationService;

    public StudyApplicationController(StudyApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationListResponse>>> getStudyApplications(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long studyId
    ) {
        List<ApplicationListResponse> response = applicationService.getStudyApplications(
                userId,
                studyId
        );

        return ResponseEntity.ok(ApiResponse.success("스터디 참여 신청 목록을 조회했습니다.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long studyId,
            @Valid @RequestBody CreateApplicationRequest request
    ) {
        ApplicationResponse response = applicationService.createApplication(
                userId,
                studyId,
                request
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("스터디 참여 신청이 완료되었습니다.", response));
    }
}
