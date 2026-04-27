package org.springframework.samples.petclinic.visit.web;

public class VisitNotFoundException extends RuntimeException {
    public VisitNotFoundException(Integer visitId) {
        super("Visit with ID " + visitId + " was not found.");
    }
}
