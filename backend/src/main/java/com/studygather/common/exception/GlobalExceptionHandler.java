package com.studygather.common.exception;

import com.studygather.application.exception.ApplicationAlreadyExistsException;
import com.studygather.application.exception.ApplicationApplicantRequiredException;
import com.studygather.application.exception.ApplicationNotFoundException;
import com.studygather.application.exception.InvalidApplicationStatusException;
import com.studygather.application.exception.StudyOwnerCannotApplyException;
import com.studygather.auth.exception.InvalidCredentialsException;
import com.studygather.common.api.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> handleApplicationNotFound(
            ApplicationNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(ApplicationApplicantRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationApplicantRequired(
            ApplicationApplicantRequiredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(InvalidApplicationStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidApplicationStatus(
            InvalidApplicationStatusException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(ApplicationAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleApplicationAlreadyExists(
            ApplicationAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(StudyOwnerCannotApplyException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudyOwnerCannotApply(
            StudyOwnerCannotApplyException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(StudyCapacityExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudyCapacityExceeded(
            StudyCapacityExceededException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(StudyOwnerRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudyOwnerRequired(
            StudyOwnerRequiredException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(StudyClosedException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudyClosed(
            StudyClosedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(StudyNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleStudyNotFound(
            StudyNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(
            UserNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(
            DuplicateEmailException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
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
                .body(ApiResponse.error(message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound() {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("요청한 경로를 찾을 수 없습니다."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported() {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("허용되지 않은 요청 방식입니다."));
    }
}
