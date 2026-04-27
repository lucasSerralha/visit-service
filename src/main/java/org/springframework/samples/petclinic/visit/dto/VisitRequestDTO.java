package org.springframework.samples.petclinic.visit.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VisitRequestDTO(
    @NotNull LocalDate visitDate,
    @NotEmpty String description,
    @NotNull Integer petId
) {}
