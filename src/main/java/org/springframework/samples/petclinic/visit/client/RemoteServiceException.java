package org.springframework.samples.petclinic.visit.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class RemoteServiceException extends RuntimeException {
    public RemoteServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
