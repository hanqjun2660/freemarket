package com.api.freemarket.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;

@Schema(description = "공통 응답 객체")
@Getter
public class CommonResponse<T> {

    private String statusCode;
    private HttpStatus httpStatus;
    private String message;
    private T data;

    private CommonResponse(CommonResponseCode code, String message, T data) {
        this.statusCode = code.getCode();
        this.message = ObjectUtils.isEmpty(message) ? code.getDefaultMessage() : message;
        this.data = data;
    }

    private CommonResponse(HttpStatus httpStatus, String message, T data) {
        this.statusCode = String.valueOf(httpStatus.value());
        this.httpStatus = httpStatus;
        this.message = message;
        this.data = data;
    }

    public static <T> CommonResponse<T> OK(String message, T data) {
        return new CommonResponse<T>(CommonResponseCode.OK, message, data);
    }

    public static <T> CommonResponse<T> OK(CommonResponseCode statusCode, String message, T data) {
        return new CommonResponse<T>(statusCode, message, data);
    }

    public static <T> CommonResponse<T> ERROR(String message, T data) {
        return new CommonResponse<T>(CommonResponseCode.ERROR, message, data);
    }

    public static <T> CommonResponse<T> ERROR(HttpStatus statusCode, String message, T data) {
        return new CommonResponse<T>(statusCode, message, data);
    }

    /*------------- OVER LOADING START -------------*/
    public static <T> CommonResponse<T> OK(T data) {
        return OK(null, data);
    }

    public static CommonResponse OK(String message) {
        return OK(message, null);
    }

    public static CommonResponse OK(CommonResponseCode statusCode, String message) {
        return OK(statusCode, message, null);
    }

    public static CommonResponse ERROR(String message) {
        return ERROR(message, null);
    }

    public static <T> CommonResponse<T> ERROR(T data) {
        return ERROR(null, data);
    }

    public static CommonResponse ERROR(HttpStatus httpStatus, String message) {
        return new CommonResponse(httpStatus, message, null);
    }
    /*------------- OVER LOADING END -------------*/
}
