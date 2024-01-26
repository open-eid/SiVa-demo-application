/*
 * Copyright 2023 - 2024 Riigi Infosüsteemi Amet
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package ee.openeid.siva.demo.controller;

import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import lombok.Data;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxRequestSize;

    @ExceptionHandler(SizeLimitExceededException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse sizeLimitException(SizeLimitExceededException exception) {
        return handleSizeLimitException();
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse sizeLimitException(MaxUploadSizeExceededException exception) {
        return handleSizeLimitException();
    }

    private ErrorResponse handleSizeLimitException() {
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
