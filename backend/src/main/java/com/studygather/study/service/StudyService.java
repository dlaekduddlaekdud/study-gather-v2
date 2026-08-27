package com.studygather.study.service;

import com.studygather.study.dto.request.CreateStudyRequest;
import com.studygather.study.dto.request.UpdateStudyRequest;
import com.studygather.study.dto.response.StudyResponse;
import com.studygather.study.dto.response.StudyMemberResponse;
import com.studygather.study.dto.response.StudySummaryResponse;
import com.studygather.study.entity.Study;
import com.studygather.study.entity.StudyMember;
import com.studygather.study.entity.StudyStatus;
import com.studygather.study.exception.StudyNotFoundException;
import com.studygather.study.exception.StudyOwnerRequiredException;
import com.studygather.study.repository.StudyMemberRepository;
import com.studygather.study.repository.StudyRepository;
import com.studygather.user.entity.User;
import com.studygather.user.exception.UserNotFoundException;
import com.studygather.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;

    public StudyService(
            StudyRepository studyRepository,
            StudyMemberRepository studyMemberRepository,
            UserRepository userRepository
    ) {
        this.studyRepository = studyRepository;
        this.studyMemberRepository = studyMemberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StudyResponse createStudy(Long ownerId, CreateStudyRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(UserNotFoundException::new);
        Study study = Study.create(
                owner,
                request.title(),
                request.description(),
                request.capacity(),
                request.recruitmentDeadline()
        );
        Study savedStudy = studyRepository.save(study);

        // 스터디와 OWNER 멤버를 같은 트랜잭션에 저장해 승인 인원과 멤버 수를 일치시킨다.
        StudyMember ownerMember = StudyMember.createOwner(savedStudy, owner);
        studyMemberRepository.save(ownerMember);

        return StudyResponse.from(savedStudy);
    }

    @Transactional(readOnly = true)
    public List<StudySummaryResponse> getOpenStudies() {
        return studyRepository
                .findAllByStatusAndRecruitmentDeadlineAfterOrderByRecruitmentDeadlineAsc(
                        StudyStatus.OPEN,
                        LocalDateTime.now()
                )
                .stream()
                .map(StudySummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public StudyResponse getStudy(Long studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(StudyNotFoundException::new);

        return StudyResponse.from(study);
    }

    @Transactional(readOnly = true)
    public List<StudyMemberResponse> getStudyMembers(Long userId, Long studyId) {
        Study study = getStudyEntity(studyId);
        validateOwner(study, userId);

        return studyMemberRepository
                .findAllByStudyIdOrderByJoinedAtAscIdAsc(studyId)
                .stream()
                .map(StudyMemberResponse::from)
                .toList();
    }

    @Transactional
    public StudyResponse updateStudy(
            Long userId,
            Long studyId,
            UpdateStudyRequest request
    ) {
        Study study = getStudyEntity(studyId);
        validateOwner(study, userId);
        study.update(
                request.title(),
                request.description(),
                request.capacity(),
                request.recruitmentDeadline()
        );

        return StudyResponse.from(study);
    }

    @Transactional
    public StudyResponse closeStudy(Long userId, Long studyId) {
        Study study = getStudyEntity(studyId);
        validateOwner(study, userId);
        study.close();

        return StudyResponse.from(study);
    }

    private Study getStudyEntity(Long studyId) {
        return studyRepository.findById(studyId)
                .orElseThrow(StudyNotFoundException::new);
    }

    private void validateOwner(Study study, Long userId) {
        if (!study.isOwnedBy(userId)) {
            throw new StudyOwnerRequiredException();
        }
    }
}
