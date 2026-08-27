package com.studygather.application.exception;

public class StudyOwnerCannotApplyException extends RuntimeException {

    public StudyOwnerCannotApplyException() {
        super("개설자는 자신의 스터디에 참여 신청할 수 없습니다.");
    }
}
