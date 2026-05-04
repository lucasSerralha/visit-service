package org.springframework.samples.petclinic.visit.client;

public class InactivePetException extends RuntimeException {
    public InactivePetException(Integer petId) {
        super("Pet with ID " + petId + " is inactive and cannot be scheduled for a visit.");
    }
}
