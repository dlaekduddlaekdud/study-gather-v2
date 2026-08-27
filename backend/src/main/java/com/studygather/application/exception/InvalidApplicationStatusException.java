package com.studygather.application.exception;

public class InvalidApplicationStatusException extends RuntimeException {

    public InvalidApplicationStatusException() {
        super("현재 상태에서는 참여 신청을 변경할 수 없습니다.");
    }
}
