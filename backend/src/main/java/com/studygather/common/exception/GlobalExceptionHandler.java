package com.studygather.common.exception;

import com.studygather.application.exception.ApplicationAlreadyExistsException;
import com.studygather.application.exception.ApplicationApplicantRequiredException;
import com.studygather.application.exception.ApplicationNotFoundException;
import com.studygather.application.exception.InvalidApplicationStatusException;
import com.studygather.application.exception.StudyOwnerCannotApplyException;
import com.studygather.auth.exception.InvalidCredentialsException;
import com.studygather.common.api.ErrorResponse;
import com.studygather.study.exception.StudyCapacityExceededException;
import com.studygather.study.exception.StudyClosedException;
import com.studygather.study.exception.StudyNotFoundException;
import com.studygather.study.exception.StudyOwnerRequiredException;
import com.studygather.user.exception.DuplicateEmailException;
import com.studygather.user.exception.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApplicationNotFound(
            ApplicationNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(ApplicationApplicantRequiredException.class)
    public ResponseEntity<ErrorResponse> handleApplicationApplicantRequired(
            ApplicationApplicantRequiredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(InvalidApplicationStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApplicationStatus(
            InvalidApplicationStatusException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(ApplicationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleApplicationAlreadyExists(
            ApplicationAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(StudyOwnerCannotApplyException.class)
    public ResponseEntity<ErrorResponse> handleStudyOwnerCannotApply(
            StudyOwnerCannotApplyException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(StudyCapacityExceededException.class)
    public ResponseEntity<ErrorResponse> handleStudyCapacityExceeded(
            StudyCapacityExceededException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(StudyOwnerRequiredException.class)
    public ResponseEntity<ErrorResponse> handleStudyOwnerRequired(
            StudyOwnerRequiredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(StudyClosedException.class)
    public ResponseEntity<ErrorResponse> handleStudyClosed(
            StudyClosedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(StudyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStudyNotFound(
            StudyNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(
            UserNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("요청 값이 올바르지 않습니다.");

        return ResponseEntity
                .badRequest()
                .body(ErrorResponse.from(message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.from("요청한 경로를 찾을 수 없습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ErrorResponse.from("허용되지 않은 요청 방식입니다."));
    }
}
