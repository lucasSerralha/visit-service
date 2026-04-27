package org.springframework.samples.petclinic.visit.web;

import org.springframework.samples.petclinic.visit.client.PetValidationException;
import org.springframework.samples.petclinic.visit.client.RemoteServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(VisitNotFoundException.class)
    public ProblemDetail handleVisitNotFoundException(VisitNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Visit Not Found");
        problemDetail.setType(URI.create("https://petclinic.org/errors/not-found"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(PetValidationException.class)
    public ProblemDetail handlePetValidationException(PetValidationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Pet Validation Failed");
        problemDetail.setType(URI.create("https://petclinic.org/errors/validation"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(RemoteServiceException.class)
    public ProblemDetail handleRemoteServiceException(RemoteServiceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problemDetail.setTitle("Remote Service Unavailable");
        problemDetail.setType(URI.create("https://petclinic.org/errors/remote-service"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
