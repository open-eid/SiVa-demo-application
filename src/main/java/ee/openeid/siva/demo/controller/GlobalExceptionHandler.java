package ee.openeid.siva.demo.controller;

import lombok.Data;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxRequestSize;

    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse sizeLimitException(SizeLimitExceededException exception) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage(String.format("Maximum upload size of %s exceeded", maxRequestSize));
        errorResponse.setErrorCode("INTERNAL_SERVER_ERROR");
        return errorResponse;
    }

    @Data
    public class ErrorResponse {
        protected String errorCode;
        protected String errorMessage;
    }

}
