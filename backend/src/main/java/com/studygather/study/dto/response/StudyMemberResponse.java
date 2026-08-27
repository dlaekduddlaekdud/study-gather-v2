package com.studygather.study.dto.response;

import com.studygather.study.entity.StudyMember;
import com.studygather.study.entity.StudyMemberRole;

import java.time.LocalDateTime;

public record StudyMemberResponse(
        Long memberId,
        Long userId,
        String nickname,
        StudyMemberRole memberRole,
        LocalDateTime joinedAt
) {

    public static StudyMemberResponse from(StudyMember member) {
        return new StudyMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getMemberRole(),
                member.getJoinedAt()
        );
    }
}
