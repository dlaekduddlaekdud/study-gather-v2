package com.studygather.study.exception;

public class StudyNotFoundException extends RuntimeException {

    public StudyNotFoundException() {
        super("스터디를 찾을 수 없습니다.");
    }
}
