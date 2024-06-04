package com.api.freemarket.common.excption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;

import static com.api.freemarket.common.excption.ErrorCode.DATA_NOT_FOUND;
import static com.api.freemarket.common.excption.ErrorCode.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {NullPointerException.class})
    protected ErrorResponse nullPointerException(Exception ex) {
        log.error("nullPointerException throw Exception : {}", DATA_NOT_FOUND);
        return DATA_NOT_FOUND.convertErrorResponse(ex);
    }

    @ExceptionHandler(value = {FileNotFoundException.class})
    protected ErrorResponse fileNotFoundException(Exception ex) {
        log.error("fileNotFoundException throw Exception : {}", DATA_NOT_FOUND);
        return DATA_NOT_FOUND.convertErrorResponse(ex);
    }

    @ExceptionHandler(value = {RuntimeException.class})
    protected ErrorResponse runtimeException(Exception ex) {
        log.error("runtimeException throw Exception : {}", INTERNAL_SERVER_ERROR);
        return INTERNAL_SERVER_ERROR.convertErrorResponse(ex);
    }
}
