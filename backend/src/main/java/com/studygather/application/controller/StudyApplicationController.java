package com.studygather.application.controller;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.service.StudyApplicationService;
import com.studygather.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/studies/{studyId}/applications")
public class StudyApplicationController {

    private final StudyApplicationService applicationService;

    public StudyApplicationController(StudyApplicationService applicationService) {
        this.applicationService = applicationService;
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
