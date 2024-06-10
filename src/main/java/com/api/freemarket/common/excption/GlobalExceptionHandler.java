package com.api.freemarket.common.excption;

import com.api.freemarket.common.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

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

//        return CommonResponse.ERROR((String)HttpStatus.NOT_FOUND, "데이터 유효성 검사를 실패했습니다.", fieldErrors);
        return CommonResponse.ERROR(DATA_VALIDATION_FAIL.getHttpStatus(), DATA_VALIDATION_FAIL.getDetail(), fieldErrors);
    }
}
