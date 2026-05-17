package org.springframework.samples.petclinic.visit.dto;

public record PetValidationResponse(
        Long id,
        Long ownerId,
        String status
) {}
