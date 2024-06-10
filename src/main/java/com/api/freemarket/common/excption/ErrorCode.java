package com.api.freemarket.common.excption;

import com.api.freemarket.common.CommonResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    /* 400 BAD_REQUEST : 잘못된 요청 */
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰이 유효하지 않습니다"),
    MISMATCH_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "리프레시 토큰의 유저 정보가 일치하지 않습니다"),

    /* 401 UNAUTHORIZED : 인증되지 않은 사용자 */
    INVALID_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "권한 정보가 없는 토큰입니다"),
    MEMBER_STATUS_ERROR(HttpStatus.UNAUTHORIZED, ""),

    /* 404 NOT_FOUND : Resource 를 찾을 수 없음 */
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저 정보를 찾을 수 없습니다"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "로그아웃 된 사용자입니다"),
    DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."),
    DATA_VALIDATION_FAIL(HttpStatus.NOT_FOUND, "데이터 유효성 검사를 실패했습니다."),

    /* 408 REQUEST_TIMEOUT : 클라이언트 요청이 서버에 도달했으나 서버 처리시간이 충분하지 않은 경우 */
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "요청 시간 초과입니다"),

    /* 409 CONFLICT : Resource 의 현재 상태와 충돌. 보통 중복된 데이터 존재 */
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "데이터가 이미 존재합니다"),

    /* 500 INTERNAL_SERVER_ERROR : 서버 내부 오류 (DB연결 실패, 응답 실패 등) */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다"),

    /* 502 BAD_GATEWAY : 게이트웨이나 프록시 서버 역할을 하는 서버가 상류 서버로부터 잘못된 응답 */
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "잘못된 게이트웨이 응답입니다"),

    /* 503 SERVICE_UNAVAILABLE : 서버 과부화 등 일시적 서비스 제공 불가 상태 */
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "서비스를 사용할 수 없습니다")
    ;

    private final HttpStatus httpStatus;
    private final String detail;

    public ErrorResponse convertErrorResponse(Exception ex) {
        return ErrorResponse.create(ex,httpStatus,detail);
    }
}