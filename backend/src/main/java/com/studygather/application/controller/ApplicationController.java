package com.studygather.application.controller;

import com.studygather.application.dto.response.ApplicationListResponse;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.service.StudyApplicationService;
import com.studygather.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final StudyApplicationService applicationService;

    public ApplicationController(StudyApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ApplicationListResponse>>> getMyApplications(
            @AuthenticationPrincipal Long userId
    ) {
        List<ApplicationListResponse> response = applicationService.getMyApplications(userId);

        return ResponseEntity.ok(ApiResponse.success("내 참여 신청 목록을 조회했습니다.", response));
    }

    @PostMapping("/{applicationId}/cancel")
    public ResponseEntity<ApiResponse<ApplicationResponse>> cancelApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long applicationId
    ) {
        ApplicationResponse response = applicationService.cancelApplication(
                userId,
                applicationId
        );

        return ResponseEntity.ok(ApiResponse.success("참여 신청을 취소했습니다.", response));
    }

    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<ApiResponse<ApplicationResponse>> approveApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long applicationId
    ) {
        ApplicationResponse response = applicationService.approveApplication(
                userId,
                applicationId
        );

        return ResponseEntity.ok(ApiResponse.success("참여 신청을 승인했습니다.", response));
    }

    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<ApiResponse<ApplicationResponse>> rejectApplication(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long applicationId
    ) {
        ApplicationResponse response = applicationService.rejectApplication(
                userId,
                applicationId
        );

        return ResponseEntity.ok(ApiResponse.success("참여 신청을 거절했습니다.", response));
    }
}
