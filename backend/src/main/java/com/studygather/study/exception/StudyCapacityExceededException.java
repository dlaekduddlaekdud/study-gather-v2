package com.studygather.study.exception;

public class StudyCapacityExceededException extends RuntimeException {

    public StudyCapacityExceededException() {
        super("스터디 정원이 초과되었습니다.");
    }

    public StudyCapacityExceededException(String message) {
        super(message);
    }
}
