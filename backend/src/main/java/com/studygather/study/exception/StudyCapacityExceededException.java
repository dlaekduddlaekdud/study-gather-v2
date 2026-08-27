package com.studygather.study.exception;

public class StudyCapacityExceededException extends RuntimeException {

    public StudyCapacityExceededException() {
        super("현재 승인 인원보다 정원을 작게 설정할 수 없습니다.");
    }
}
