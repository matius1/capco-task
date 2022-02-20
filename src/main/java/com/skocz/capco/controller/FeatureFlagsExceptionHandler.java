package com.skocz.capco.controller;

import com.skocz.capco.exception.FeatureFlagException;
import com.skocz.capco.exception.UserNotFoundException;
import lombok.extern.java.Log;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log
public class FeatureFlagsExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = {UserNotFoundException.class, FeatureFlagException.class})
    protected Object handle(RuntimeException ex, WebRequest request) {
        log.info("Handling custom exception: " + ex.getMessage());
        ex.printStackTrace();
        return handleExceptionInternal(ex, ex.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}
