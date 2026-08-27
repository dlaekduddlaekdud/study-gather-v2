package com.studygather.application.exception;

public class ApplicationApplicantRequiredException extends RuntimeException {

    public ApplicationApplicantRequiredException() {
        super("신청자 본인만 수행할 수 있습니다.");
    }
}
