package com.api.freemarket.common.excption;

import com.api.freemarket.common.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static com.api.freemarket.common.excption.ErrorCode.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {NullPointerException.class})
    protected ErrorResponse nullPointerException(NullPointerException ex) {
        log.error("nullPointerException throw Exception : {}", DATA_NOT_FOUND);
        return DATA_NOT_FOUND.convertErrorResponse(ex);
    }

    @ExceptionHandler(value = {FileNotFoundException.class})
    protected ErrorResponse fileNotFoundException(FileNotFoundException ex) {
        log.error("fileNotFoundException throw Exception : {}", DATA_NOT_FOUND);
        return DATA_NOT_FOUND.convertErrorResponse(ex);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ErrorResponse runtimeException(RuntimeException ex) {
        log.error("runtimeException throw Exception : {}", INTERNAL_SERVER_ERROR);
        return INTERNAL_SERVER_ERROR.convertErrorResponse(ex);
    }

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public CommonResponse handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException throw Exception : {}", DATA_VALIDATION_FAIL);

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        return CommonResponse.ERROR(DATA_VALIDATION_FAIL.getHttpStatus(), DATA_VALIDATION_FAIL.getDetail(), fieldErrors);
    }

    @ExceptionHandler(value = {UsernameNotFoundException.class})
    public CommonResponse handleValidationExceptions(UsernameNotFoundException ex) {

        log.error("UsernameNotFoundException throw Exception : {}", MEMBER_STATUS_ERROR);

        return CommonResponse.ERROR(MEMBER_STATUS_ERROR.getHttpStatus(), ex.getMessage());
    }

    @ExceptionHandler(value = {MailSendException.class})
    public CommonResponse handleMailSendException(MailSendException ex) {

        log.error("MailSendException throw Exception : {}", MAIL_SEND_ERROR);

        return CommonResponse.ERROR(MAIL_SEND_ERROR.getHttpStatus(), ex.getMessage());
    }
}
