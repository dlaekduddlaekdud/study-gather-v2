package com.studygather.study.controller;

import com.studygather.common.api.ApiResponse;
import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.dto.response.StudySummaryResponse;
import com.studygather.study.service.StudyService;
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
@RequestMapping("/api/studies")
public class StudyController {

    private final StudyService studyService;

    public StudyController(StudyService studyService) {
        this.studyService = studyService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudySummaryResponse>>> getOpenStudies() {
        List<StudySummaryResponse> response = studyService.getOpenStudies();

        return ResponseEntity.ok(ApiResponse.success("스터디 목록을 조회했습니다.", response));
    }

    @GetMapping("/{studyId}")
    public ResponseEntity<ApiResponse<StudyResponse>> getStudy(
            @PathVariable Long studyId
    ) {
        StudyResponse response = studyService.getStudy(studyId);

        return ResponseEntity.ok(ApiResponse.success("스터디를 조회했습니다.", response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudyResponse>> createStudy(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateStudyRequest request
    ) {
        StudyResponse response = studyService.createStudy(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("스터디가 생성되었습니다.", response));
    }
}
