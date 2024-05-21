package com.api.freemarket.common.excption;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.FileNotFoundException;

import static com.api.freemarket.common.excption.ErrorCode.*;

@ControllerAdvice
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
}
