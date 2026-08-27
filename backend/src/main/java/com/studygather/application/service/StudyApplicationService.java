package com.studygather.application.service;

import com.studygather.application.dto.request.CreateApplicationRequest;
import com.studygather.application.dto.response.ApplicationListResponse;
import com.studygather.application.dto.response.ApplicationResponse;
import com.studygather.application.entity.StudyApplication;
import com.studygather.application.exception.ApplicationAlreadyExistsException;
import com.studygather.application.exception.ApplicationApplicantRequiredException;
import com.studygather.application.exception.ApplicationNotFoundException;
import com.studygather.application.exception.StudyOwnerCannotApplyException;
import com.studygather.application.repository.StudyApplicationRepository;
import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.exception.StudyClosedException;
import com.studygather.study.exception.StudyNotFoundException;
import com.studygather.study.exception.StudyOwnerRequiredException;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.study.repository.StudyRepository;
import com.studygather.user.entity.User;
import com.studygather.user.exception.UserNotFoundException;
import com.studygather.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyApplicationService {

    private final StudyApplicationRepository applicationRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;

    public StudyApplicationService(
            StudyApplicationRepository applicationRepository,
            StudyRepository studyRepository,
            StudyMemberRepository studyMemberRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ApplicationResponse createApplication(
            Long applicantId,
            Long studyId,
            CreateApplicationRequest request
    ) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(StudyNotFoundException::new);

        if (!study.isRecruitingAt(LocalDateTime.now())) {
            throw new StudyClosedException();
        }
        if (study.isOwnedBy(applicantId)) {
            throw new StudyOwnerCannotApplyException();
        }
        if (applicationRepository.existsByStudyIdAndApplicantId(studyId, applicantId)
                || studyMemberRepository.existsByStudyIdAndUserId(studyId, applicantId)) {
            throw new ApplicationAlreadyExistsException();
        }

        User applicant = userRepository.findById(applicantId)
                .orElseThrow(UserNotFoundException::new);
        StudyApplication application = StudyApplication.create(
                study,
                applicant,
                request.message()
        );

        try {
            StudyApplication savedApplication = applicationRepository.saveAndFlush(application);
            return ApplicationResponse.from(savedApplication);
        } catch (DataIntegrityViolationException exception) {
            // 사전 조회 사이에 들어온 동시 요청은 DB UNIQUE 제약으로 최종 차단한다.
            throw new ApplicationAlreadyExistsException();
        }
    }

    @Transactional(readOnly = true)
    public List<ApplicationListResponse> getStudyApplications(
            Long userId,
            Long studyId
    ) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(StudyNotFoundException::new);
        validateStudyOwner(study, userId);

        return applicationRepository.findAllByStudyIdOrderByCreatedAtAsc(studyId)
                .stream()
                .map(ApplicationListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationListResponse> getMyApplications(Long applicantId) {
        return applicationRepository.findAllByApplicantIdOrderByCreatedAtDesc(applicantId)
                .stream()
                .map(ApplicationListResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationResponse cancelApplication(Long userId, Long applicationId) {
        StudyApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);

        if (!application.isAppliedBy(userId)) {
            throw new ApplicationApplicantRequiredException();
        }

        application.cancel();
        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse approveApplication(Long userId, Long applicationId) {
        StudyApplication application = findApplicationForUpdate(applicationId);
        application.validatePending();

        // 잠금 전에 Study를 조회하면 영속성 컨텍스트에 오래된 정원 값이 남을 수 있다.
        Study lockedStudy = studyRepository.findByIdForUpdate(application.getStudy().getId())
                .orElseThrow(StudyNotFoundException::new);

        validateStudyOwner(lockedStudy, userId);
        lockedStudy.validateCapacityAvailable();
        application.approve();
        StudyMember member = StudyMember.createMember(lockedStudy, application.getApplicant());
        studyMemberRepository.saveAndFlush(member);
        lockedStudy.increaseApprovedCount();

        return ApplicationResponse.from(application);
    }

    @Transactional
    public ApplicationResponse rejectApplication(Long userId, Long applicationId) {
        StudyApplication application = findApplicationForUpdate(applicationId);
        application.validatePending();
        validateStudyOwner(application.getStudy(), userId);

        application.reject();
        return ApplicationResponse.from(application);
    }

    private StudyApplication findApplicationForUpdate(Long applicationId) {
        return applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(ApplicationNotFoundException::new);
    }

    private void validateStudyOwner(Study study, Long userId) {
        if (!study.isOwnedBy(userId)) {
            throw new StudyOwnerRequiredException();
        }
    }
}
