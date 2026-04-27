package org.springframework.samples.petclinic.visit.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PetValidationException extends RuntimeException {
    public PetValidationException(String message) {
        super(message);
    }
}
