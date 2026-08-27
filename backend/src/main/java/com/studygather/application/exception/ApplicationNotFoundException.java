package com.studygather.application.exception;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException() {
        super("참여 신청을 찾을 수 없습니다.");
    }
}
