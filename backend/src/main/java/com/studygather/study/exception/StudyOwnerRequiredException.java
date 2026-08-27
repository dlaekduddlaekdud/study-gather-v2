package com.studygather.study.exception;

public class StudyOwnerRequiredException extends RuntimeException {

    public StudyOwnerRequiredException() {
        super("스터디 개설자만 수행할 수 있습니다.");
    }
}
