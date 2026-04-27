package org.springframework.samples.petclinic.visit.dto;

import java.time.LocalDate;

public record VisitResponseDTO(
    Integer id,
    LocalDate visitDate,
    String description,
    Integer petId
) {}
