package pookyBlog.web.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pookyBlog.common.Dto.Response.ErrorResponse;

@RestControllerAdvice
public class DownstreamExceptionHandler {

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleDownstreamError(WebClientResponseException exception) {
        HttpStatusCode status = exception.getStatusCode();
        ErrorResponse response = ErrorResponse.builder()
                .code(Integer.toString(status.value()))
                .message(status.value() == 403
                        ? "요청을 수행할 권한이 없습니다."
                        : "내부 서비스가 요청을 처리하지 못했습니다.")
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
