package com.studygather.application.exception;

public class ApplicationAlreadyExistsException extends RuntimeException {

    public ApplicationAlreadyExistsException() {
        super("이미 해당 스터디에 신청했거나 참여 중입니다.");
    }
}
