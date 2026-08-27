package com.studygather.study.exception;

public class StudyClosedException extends RuntimeException {

    public StudyClosedException() {
        super("모집이 마감된 스터디입니다.");
    }
}
