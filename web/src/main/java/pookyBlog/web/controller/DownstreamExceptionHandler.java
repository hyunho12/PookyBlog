package pookyBlog.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pookyBlog.common.Dto.Response.ErrorResponse;

@RestControllerAdvice
public class DownstreamExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleDownstreamError(WebClientResponseException exception) {
        HttpStatusCode status = exception.getStatusCode();
        ErrorResponse response = downstreamError(exception);
        return ResponseEntity.status(status).body(response);
    }

    private ErrorResponse downstreamError(WebClientResponseException exception) {
        if (exception.getStatusCode().is4xxClientError() && exception.getResponseBodyAsByteArray().length > 0) {
            try {
                JsonNode body = objectMapper.readTree(exception.getResponseBodyAsByteArray());
                JsonNode message = body.get("message");
                if (message != null && message.isTextual()) {
                    JsonNode code = body.get("code");
                    return ErrorResponse.builder()
                            .code(code != null && code.isTextual()
                                    ? code.asText()
                                    : Integer.toString(exception.getStatusCode().value()))
                            .message(message.asText())
                            .build();
                }
            } catch (Exception ignored) {
                // Fall back to the BFF error contract when downstream JSON is malformed.
            }
        }

        HttpStatusCode status = exception.getStatusCode();
        return ErrorResponse.builder()
                .code(Integer.toString(status.value()))
                .message(status.value() == 403
                        ? "요청을 수행할 권한이 없습니다."
                        : "내부 서비스가 요청을 처리하지 못했습니다.")
                .build();
    }
}
